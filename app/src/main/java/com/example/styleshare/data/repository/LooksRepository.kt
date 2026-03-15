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
import com.example.styleshare.model.Look
import java.util.UUID
import kotlin.String
import kotlin.collections.List

class LooksRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).lookDao()
    private val commentDao = AppDatabase.getInstance(context).commentDao()

    /** מחזיר את הפיד */
    suspend fun getFeed(currentUid: String): List<Look> {
        return dao.getAllLooks().map { it.toModel(currentUid) }
    }

    /** מחזיר מועדפים */
    suspend fun getFavorites(currentUid: String): List<Look> {
        return dao.getFavorites(currentUid).map { it.toModel(currentUid) }
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
        imagePath: String,
        createdByUid: String,
        tags: List<String> = emptyList()
    ): String {
        val newId = UUID.randomUUID().toString()
        val entity = LookEntity(
            id = newId,
            title = title,
            description = description,
            imagePath = imagePath,
            favoritedBy = emptyList(),
            createdAt = System.currentTimeMillis(),
            createdByUid = createdByUid,
            tags = tags,
            likesCount = 0,
            commentsCount = 0
        )

        dao.upsert(entity)
        return newId
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
            tags = tags ?: current.tags // ✅ לא מוחק תגיות אם לא שלחת
        )

        dao.upsert(look = updated)
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
        dao.upsert(current.copy(favoritedBy = newFavoritedBy, likesCount = newLikesCount))
    }

    /** משנה לייקים (הדגמה - כרגע רק מגדיל/מקטין) */
    suspend fun incrementLike(lookId: String) {
        val current = dao.getById(lookId) ?: return
        dao.upsert(current.copy(likesCount = current.likesCount + 1))
    }

    /** מוחק לוק */
    suspend fun deleteLook(lookId: String) {
        dao.deleteById(lookId)
    }
    
    /** מחזיר רק את הלוקים של משתמש מסוים (MyLooks) */
    suspend fun getMyLooks(uid: String): List<Look> {
        return dao.getLooksByUser(uid).map { it.toModel(uid) }
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

    suspend fun addComment(lookId: String, text: String, authorName: String) {
        val commentEntity = com.example.styleshare.data.local.entity.CommentEntity(
            id = UUID.randomUUID().toString(),
            lookId = lookId,
            text = text,
            authorName = authorName,
            createdAt = System.currentTimeMillis()
        )
        commentDao.insertComment(commentEntity)
        
        // Update comment count on Look
        val currentLook = dao.getById(lookId)
        if (currentLook != null) {
            dao.upsert(currentLook.copy(commentsCount = currentLook.commentsCount + 1))
        }
    }

    /** המרה Entity -> Model */
    private fun LookEntity.toModel(currentUid: String): Look = Look(
        id = id,
        title = title,
        description = description,
        imagePath = imagePath,
        isFavorite = favoritedBy.contains(currentUid),
        createdAt = createdAt,
        createdByUid = createdByUid,
        tags = tags,
        likesCount = likesCount,
        commentsCount = commentsCount
    )
}
