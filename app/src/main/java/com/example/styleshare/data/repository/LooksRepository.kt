/**
 * מטרת הקובץ:
 * Repository ללוקים - שמירה מקומית ב-Room (SQLite) בלבד.
 * אחראי על:
 * - יצירת לוק
 * - טעינת פיד
 * - מועדפים
 * - עדכון/מחיקה
 */
package com.example.styleshare.data.repository

import android.content.Context
import com.example.styleshare.data.local.db.AppDatabase
import com.example.styleshare.data.local.entity.LookEntity
import com.example.styleshare.data.local.entity.UserEntity
import com.example.styleshare.data.remote.firebase.LookImagesRemoteDataSource
import com.example.styleshare.data.remote.firebase.LookRemoteDto
import com.example.styleshare.data.remote.firebase.LooksRemoteDataSource
import com.example.styleshare.model.Look
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.String
import kotlin.collections.List

class LooksRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).lookDao()
    private val commentDao = AppDatabase.getInstance(context).commentDao()
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val remote = LooksRemoteDataSource()
    private val remoteImages = LookImagesRemoteDataSource()

    /** מחזיר את הפיד */
    suspend fun getFeed(currentUid: String): List<Look> {
        return dao.getAllLooks().mapToLooks(currentUid)
    }

    /** מחזיר מועדפים */
    suspend fun getFavorites(currentUid: String): List<Look> {
        return dao.getFavorites(currentUid).mapToLooks(currentUid)
    }

    /** מביא לוק לפי id */
    suspend fun getLookById(lookId: String, currentUid: String): Look? {
        return dao.getById(lookId)?.toModel(currentUid)
    }

    /**
     * יוצר לוק חדש ושומר אותו ב-Room (SQLite)
     */
    suspend fun createLook(
        title: String,
        description: String,
        imageUrl: String,
        createdByUid: String,
        tags: List<String> = emptyList(),
        likesCount: Int = 0,
        commentsCount: Int = 0,
        createdAt: Long = System.currentTimeMillis()
    ): String {
        return withContext(Dispatchers.IO) {
            val newId = UUID.randomUUID().toString()
            val resolvedImagePath = resolveRemoteImagePath(imageUrl, createdByUid)
            val entity = LookEntity(
                id = newId,
                title = title,
                description = description,
                imageUrl = resolvedImagePath,
                favoritedBy = emptyList(),
                createdAt = createdAt,
                createdByUid = createdByUid,
                tags = tags,
                likesCount = likesCount,
                commentsCount = commentsCount
            )

            dao.upsert(entity)
            syncLookToRemote(entity)
            newId
        }
    }

    /**
     * מטרת הפונקציה:
     * עדכון לוק קיים ב-Room
     * ✅ אם לא שלחת tags חדשים, נשמרים הישנים
     */
    suspend fun updateLook(
        lookId: String,
        title: String,
        description: String,
        imageUrl: String,
        tags: List<String>? = null
    ) {
        val current = dao.getById(lookId) ?: return

        val updated = current.copy(
            title = title,
            description = description,
            imageUrl = resolveRemoteImagePath(imageUrl, current.createdByUid),
            tags = tags ?: current.tags // ✅ לא מוחק תגיות אם לא שלחת
        )

        dao.upsert(look = updated)
        syncLookToRemote(updated)
    }

    /** משנה מועדפים וכמות לייקים בהתאמה */
    suspend fun toggleFavorite(lookId: String, currentUid: String) {
        val current = dao.getById(lookId) ?: return
        
        val isCurrentlyFavorited = current.favoritedBy.contains(currentUid)
        val newFavoritedBy = if (isCurrentlyFavorited) {
            current.favoritedBy.filter { it != currentUid }
        } else {
            current.favoritedBy + currentUid
        }

        val updated = current.copy(favoritedBy = newFavoritedBy)
        dao.upsert(updated)
        syncLookToRemote(updated)
    }

    /** מחליף לייק - לא קשור למועדפים */
    suspend fun toggleLike(lookId: String, currentUid: String) {
        val current = dao.getById(lookId) ?: return

        val isCurrentlyLiked = current.likedBy.contains(currentUid)
        val newLikedBy = if (isCurrentlyLiked) {
            current.likedBy.filter { it != currentUid }
        } else {
            current.likedBy + currentUid
        }

        val updated = current.copy(
            likedBy = newLikedBy,
            likesCount = newLikedBy.size
        )
        dao.upsert(updated)
        syncLookToRemote(updated)
    }

    /** מוחק לוק */
    suspend fun deleteLook(lookId: String) {
        dao.deleteById(lookId)
        runCatching { remote.deleteLook(lookId) }
    }
    
    /** מחזיר רק את הלוקים של משתמש מסוים (MyLooks) */
    suspend fun getMyLooks(uid: String): List<Look> {
        return dao.getLooksByUser(uid).mapToLooks(uid)
    }

    suspend fun refreshLooksFromRemote(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastRemoteSyncAt < REMOTE_SYNC_TTL_MS) return
        syncRemoteLooksToLocal()
        lastRemoteSyncAt = System.currentTimeMillis()
    }

    // --- COMMESTS ---

    suspend fun getCommentsForLook(lookId: String): List<com.example.styleshare.model.Comment> {
        return commentDao.getCommentsForLook(lookId).map {
            com.example.styleshare.model.Comment(
                id = it.id,
                lookId = it.lookId,
                text = it.text,
                authorName = it.authorName,
                createdAt = it.createdAt
            )
        }
    }

    suspend fun addComment(lookId: String, text: String, authorName: String? = null) {
        val resolvedAuthorName = authorName?.trim().takeUnless { it.isNullOrEmpty() } ?: resolveCurrentUserName()
        val commentEntity = com.example.styleshare.data.local.entity.CommentEntity(
            id = UUID.randomUUID().toString(),
            lookId = lookId,
            text = text,
            authorName = resolvedAuthorName,
            createdAt = System.currentTimeMillis()
        )
        commentDao.insertComment(commentEntity)

        // Derive count from actual rows to stay in sync
        val currentLook = dao.getById(lookId)
        if (currentLook != null) {
            val realCount = commentDao.countCommentsForLook(lookId)
            val updated = currentLook.copy(commentsCount = realCount)
            dao.upsert(updated)
            syncLookToRemote(updated)
        }
    }

    private suspend fun List<LookEntity>.mapToLooks(currentUid: String): List<Look> {
        val looks = ArrayList<Look>(size)
        for (entity in this) {
            looks += entity.toModel(currentUid)
        }
        return looks
    }

    /** המרה Entity -> Model */
    private suspend fun LookEntity.toModel(currentUid: String): Look = Look(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        authorName = resolveAuthorName(createdByUid),
        isFavorite = favoritedBy.contains(currentUid),
        isLiked = likedBy.contains(currentUid),
        createdAt = createdAt,
        createdByUid = createdByUid,
        tags = tags,
        likesCount = likesCount,
        commentsCount = commentsCount
    )

    private suspend fun resolveAuthorName(authorUid: String): String {
        val storedName = userDao.getByUid(authorUid)?.fullName?.trim().orEmpty()
        if (storedName.isNotBlank()) return storedName

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser?.uid == authorUid) {
            val displayName = currentUser.displayName?.trim().orEmpty()
            if (displayName.isNotBlank()) return displayName

            val emailPrefix = currentUser.email?.substringBefore("@").orEmpty()
            if (emailPrefix.isNotBlank()) return emailPrefix
        }

        return "User"
    }

    private suspend fun resolveCurrentUserName(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return "Guest User"
        return resolveAuthorName(currentUser.uid)
    }

    private suspend fun syncRemoteLooksToLocal() {
        val remoteLooks = runCatching { remote.getAllLooks() }.getOrNull() ?: return
        val localLooks = dao.getAllLooks()

        if (remoteLooks.isEmpty()) {
            if (localLooks.isNotEmpty()) {
                localLooks.forEach { localLook ->
                    syncLookToRemote(localLook)
                }
            }
            return
        }

        dao.upsertAll(remoteLooks.map { it.toEntity() })
        dao.deleteAllExcept(remoteLooks.map { it.id })

        remoteLooks.forEach { remoteLook ->
            if (remoteLook.createdByUid.isNotBlank() && remoteLook.authorName.isNotBlank()) {
                userDao.upsert(
                    UserEntity(
                        uid = remoteLook.createdByUid,
                        fullName = remoteLook.authorName,
                        bio = "",
                        imagePath = null
                    )
                )
            }
        }
    }

    private suspend fun syncLookToRemote(look: LookEntity) {
        val authorName = resolveAuthorName(look.createdByUid)
        val remoteImagePath = resolveRemoteImagePath(look.imageUrl ?: "", look.createdByUid)
        val remoteLook = LookRemoteDto(
            id = look.id,
            title = look.title,
            description = look.description,
            imageUrl = remoteImagePath,
            createdAt = look.createdAt,
            createdByUid = look.createdByUid,
            authorName = authorName,
            tags = look.tags,
            likesCount = look.likesCount,
            commentsCount = look.commentsCount,
            favoritedBy = look.favoritedBy,
            likedBy = look.likedBy
        )

        runCatching {
            withTimeoutOrNull(8_000L) {
                remote.upsertLook(remoteLook)
            }
        }

        if (remoteImagePath != look.imageUrl && remoteImagePath.startsWith("http")) {
            dao.upsert(look.copy(imageUrl = remoteImagePath))
        }
    }

    private suspend fun resolveRemoteImagePath(imagePath: String, createdByUid: String): String {
        if (imagePath.startsWith("http")) return imagePath
        return runCatching {
            withTimeoutOrNull(8_000L) {
                remoteImages.uploadLookImage(
                    localImagePath = imagePath,
                    userUid = createdByUid
                )
            } ?: imagePath
        }.getOrDefault(imagePath)
    }

    private fun LookRemoteDto.toEntity(): LookEntity {
        return LookEntity(
            id = id,
            title = title,
            description = description,
            imageUrl = imageUrl,
            favoritedBy = favoritedBy,
            likedBy = likedBy,
            createdAt = createdAt,
            createdByUid = createdByUid,
            tags = tags,
            likesCount = likesCount,
            commentsCount = commentsCount
        )
    }

    private companion object {
        private var lastRemoteSyncAt: Long = 0L
        private const val REMOTE_SYNC_TTL_MS = 60_000L
    }
}
