package com.example.styleshare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val lookId: String,
    val text: String,
    val authorName: String,
    val createdAt: Long
)
