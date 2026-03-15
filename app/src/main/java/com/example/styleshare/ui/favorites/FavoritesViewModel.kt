/**
 * מטרת הקובץ:
 * ViewModel למסך מועדפים:
 * - טעינת לוקים מועדפים מ-Room
 */
package com.example.styleshare.ui.favorites

import android.content.Context
import androidx.lifecycle.*
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.model.Look
import com.example.styleshare.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class FavoritesViewModel : ViewModel() {

    private var looksRepo: LooksRepository? = null

    private val _state = MutableLiveData<Result<List<Look>>>()
    val state: LiveData<Result<List<Look>>> = _state

    /** init */
    fun init(context: Context) {
        if (looksRepo == null) looksRepo = LooksRepository(context.applicationContext)
    }

    /** טעינת מועדפים */
    fun loadFavorites() {
        viewModelScope.launch {
            _state.value = Result.Loading
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                val list = looksRepo?.getFavorites(uid) ?: emptyList()
                _state.value = Result.Success(list)
            } catch (e: Exception) {
                _state.value = Result.Error(e.message ?: "Favorites failed")
            }
        }
    }
}
