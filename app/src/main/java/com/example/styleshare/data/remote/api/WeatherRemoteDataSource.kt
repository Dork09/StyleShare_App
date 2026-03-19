package com.example.styleshare.data.remote.api

import com.example.styleshare.ui.home.HomeWeather
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRemoteDataSource {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApiService::class.java)

    suspend fun getCurrentWeather(lat: Double, lon: Double): HomeWeather {
        val response = api.getWeather(lat = lat, lon = lon)
        val current = response.current
        return HomeWeather(
            temperatureCelsius = current.temperature_2m,
            conditionLabel = weatherCodeToCondition(current.weather_code)
        )
    }

    private fun weatherCodeToCondition(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "clear"
            1 -> "mainly clear"
            2, 3, 45, 48 -> "cloudy"
            51, 53, 55, 56, 57 -> "drizzle"
            61, 63, 65, 66, 67, 80, 81, 82 -> "rain"
            71, 73, 75, 77, 85, 86 -> "snow"
            95, 96, 99 -> "thunderstorm"
            else -> "unknown"
        }
    }
}
