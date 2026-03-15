/**
 * מטרת הקובץ:
 * ViewModel למסך פרופיל:
 * - טעינת פרטי משתמש מ-Room דרך Repository
 * - שמירת BIO / שם מלא / תמונת פרופיל
 * - התנתקות (Firebase Auth)
 */
package com.example.styleshare.ui.profile

import android.app.Application
import androidx.lifecycle.*
import com.example.styleshare.data.local.entity.UserEntity
import com.example.styleshare.data.repository.AuthRepository
import com.example.styleshare.data.repository.UserProfileRepository
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.utils.Result
import kotlinx.coroutines.launch

class ProfileViewModel(app: Application) : AndroidViewModel(app) {

    /** Repository של פרופיל (Room) */
    private val profileRepo = UserProfileRepository(app.applicationContext)

    /** Repository של Auth (Firebase) */
    private val authRepo = AuthRepository()

    /** Repository של Looks לחישוב סטטיסטיקות */
    private val looksRepo = LooksRepository(app.applicationContext)

    private val _state = MutableLiveData<Result<UserEntity>>()
    val state: LiveData<Result<UserEntity>> = _state

    // Pair of (LooksCount, TotalLikesCount)
    private val _stats = MutableLiveData<Pair<Int, Int>>()
    val stats: LiveData<Pair<Int, Int>> = _stats

    /**
     * מטרת הפונקציה:
     * טוענת את הפרופיל של המשתמש הנוכחי:
     * - אם קיים ב-Room -> מחזירה אותו
     * - אם לא קיים -> יוצרת פרופיל ברירת מחדל ומחזירה
     */
    fun loadProfile() {
        viewModelScope.launch {
            _state.value = Result.Loading

            try {
                val uid = authRepo.currentUid() ?: "guest"
                
                // Fetch stats concurrently
                val myLooks = looksRepo.getMyLooks(uid)
                val looksCount = myLooks.size
                val totalLikes = myLooks.sumOf { it.likesCount }
                _stats.value = Pair(looksCount, totalLikes)

                val user = profileRepo.getProfile(uid)

                if (user != null) {
                    _state.value = Result.Success(user)
                } else {
                    // Create default profile to prevent infinite loading
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    val fallbackName = firebaseUser?.displayName?.takeIf { it.isNotBlank() } ?: "משתמש/ת חדש/ה"

                    val defaultUser = UserEntity(
                        uid = uid,
                        fullName = fallbackName,
                        bio = "כאן יהיה ה-BIO שלך ✨",
                        imagePath = null
                    )
                    profileRepo.saveProfile(defaultUser)
                    _state.value = Result.Success(defaultUser)
                }            } catch (e: Exception) {
                _state.value = Result.Error(e.message ?: "שגיאה בטעינת הפרופיל")
            }
        }
    }

    /**
     * מטרת הפונקציה:
     * שומרת פרופיל ב-Room (שם מלא + BIO + תמונה)
     */
    fun saveProfile(fullName: String, bio: String, imagePath: String?) {
        viewModelScope.launch {
            try {
                val uid = authRepo.currentUid() ?: "guest"

                val updated = UserEntity(
                    uid = uid,
                    fullName = fullName,
                    bio = bio,
                    imagePath = imagePath
                )

                profileRepo.saveProfile(updated)
                _state.value = Result.Success(updated)

            } catch (e: Exception) {
                _state.value = Result.Error(e.message ?: "שגיאה בשמירת הפרופיל")
            }
        }
    }

    /**
     * מטרת הפונקציה:
     * Logout מהמשתמש ב-Firebase
     */
    fun logout() {
        authRepo.logout()
    }
}
