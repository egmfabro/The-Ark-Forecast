package com.example.thearkforecast

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [WeatherHistory::class], version = 1)
abstract class AppDataBase : RoomDatabase(){
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