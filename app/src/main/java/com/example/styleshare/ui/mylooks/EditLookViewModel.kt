package com.example.styleshare.ui.mylooks

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.model.Look
import com.example.styleshare.utils.Result
import com.example.styleshare.utils.uploadImageToCloudinary
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class EditLookViewModel(app: Application) : AndroidViewModel(app) {

    private val looksRepo = LooksRepository(app.applicationContext)

    private val _lookState = MutableLiveData<Result<Look>>()
    val lookState: LiveData<Result<Look>> = _lookState

    private val _saveState = MutableLiveData<Result<Unit>>()
    val saveState: LiveData<Result<Unit>> = _saveState

    fun loadLook(lookId: String) {
        viewModelScope.launch {
            _lookState.value = Result.Loading
            try {
                val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val look = looksRepo.getLookById(lookId, currentUid)
                if (look != null) _lookState.value = Result.Success(look)
                else _lookState.value = Result.Error("Look not found")
            } catch (e: Exception) {
                _lookState.value = Result.Error(e.message ?: "Load failed")
            }
        }
    }

    /**
     * [newImageUri] — non-null when the user picked a new image; null to keep the existing one.
     * [currentImageUrl] — the Cloudinary URL already stored for this look.
     */
    fun updateLook(
        lookId: String,
        title: String,
        desc: String,
        newImageUri: Uri?,
        currentImageUrl: String?,
        tags: List<String>
    ) {
        viewModelScope.launch {
            _saveState.value = Result.Loading
            try {
                val finalImageUrl = if (newImageUri != null) {
                    uploadImageToCloudinary(getApplication(), newImageUri)
                } else {
                    currentImageUrl.orEmpty()
                }
                looksRepo.updateLook(lookId, title, desc, finalImageUrl, tags)
                _saveState.value = Result.Success(Unit)
            } catch (e: Exception) {
                _saveState.value = Result.Error(e.message ?: "Update failed")
            }
        }
    }

    fun deleteLook(lookId: String) {
        viewModelScope.launch {
            looksRepo.deleteLook(lookId)
        }
    }
}
