/**
 * מטרת הקובץ:
 * ViewModel למסך MyLooks:
 * מציג רק לוקים של המשתמש המחובר (createdByUid)
 */
package com.example.styleshare.ui.mylooks

import android.content.Context
import androidx.lifecycle.*
import com.example.styleshare.data.repository.AuthRepository
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.model.Look
import com.example.styleshare.utils.Result
import kotlinx.coroutines.launch

class MyLooksViewModel : ViewModel() {

    private var looksRepo: LooksRepository? = null
    private val authRepo = AuthRepository()

    private val _state = MutableLiveData<Result<List<Look>>>()
    val state: LiveData<Result<List<Look>>> = _state

    /** init */
    fun init(context: Context) {
        if (looksRepo == null) looksRepo = LooksRepository(context.applicationContext)
    }

    /** טוען לוקים של המשתמש המחובר */
    fun loadMyLooks() {
        viewModelScope.launch {
            _state.value = Result.Loading
            try {
                val uid = authRepo.currentUid() ?: "guest"
                val list = looksRepo?.getMyLooks(uid) ?: emptyList()
                _state.value = Result.Success(list)
            } catch (e: Exception) {
                _state.value = Result.Error(e.message ?: "MyLooks failed")
            }
        }
    }
}
