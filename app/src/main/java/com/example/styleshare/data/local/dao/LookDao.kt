/**
 * מטרת הקובץ:
 * DAO ללוקים - כל הפעולות על טבלת looks.
 */
package com.example.styleshare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.styleshare.data.local.entity.LookEntity

@Dao
interface LookDao {

    /** מחזיר את כל הלוקים */
    @Query("SELECT * FROM looks ORDER BY createdAt DESC")
    suspend fun getAllLooks(): List<LookEntity>

    /** מחזיר לוקים מועדפים בלבד (לפי UID) */
    @Query("SELECT * FROM looks WHERE favoritedBy LIKE '%' || :uid || '%' ORDER BY createdAt DESC")
    suspend fun getFavorites(uid: String): List<LookEntity>

    /** מחזיר לוקים שנוצרו ע״י משתמש מסוים */
    @Query("SELECT * FROM looks WHERE createdByUid = :uid ORDER BY createdAt DESC")
    suspend fun getMyLooks(uid: String): List<LookEntity>

    /** מחזיר לוק לפי ID */
    @Query("SELECT * FROM looks WHERE id = :lookId LIMIT 1")
    suspend fun getById(lookId: String): LookEntity?

    /** הוספה/עדכון */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(look: LookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(looks: List<LookEntity>)

    /** מחיקה */
    @Query("DELETE FROM looks WHERE id = :lookId")
    suspend fun deleteById(lookId: String)

    @Query("DELETE FROM looks")
    suspend fun deleteAll()

    @Query("DELETE FROM looks WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)

    /**
     * מחזיר לוקים לפי משתמש יוצר (למסך MyLooks)
     */
    @Query("SELECT * FROM looks WHERE createdByUid = :uid ORDER BY createdAt DESC")
    suspend fun getLooksByUser(uid: String): List<LookEntity>

    /**
     * מחזיר לוקים לפי משתמש יוצר ומועדפים
     */
    @Query("SELECT * FROM looks WHERE createdByUid = :creatorUid AND favoritedBy LIKE '%' || :favoriteUid || '%' ORDER BY createdAt DESC")
    suspend fun getFavoritesByUser(creatorUid: String, favoriteUid: String): List<LookEntity>


}
