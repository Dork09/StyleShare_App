/**
 * מטרת הקובץ:
 * Repository להתחברות/הרשמה/התנתקות באמצעות Firebase Auth בלבד.
 */
package com.example.styleshare.data.repository

import com.example.styleshare.data.remote.firebase.AuthRemoteDataSource

class AuthRepository(
    private val remote: AuthRemoteDataSource = AuthRemoteDataSource()
) {

    /** התחברות */
    suspend fun login(email: String, password: String) {
        remote.login(email, password)
    }

    /** הרשמה */
    suspend fun register(email: String, password: String, username: String) {
        remote.register(email, password, username)
    }

    /** התנתקות */
    fun logout() {
        remote.logout()
    }

    /** UID של המשתמש */
    fun currentUid(): String? = remote.currentUid()

    /** Email של המשתמש */
    fun currentEmail(): String? = remote.currentEmail()

    /** האם יש משתמש מחובר */
    fun isLoggedIn(): Boolean = remote.currentUid() != null

}
