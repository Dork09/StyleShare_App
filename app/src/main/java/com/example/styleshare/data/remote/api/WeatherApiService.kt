/**
 * מטרת הקובץ:
 * Retrofit interface לקריאה ל-OpenMeteo API
 */
package com.example.styleshare.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    /** מביא מזג אוויר לפי lat/lon */
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): WeatherResponse
}
