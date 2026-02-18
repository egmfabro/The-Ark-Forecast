package com.example.thearkforecast.data

data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val sys: Sys,
    val timezone: Int,
    val wind: Wind
)


data class Main(
    val temp: Float,
    val pressure: Int,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double,
    val feels_like: Double
)

data class Weather(
    val main: String,
    val description: String
)

data class Sys(
    val country: String,
    val sunrise: Long,
    val sunset: Long
)

data class Wind(
    val speed: Double,
    val deg: Int
)