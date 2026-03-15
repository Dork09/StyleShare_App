/**
 * מטרת הקובץ:
 * WeatherRemoteDataSource:
 * אחראי לקריאות REST ל-Open-Meteo (ללא API KEY)
 */
package com.example.styleshare.data.remote.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherRemoteDataSource {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(WeatherApiService::class.java)

    /** מחזיר טמפרטורה לפי מיקום */
    suspend fun getTemperature(lat: Double, lon: Double): Double {
        val response = api.getWeather(lat = lat, lon = lon)
        return response.current_weather.temperature
    }
}
