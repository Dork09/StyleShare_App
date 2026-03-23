package com.example.styleshare.data.repository

import android.content.Context
import com.example.styleshare.data.local.db.AppDatabase
import com.example.styleshare.data.local.entity.LookEntity
import com.example.styleshare.data.local.entity.UserEntity
import com.example.styleshare.data.remote.firebase.LookRemoteDto
import com.example.styleshare.data.remote.firebase.LooksRemoteDataSource
import com.example.styleshare.model.Look
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class LooksRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).lookDao()
    private val commentDao = AppDatabase.getInstance(context).commentDao()
    private val userDao = AppDatabase.getInstance(context).userDao()
    private val remote = LooksRemoteDataSource()

    suspend fun getFeed(currentUid: String): List<Look> =
        dao.getAllLooks().mapToLooks(currentUid)

    suspend fun getFavorites(currentUid: String): List<Look> =
        dao.getFavorites(currentUid).mapToLooks(currentUid)

    suspend fun getLookById(lookId: String, currentUid: String): Look? =
        dao.getById(lookId)?.toModel(currentUid)

    /**
     * Saves a new look. [imageUrl] must already be a Cloudinary HTTPS URL —
     * upload via uploadImageToCloudinary() in the ViewModel before calling this.
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
    ): String = withContext(Dispatchers.IO) {
        val newId = UUID.randomUUID().toString()
        val entity = LookEntity(
            id = newId,
            title = title,
            description = description,
            imageUrl = imageUrl,
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

    /**
     * Updates an existing look. [imageUrl] is the Cloudinary URL — either freshly
     * uploaded (if image changed) or the existing URL (if image unchanged).
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
            imageUrl = imageUrl,
            tags = tags ?: current.tags
        )
        dao.upsert(updated)
        syncLookToRemote(updated)
    }

    suspend fun toggleFavorite(lookId: String, currentUid: String) {
        val current = dao.getById(lookId) ?: return
        val isCurrentlyFavorited = current.favoritedBy.contains(currentUid)
        val newFavoritedBy = if (isCurrentlyFavorited) {
            current.favoritedBy.filter { it != currentUid }
        } else {
            current.favoritedBy + currentUid
        }
        val newLikesCount = if (!isCurrentlyFavorited) current.likesCount + 1
                            else maxOf(0, current.likesCount - 1)
        val updated = current.copy(favoritedBy = newFavoritedBy, likesCount = newLikesCount)
        dao.upsert(updated)
        syncLookToRemote(updated)
    }

    suspend fun incrementLike(lookId: String) {
        val current = dao.getById(lookId) ?: return
        val updated = current.copy(likesCount = current.likesCount + 1)
        dao.upsert(updated)
        syncLookToRemote(updated)
    }

    suspend fun deleteLook(lookId: String) {
        dao.deleteById(lookId)
        runCatching { remote.deleteLook(lookId) }
    }

    suspend fun getMyLooks(uid: String): List<Look> =
        dao.getLooksByUser(uid).mapToLooks(uid)

    suspend fun refreshLooksFromRemote(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (!force && now - lastRemoteSyncAt < REMOTE_SYNC_TTL_MS) return
        syncRemoteLooksToLocal()
        lastRemoteSyncAt = System.currentTimeMillis()
    }

    // --- COMMENTS ---

    suspend fun getCommentsForLook(lookId: String): List<com.example.styleshare.model.Comment> =
        commentDao.getCommentsForLook(lookId).map {
            com.example.styleshare.model.Comment(
                id = it.id,
                lookId = it.lookId,
                text = it.text,
                authorName = it.authorName,
                createdAt = it.createdAt
            )
        }

    suspend fun addComment(lookId: String, text: String, authorName: String? = null) {
        val resolvedAuthorName = authorName?.trim().takeUnless { it.isNullOrEmpty() }
            ?: resolveCurrentUserName()
        val commentEntity = com.example.styleshare.data.local.entity.CommentEntity(
            id = UUID.randomUUID().toString(),
            lookId = lookId,
            text = text,
            authorName = resolvedAuthorName,
            createdAt = System.currentTimeMillis()
        )
        commentDao.insertComment(commentEntity)

        val currentLook = dao.getById(lookId)
        if (currentLook != null) {
            val updated = currentLook.copy(commentsCount = currentLook.commentsCount + 1)
            dao.upsert(updated)
            syncLookToRemote(updated)
        }
    }

    // --- private helpers ---

    private suspend fun List<LookEntity>.mapToLooks(currentUid: String): List<Look> {
        val looks = ArrayList<Look>(size)
        for (entity in this) looks += entity.toModel(currentUid)
        return looks
    }

    private suspend fun LookEntity.toModel(currentUid: String) = Look(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
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

    private suspend fun syncRemoteLooksToLocal() {
        val remoteLooks = runCatching { remote.getAllLooks() }.getOrNull() ?: return
        val localLooks = dao.getAllLooks()

        if (remoteLooks.isEmpty()) {
            if (localLooks.isNotEmpty()) localLooks.forEach { syncLookToRemote(it) }
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
        val remoteLook = LookRemoteDto(
            id = look.id,
            title = look.title,
            description = look.description,
            imageUrl = look.imageUrl,
            createdAt = look.createdAt,
            createdByUid = look.createdByUid,
            authorName = authorName,
            tags = look.tags,
            likesCount = look.likesCount,
            commentsCount = look.commentsCount,
            favoritedBy = look.favoritedBy
        )
        runCatching {
            withTimeoutOrNull(8_000L) { remote.upsertLook(remoteLook) }
        }
    }

    private fun LookRemoteDto.toEntity() = LookEntity(
        id = id,
        title = title,
        description = description,
        imageUrl = imageUrl,
        favoritedBy = favoritedBy,
        createdAt = createdAt,
        createdByUid = createdByUid,
        tags = tags,
        likesCount = likesCount,
        commentsCount = commentsCount
    )

    private companion object {
        private var lastRemoteSyncAt: Long = 0L
        private const val REMOTE_SYNC_TTL_MS = 60_000L
    }
}
