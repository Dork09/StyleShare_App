package com.example.styleshare.data.repository

import com.example.styleshare.data.remote.api.WeatherRemoteDataSource
import com.example.styleshare.ui.home.HomeWeather

class WeatherRepository(
    private val remote: WeatherRemoteDataSource = WeatherRemoteDataSource()
) {

    suspend fun getCurrentWeather(lat: Double, lon: Double): HomeWeather {
        return remote.getCurrentWeather(lat, lon)
    }
}
