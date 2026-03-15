/**
 * מטרת הקובץ:
 * ViewModel לעריכת לוק:
 * - טעינת לוק לפי id
 * - עדכון לוק
 * - מחיקה
 */
package com.example.styleshare.ui.mylooks

import android.content.Context
import androidx.lifecycle.*
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.model.Look
import com.example.styleshare.utils.Result
import kotlinx.coroutines.launch

class EditLookViewModel : ViewModel() {

    private var looksRepo: LooksRepository? = null

    private val _lookState = MutableLiveData<Result<Look>>()
    val lookState: LiveData<Result<Look>> = _lookState

    private val _saveState = MutableLiveData<Result<Unit>>()
    val saveState: LiveData<Result<Unit>> = _saveState

    /** init */
    fun init(context: Context) {
        if (looksRepo == null) looksRepo = LooksRepository(context.applicationContext)
    }

    /** טעינת לוק */
    fun loadLook(lookId: String) {
        viewModelScope.launch {
            _lookState.value = Result.Loading
            try {
                val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val look = looksRepo?.getLookById(lookId, currentUid)
                if (look != null) _lookState.value = Result.Success(look)
                else _lookState.value = Result.Error("Look not found")
            } catch (e: Exception) {
                _lookState.value = Result.Error(e.message ?: "Load failed")
            }
        }
    }

    /** שמירה */
    fun updateLook(lookId: String, title: String, desc: String, imagePath: String, tags: List<String>) {
        viewModelScope.launch {
            _saveState.value = Result.Loading
            try {
                looksRepo?.updateLook(lookId, title, desc, imagePath, tags)
                _saveState.value = Result.Success(Unit)
            } catch (e: Exception) {
                _saveState.value = Result.Error(e.message ?: "Update failed")
            }
        }
    }

    /** מחיקה */
    fun deleteLook(lookId: String) {
        viewModelScope.launch {
            looksRepo?.deleteLook(lookId)
        }
    }
}
