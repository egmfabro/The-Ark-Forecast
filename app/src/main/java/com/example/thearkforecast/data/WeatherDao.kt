package com.example.thearkforecast.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.thearkforecast.data.WeatherHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert
    suspend fun insert(history: WeatherHistory)

    @Query("SELECT * FROM weather_history ORDER BY id DESC")
    fun getAllHistory(): Flow<List<WeatherHistory>>

    @Query("DELETE FROM weather_history")
    suspend fun clearHistory(): Unit
}