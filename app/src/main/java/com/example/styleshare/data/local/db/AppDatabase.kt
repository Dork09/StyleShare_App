package com.example.styleshare.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.styleshare.data.local.dao.CommentDao
import com.example.styleshare.data.local.dao.LookDao
import com.example.styleshare.data.local.dao.UserDao
import com.example.styleshare.data.local.entity.CommentEntity
import com.example.styleshare.data.local.entity.LookEntity
import com.example.styleshare.data.local.entity.UserEntity

@Database(
    entities = [LookEntity::class, UserEntity::class, CommentEntity::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lookDao(): LookDao

    abstract fun userDao(): UserDao

    abstract fun commentDao(): CommentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "styleshare_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
