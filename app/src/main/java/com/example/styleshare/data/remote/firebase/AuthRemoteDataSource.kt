/**
 * מטרת הקובץ:
 * DataSource שמדבר מול Firebase Authentication בלבד (לפי הנחיות המרצה).
 * אחראי על:
 * - Register
 * - Login
 * - Logout
 * - קבלת המשתמש הנוכחי
 */
package com.example.styleshare.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRemoteDataSource {

    private val auth = FirebaseAuth.getInstance()

    /**
     * התחברות משתמש עם אימייל וסיסמה.
     */
    suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    /**
     * הרשמת משתמש חדש עם אימייל וסיסמה.
     */
    suspend fun register(email: String, password: String, username: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.let { user ->
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build()
            user.updateProfile(profileUpdates).await()
        }
    }

    /**
     * התנתקות מהמשתמש הנוכחי.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * מחזיר UID של המשתמש הנוכחי (או null אם לא מחובר).
     */
    fun currentUid(): String? = auth.currentUser?.uid

    /**
     * מחזיר Email של המשתמש הנוכחי (או null).
     */
    fun currentEmail(): String? = auth.currentUser?.email
}
