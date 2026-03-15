/**
 * מטרת הקובץ:
 * ViewModel להתחברות והרשמה (MVVM + LiveData).
 */
package com.example.styleshare.ui.auth

import android.app.Application
import androidx.lifecycle.*
import com.example.styleshare.data.repository.AuthRepository
import com.example.styleshare.data.repository.UserProfileRepository
import com.example.styleshare.data.local.entity.UserEntity
import com.example.styleshare.utils.Result
import kotlinx.coroutines.launch

class AuthViewModel(app: Application) : AndroidViewModel(app) {

    private val authRepo = AuthRepository()
    private val profileRepo = UserProfileRepository(app.applicationContext)

    private val _state = MutableLiveData<Result<Unit>>()
    val state: LiveData<Result<Unit>> = _state

    /** בדיקה אם משתמש כבר מחובר */
    fun isLoggedIn(): Boolean = authRepo.isLoggedIn()

    /** התחברות */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = Result.Loading
            try {
                authRepo.login(email, password)
                _state.value = Result.Success(Unit)
            } catch (e: Exception) {
                _state.value = Result.Error(e.message ?: "Login failed")
            }
        }
    }

    /** הרשמה */
    fun register(email: String, password: String, username: String) {
        viewModelScope.launch {
            _state.value = Result.Loading
            try {
                authRepo.register(email, password, username)
                
                // Save user profile locally
                val uid = authRepo.currentUid() ?: "guest"
                val defaultUser = UserEntity(
                    uid = uid,
                    fullName = username,
                    bio = "כאן יהיה ה-BIO שלך ✨",
                    imagePath = null
                )
                profileRepo.saveProfile(defaultUser)
                
                _state.value = Result.Success(Unit)
            } catch (e: Exception) {
                _state.value = Result.Error(e.message ?: "Register failed")
            }
        }
    }
}
