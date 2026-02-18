package com.example.thearkforecast.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_history")
data class WeatherHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityName: String,
    val country: String,
    val temperature: String,
    val description: String,
    val sunrise: String,
    val sunset: String,
    val dateTime: String
)