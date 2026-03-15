/**
 * מטרת הקובץ:
 * DAO לפרופיל משתמש ב-Room.
 */
package com.example.styleshare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.styleshare.data.local.entity.UserEntity

@Dao
interface UserDao {

    /** מביא פרופיל לפי UID */
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): UserEntity?

    /** שומר/מעדכן פרופיל */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)
}
