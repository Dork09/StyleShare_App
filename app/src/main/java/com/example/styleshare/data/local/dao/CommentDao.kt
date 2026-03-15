package com.example.styleshare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.styleshare.data.local.entity.CommentEntity

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE lookId = :lookId ORDER BY createdAt ASC")
    suspend fun getCommentsForLook(lookId: String): List<CommentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
}
