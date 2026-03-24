package com.example.styleshare.ui.home

import android.content.Context
import com.example.styleshare.data.repository.LooksRepository
import com.example.styleshare.data.repository.WeatherRepository
import com.example.styleshare.model.Look

class HomeRepository(context: Context) {

    private val looksRepo = LooksRepository(context)
    private val weatherRepo = WeatherRepository()

    suspend fun getFeed(currentUid: String): List<Look> {
        return looksRepo.getFeed(currentUid)
    }

    suspend fun refreshFeedFromRemote(force: Boolean = false) {
        looksRepo.refreshLooksFromRemote(force)
    }

    suspend fun toggleFavorite(lookId: String, currentUid: String) =
        looksRepo.toggleFavorite(lookId, currentUid)

    suspend fun toggleLike(lookId: String, currentUid: String) = looksRepo.toggleLike(lookId, currentUid)

    suspend fun getCurrentWeather(lat: Double, lon: Double): HomeWeather =
        weatherRepo.getCurrentWeather(lat, lon)
}
