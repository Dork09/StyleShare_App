/**
 * מטרת הקובץ:
 * ViewModel למסך פרטי לוק:
 * - טעינת לוק לפי id
 * - מועדפים
 * - מחיקה
 */
package com.example.styleshare.ui.details

import android.content.Context
import androidx.lifecycle.*
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.model.Look
import com.example.styleshare.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class LookDetailsViewModel : ViewModel() {

    private var looksRepo: LooksRepository? = null

    private val _lookState = MutableLiveData<Result<Look>>()
    val lookState: LiveData<Result<Look>> = _lookState

    /** אתחול repository פעם אחת */
    fun init(context: Context) {
        if (looksRepo == null) looksRepo = LooksRepository(context.applicationContext)
    }

    private val _commentsState = MutableLiveData<Result<List<com.example.styleshare.model.Comment>>>()
    val commentsState: LiveData<Result<List<com.example.styleshare.model.Comment>>> = _commentsState

    /** טעינת לוק לפי id */
    fun loadLook(lookId: String) {
        viewModelScope.launch {
            _lookState.value = Result.Loading
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                val look = looksRepo?.getLookById(lookId, uid)
                if (look != null) {
                    _lookState.value = Result.Success(look)
                    fetchComments(lookId)
                } else {
                    _lookState.value = Result.Error("Look not found")
                }
            } catch (e: Exception) {
                _lookState.value = Result.Error(e.message ?: "Failed loading look")
            }
        }
    }

    private fun fetchComments(lookId: String) {
        viewModelScope.launch {
            _commentsState.value = Result.Loading
            try {
                val comments = looksRepo?.getCommentsForLook(lookId) ?: emptyList()
                _commentsState.value = Result.Success(comments)
            } catch (e: Exception) {
                _commentsState.value = Result.Error(e.message ?: "Failed loading comments")
            }
        }
    }

    fun addComment(lookId: String, text: String, authorName: String = "Guest User") {
        viewModelScope.launch {
            try {
                looksRepo?.addComment(lookId, text, authorName)
                fetchComments(lookId)
                loadLook(lookId) // refresh comments count in Look if needed
            } catch (e: Exception) {
                _commentsState.value = Result.Error(e.message ?: "Failed adding comment")
            }
        }
    }

    /** מועדפים */
    fun toggleFavorite(lookId: String) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            looksRepo?.toggleFavorite(lookId, uid)
            loadLook(lookId)
        }
    }

    /** העלאת לייקים */
    fun incrementLike(lookId: String) {
        viewModelScope.launch {
            looksRepo?.incrementLike(lookId)
            loadLook(lookId)
        }
    }

    /** מחיקה */
    fun deleteLook(lookId: String) {
        viewModelScope.launch {
            looksRepo?.deleteLook(lookId)
        }
    }
}
