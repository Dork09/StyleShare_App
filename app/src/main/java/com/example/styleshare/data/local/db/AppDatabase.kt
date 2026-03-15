/**
 * מטרת הקובץ:
 * Room Database - מחזיק DAOs של האפליקציה.
 *
 * מה נשמר ב-DB:
 * 1) LookEntity - לוקים (כולל תגיות)
 * 2) UserEntity - פרופיל משתמש (שם מלא + BIO + תמונה)
 *
 * הערה:
 * כרגע אנחנו בתחילת הפרויקט, לכן אנחנו משתמשים ב:
 * fallbackToDestructiveMigration()
 * כדי שאם נשנה שדות בזמן פיתוח - DB ימחק וייבנה מחדש בלי קריסות.
 */
package com.example.styleshare.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.styleshare.data.local.dao.LookDao
import com.example.styleshare.data.local.dao.UserDao
import com.example.styleshare.data.local.dao.CommentDao
import com.example.styleshare.data.local.entity.LookEntity
import com.example.styleshare.data.local.entity.UserEntity
import com.example.styleshare.data.local.entity.CommentEntity
import com.example.styleshare.data.local.db.Converters

@Database(
    entities = [LookEntity::class, UserEntity::class, CommentEntity::class],
    version = 3, // ✅ העלינו גרסה כי שינינו שדות שוב
    exportSchema = false
)
@TypeConverters(Converters::class) // ✅ מאפשר שמירה של List<String> (תגיות) ב-Room
abstract class AppDatabase : RoomDatabase() {

    /** DAO לוקים */
    abstract fun lookDao(): LookDao

    /** DAO פרופיל */
    abstract fun userDao(): UserDao

    /** DAO תגובות */
    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** מחזיר מופע Singleton של DB */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "styleshare_db"
                )
                    // ✅ בזמן פיתוח: מוחק DB ישנה אם שינינו סכימה
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
