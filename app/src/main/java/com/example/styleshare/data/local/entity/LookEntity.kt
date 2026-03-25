/**
 * מטרת הקובץ:
 * Entity של Room לטבלת looks.
 */
package com.example.styleshare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "looks")
data class LookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageUrl: String?,
    val favoritedBy: List<String> = emptyList(),
    val likedBy: List<String> = emptyList(),
    val createdAt: Long,
    val createdByUid: String,
    val tags: List<String> = emptyList(),
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)
