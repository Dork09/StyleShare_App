/**
 * מטרת הקובץ:
 * Repository למזג אוויר דרך REST API (Retrofit).
 */
package com.example.styleshare.data.repository

import com.example.styleshare.data.remote.api.WeatherRemoteDataSource

class WeatherRepository(
    private val remote: WeatherRemoteDataSource = WeatherRemoteDataSource()
) {

    /** מחזיר טמפרטורה לפי מיקום */
    suspend fun getTemperature(lat: Double, lon: Double): Double {
        return remote.getTemperature(lat, lon)
    }
}
