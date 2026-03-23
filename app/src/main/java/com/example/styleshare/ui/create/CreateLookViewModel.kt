package com.example.styleshare.ui.create

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.utils.Result
import com.example.styleshare.utils.uploadImageToCloudinary
import kotlinx.coroutines.launch

class CreateLookViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = LooksRepository(app.applicationContext)

    private val _state = MutableLiveData<Result<Unit>>()
    val state: LiveData<Result<Unit>> = _state

    fun saveLook(title: String, desc: String, imageUri: Uri, createdByUid: String, tags: List<String>) {
        viewModelScope.launch {
            _state.value = Result.Loading
            try {
                val imageUrl = uploadImageToCloudinary(getApplication(), imageUri)
                repo.createLook(
                    title = title,
                    description = desc,
                    imageUrl = imageUrl,
                    createdByUid = createdByUid,
                    tags = tags
                )
                _state.value = Result.Success(Unit)
            } catch (e: Exception) {
                _state.value = Result.Error(e.message ?: "שגיאה בשמירה")
            }
        }
    }
}
