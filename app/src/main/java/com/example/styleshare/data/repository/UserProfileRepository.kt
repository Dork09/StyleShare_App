package com.example.styleshare.data.repository

import android.content.Context
import com.example.styleshare.data.local.db.AppDatabase
import com.example.styleshare.data.local.entity.UserEntity

class UserProfileRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).userDao()

    suspend fun getProfile(uid: String): UserEntity? {
        return dao.getByUid(uid)
    }

    suspend fun saveProfile(profile: UserEntity) {
        dao.upsert(profile)
    }
}
