/**
 * מטרת הקובץ:
 * ViewModel למסך יצירת לוק:
 * - שומר לוק חדש ל-Room דרך LooksRepository
 */
package com.example.styleshare.ui.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.utils.Result
import kotlinx.coroutines.launch

class CreateLookViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LooksRepository(app.applicationContext)

    private val _state = MutableLiveData<Result<Unit>>()
    val state: LiveData<Result<Unit>> = _state


    /** שמירת לוק חדש ל-Room */
    fun saveLook(title: String, desc: String, imagePath: String, createdByUid: String, tags: List<String>) {
        viewModelScope.launch {
            _state.value = Result.Loading
            try {
                repo.createLook(
                    title = title,
                    description = desc,
                    imagePath = imagePath,
                    createdByUid = createdByUid,
                    tags=tags
                )
                _state.value = Result.Success(Unit)
            } catch (e: Exception) {
                _state.value = Result.Error(e.message ?: "שגיאה בשמירה")
            }
        }
    }
}
