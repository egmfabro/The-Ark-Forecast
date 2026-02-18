package com.example.thearkforecast.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.thearkforecast.data.WeatherDao
import com.example.thearkforecast.data.WeatherHistory

@Database(entities = [WeatherHistory::class], version = 2)
abstract class AppDataBase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile
        private var INSTANCE: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "ark_weather_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}