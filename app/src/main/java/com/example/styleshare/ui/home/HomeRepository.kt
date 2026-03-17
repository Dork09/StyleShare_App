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

    suspend fun toggleFavorite(lookId: String, currentUid: String) =
        looksRepo.toggleFavorite(lookId, currentUid)

    suspend fun incrementLike(lookId: String) = looksRepo.incrementLike(lookId)

    suspend fun getTemperature(lat: Double, lon: Double): Double =
        weatherRepo.getTemperature(lat, lon)
}
