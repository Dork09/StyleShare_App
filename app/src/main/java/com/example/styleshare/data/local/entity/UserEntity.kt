/**
 * מטרת הקובץ:
 * Entity של פרופיל משתמש שנשמר ב-Room.
 */
package com.example.styleshare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val fullName: String,
    val bio: String,
    val imagePath: String?
)
