package com.example.styleshare.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.styleshare.data.repository.UserProfileRepository
import com.example.styleshare.model.Look
import com.example.styleshare.utils.Result
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HomeRepository(app.applicationContext)
    private val profileRepo = UserProfileRepository(app.applicationContext)
    private val recommendationEngine = WeatherRecommendationEngine()

    private val _feedState = MutableLiveData<Result<List<Look>>>()
    val feedState: LiveData<Result<List<Look>>> = _feedState

    private val _weatherLocation = MutableLiveData(
        app.applicationContext.getString(com.example.styleshare.R.string.home_weather_loading)
    )
    val weatherLocation: LiveData<String> = _weatherLocation

    private val _weatherTemp = MutableLiveData<String>()
    val weatherTemp: LiveData<String> = _weatherTemp

    private val _greeting = MutableLiveData<String>()
    val greeting: LiveData<String> = _greeting

    private val _recommended = MutableLiveData<List<Look>>()
    val recommended: LiveData<List<Look>> = _recommended

    private var currentWeather: HomeWeather? = null

    init {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            val uid = user?.uid ?: "guest"
            val email = user?.email ?: "User"
            val displayName = user?.displayName

            val profile = profileRepo.getProfile(uid)
            val name = profile?.fullName?.takeIf { it.isNotBlank() && it != "משתמש/ת חדש/ה" }
                ?: displayName?.takeIf { it.isNotBlank() }
                ?: email.substringBefore("@")

            _greeting.value = getApplication<Application>()
                .getString(com.example.styleshare.R.string.home_greeting, name)
        }
    }

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

    fun toggleFavorite(lookId: String) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
            repo.toggleFavorite(lookId, uid)
            loadFeed()
        }
    }

    fun incrementLike(lookId: String) {
        viewModelScope.launch {
            repo.incrementLike(lookId)
            loadFeed()
        }
    }

    fun loadWeather(lat: Double, lon: Double, city: String) {
        viewModelScope.launch {
            try {
                val weather = repo.getCurrentWeather(lat, lon)
                currentWeather = weather
                _weatherLocation.value = getApplication<Application>()
                    .getString(com.example.styleshare.R.string.home_weather_location, city)
                _weatherTemp.value = getApplication<Application>()
                    .getString(
                        com.example.styleshare.R.string.home_weather_temp,
                        weather.temperatureCelsius.toInt().toString()
                    )
                updateRecommendations((feedState.value as? Result.Success)?.data ?: emptyList())
            } catch (e: Exception) {
                android.util.Log.e("WeatherError", "Failed to load weather: ${e.message}", e)
                currentWeather = null
                _weatherLocation.value = getApplication<Application>()
                    .getString(com.example.styleshare.R.string.home_weather_error)
                _weatherTemp.value = ""
                updateRecommendations((feedState.value as? Result.Success)?.data ?: emptyList())
            }
        }
    }

    private fun updateRecommendations(allLooks: List<Look>) {
        _recommended.value = recommendationEngine.recommend(
            looks = allLooks,
            weather = currentWeather,
            limit = 5
        )
    }
}
