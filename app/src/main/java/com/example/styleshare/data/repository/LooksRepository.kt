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
import com.example.styleshare.data.local.entity.CommentEntity
import com.example.styleshare.data.local.entity.LookEntity
import com.example.styleshare.model.Look
import com.example.styleshare.utils.ImageStorage
import com.example.styleshare.utils.LookMemoryCache
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LooksRepository(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val dao = database.lookDao()
    private val commentDao = database.commentDao()
    private val userDao = database.userDao()

    /** מחזיר את הפיד */
    suspend fun getFeed(currentUid: String): List<Look> {
        val cacheKey = "feed:$currentUid"
        LookMemoryCache.getList(cacheKey)?.let { return it }
        return dao.getAllLooks().mapToLooks(currentUid).also {
            LookMemoryCache.putList(cacheKey, it)
        }
    }

    /** מחזיר מועדפים */
    suspend fun getFavorites(currentUid: String): List<Look> {
        val cacheKey = "favorites:$currentUid"
        LookMemoryCache.getList(cacheKey)?.let { return it }
        return dao.getFavorites(currentUid).mapToLooks(currentUid).also {
            LookMemoryCache.putList(cacheKey, it)
        }
    }

    /** מביא לוק לפי id */
    suspend fun getLookById(lookId: String, currentUid: String): Look? {
        return dao.getById(lookId)?.toModel(currentUid)?.also {
            LookMemoryCache.putLook(it)
        }
    }

    /**
     * יוצר לוק חדש ושומר אותו ב-Room (SQLite)
     */
    suspend fun createLook(
        title: String,
        description: String,
        imagePath: String,
        createdByUid: String,
        tags: List<String> = emptyList(),
        likesCount: Int = 0,
        commentsCount: Int = 0,
        createdAt: Long = System.currentTimeMillis()
    ): String {
        return withContext(Dispatchers.IO) {
            val newId = UUID.randomUUID().toString()
            val entity = LookEntity(
                id = newId,
                title = title,
                description = description,
                imagePath = imagePath,
                favoritedBy = emptyList(),
                createdAt = createdAt,
                createdByUid = createdByUid,
                tags = tags,
                likesCount = likesCount,
                commentsCount = commentsCount
            )

            dao.upsert(entity)
            invalidateLookCaches()
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
        imagePath: String,
        tags: List<String>? = null
    ) {
        val current = dao.getById(lookId) ?: return

        val updated = current.copy(
            title = title,
            description = description,
            imagePath = imagePath,
            tags = tags ?: current.tags
        )

        dao.upsert(updated)
        invalidateLookCaches()
        if (current.imagePath != imagePath) {
            ImageStorage.deleteImageIfInternal(appContext, current.imagePath)
        }
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
        
        val newLikesCount = if (!isCurrentlyFavorited) current.likesCount + 1 else maxOf(0, current.likesCount - 1)
        val updated = current.copy(favoritedBy = newFavoritedBy, likesCount = newLikesCount)
        dao.upsert(updated)
        invalidateLookCaches()
    }

    /** משנה לייקים (הדגמה - כרגע רק מגדיל/מקטין) */
    suspend fun incrementLike(lookId: String) {
        val current = dao.getById(lookId) ?: return
        val updated = current.copy(likesCount = current.likesCount + 1)
        dao.upsert(updated)
        invalidateLookCaches()
    }

    /** מוחק לוק */
    suspend fun deleteLook(lookId: String) {
        val current = dao.getById(lookId) ?: return
        commentDao.deleteCommentsForLook(lookId)
        dao.deleteById(lookId)
        ImageStorage.deleteImageIfInternal(appContext, current.imagePath)
        invalidateLookCaches()
    }
    
    /** מחזיר רק את הלוקים של משתמש מסוים (MyLooks) */
    suspend fun getMyLooks(uid: String): List<Look> {
        val cacheKey = "myLooks:$uid"
        LookMemoryCache.getList(cacheKey)?.let { return it }
        return dao.getLooksByUser(uid).mapToLooks(uid).also {
            LookMemoryCache.putList(cacheKey, it)
        }
    }

    // --- COMMENTS ---

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
        val commentEntity = CommentEntity(
            id = UUID.randomUUID().toString(),
            lookId = lookId,
            text = text,
            authorName = resolvedAuthorName,
            createdAt = System.currentTimeMillis()
        )
        commentDao.insertComment(commentEntity)
        
        // Update comment count on Look
        val currentLook = dao.getById(lookId)
        if (currentLook != null) {
            val updated = currentLook.copy(commentsCount = currentLook.commentsCount + 1)
            dao.upsert(updated)
            invalidateLookCaches()
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
        imagePath = imagePath,
        authorName = resolveAuthorName(createdByUid),
        isFavorite = favoritedBy.contains(currentUid),
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

    private fun invalidateLookCaches() {
        LookMemoryCache.invalidateAll()
    }
}
