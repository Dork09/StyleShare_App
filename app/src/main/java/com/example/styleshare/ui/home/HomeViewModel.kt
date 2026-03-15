/**
 * מטרת הקובץ:
 * ViewModel למסך Home:
 * - טעינת פיד
 * - שינוי מועדפים
 * - טעינת מזג אוויר
 */
package com.example.styleshare.ui.home

import android.app.Application
import androidx.lifecycle.*
import com.example.styleshare.data.repository.UserProfileRepository
import com.example.styleshare.model.Look
import com.example.styleshare.utils.Result
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HomeRepository(app.applicationContext)
    private val profileRepo = UserProfileRepository(app.applicationContext)

    private val _feedState = MutableLiveData<Result<List<Look>>>()
    val feedState: LiveData<Result<List<Look>>> = _feedState

    private val _weatherLocation = MutableLiveData(app.applicationContext.getString(com.example.styleshare.R.string.home_weather_loading))
    val weatherLocation: LiveData<String> = _weatherLocation

    private val _weatherTemp = MutableLiveData<String>()
    val weatherTemp: LiveData<String> = _weatherTemp

    /** טוען פיד */
    fun loadFeed() {
        viewModelScope.launch {
            _feedState.value = Result.Loading
            try {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                val looks = repo.getFeed(uid)
                _feedState.value = Result.Success(looks)
                updateRecommendations(looks)
            } catch (e: Exception) {
                _feedState.value = Result.Error(e.message ?: "Failed loading feed")
            }
        }
    }

    /** מועדפים */
    fun toggleFavorite(lookId: String) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            repo.toggleFavorite(lookId, uid)
            loadFeed()
        }
    }

    /** העלאת לייקים */
    fun incrementLike(lookId: String) {
        viewModelScope.launch {
            repo.incrementLike(lookId)
            loadFeed()
        }
    }

    /** מזג אוויר כולל שם מיקום */
    fun loadWeather(lat: Double, lon: Double, city: String) {
        viewModelScope.launch {
            try {
                val temp = repo.getTemperature(lat, lon)
                _weatherLocation.value = getApplication<Application>().getString(com.example.styleshare.R.string.home_weather_location, city)
                _weatherTemp.value = getApplication<Application>().getString(com.example.styleshare.R.string.home_weather_temp, temp.toInt().toString())
                lastTemp = temp
                updateRecommendations((feedState.value as? Result.Success)?.data ?: emptyList())
            } catch (e: Exception) {
                // --- Added: Log the exact error to logcat to see why Retrofit might be failing ---
                android.util.Log.e("WeatherError", "Failed to load weather: ${e.message}", e)
                _weatherLocation.value = getApplication<Application>().getString(com.example.styleshare.R.string.home_weather_error)
                _weatherTemp.value = ""
            }
        }
    }


    private val _greeting = MutableLiveData<String>()
    val greeting: LiveData<String> = _greeting

    init {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val uid = user?.uid ?: "guest"
            val email = user?.email ?: "User"
            val displayName = user?.displayName
            
            // Try Room first, then Firebase displayName, then email prefix
            val profile = profileRepo.getProfile(uid)
            val name = profile?.fullName?.takeIf { it.isNotBlank() && it != "משתמש/ת חדש/ה" }
                ?: displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")
            
            _greeting.value = getApplication<Application>().getString(com.example.styleshare.R.string.home_greeting, name)
        }
    }
    private val _recommended = MutableLiveData<List<Look>>()
    val recommended: LiveData<List<Look>> = _recommended

    private var lastTemp: Double? = null

    /** יוצר רשימת מומלצים לפי טמפרטורה */
    private fun updateRecommendations(all: List<Look>) {
        val temp = lastTemp ?: run {
            _recommended.value = all.take(5)
            return
        }

        val winterWords = listOf("מעיל", "ג׳קט", "סוודר", "חורף", "ארוך","קר", "קריר","קפוא","שלג","גשם","סקי")
        val summerWords = listOf("קיץ", "שמלה", "שורט", "קצר", "סנדל", "טישרט", "חם", "רותח")

        val filtered = when {
            temp <= 18 -> all.filter { l ->
                (l.title + " " + l.description).containsAny(winterWords)
            }
            temp >= 26 -> all.filter { l ->
                (l.title + " " + l.description).containsAny(summerWords)
            }
            else -> all.shuffled()
        }

        _recommended.value = (filtered.ifEmpty { all }).take(5)
    }

    /** עוזר: בדיקת התאמה למילים */
    private fun String.containsAny(words: List<String>): Boolean {
        return words.any { this.contains(it, ignoreCase = true) }
    }



}
