package com.example.styleshare.data.remote.api

data class WeatherResponse(
    val current_weather: CurrentWeather
)

data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int
)
