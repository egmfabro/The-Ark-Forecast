package com.example.thearkforecast.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.thearkforecast.data.WeatherApi
import com.example.thearkforecast.data.AppDataBase
import com.example.thearkforecast.data.WeatherHistory
import com.example.thearkforecast.data.WeatherResponse
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData
    private val weatherApi = WeatherApi.Companion.create()
    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val db = AppDataBase.Companion.getDatabase(application)
    private val weatherDao = db.weatherDao()
    val historyList: StateFlow<List<WeatherHistory>> = weatherDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(5000), emptyList())
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() {
        _errorMessage.value = null
    }

    fun saveSearchToHistory(response: WeatherResponse) {
        val timestamp = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
            .format(Date())

        viewModelScope.launch {
            weatherDao.insert(
                WeatherHistory(
                    cityName = response.name,
                    country = response.sys.country,
                    temperature = "${response.main.temp.toInt()}°C",
                    description = response.weather[0].description,
                    sunrise = formatTime(response.sys.sunrise, response.timezone),
                    sunset = formatTime(response.sys.sunset, response.timezone),
                    dateTime = timestamp
                )
            )
        }
    }


    fun fetchWeather(city: String, apiKey: String) {
        if (city.isBlank()) {
            _errorMessage.value = "Please enter a city name"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = weatherApi.getWeather(city, apiKey)
                _weatherData.value = response
                saveSearchToHistory(response)
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 404) {
                    _errorMessage.value = "City not found. Try searching for the parent city (e.g., Pasig or Mandaluyong)."
                } else {
                    _errorMessage.value = "Server error: ${e.message()}"
                }
            } catch (e: IOException) {
                _errorMessage.value = "Check your internet connection."
            } catch (e: Exception) {
                _errorMessage.value = "An unexpected error occurred."
            } finally {
                _isLoading.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchWeatherByLocation(apiKey: String) {
        _isLoading.value = true
        val priority = Priority.PRIORITY_HIGH_ACCURACY

        fusedLocationClient.getCurrentLocation(priority, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    viewModelScope.launch {
                        try {
                            val response = weatherApi.getWeatherByLocation(
                                location.latitude,
                                location.longitude,
                                apiKey
                            )
                            _weatherData.value = response
                        } catch (e: IOException) {
                            _errorMessage.value =
                                "You are not connected to the internet. Please check your connection and try again."
                        } catch (e: Exception) {
                            _errorMessage.value = "An error occurred: ${e.localizedMessage}"
                        } finally {
                            _isLoading.value = false
                        }
                    }
                } else {
                    fetchWeather("Ortigas", apiKey)
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }

    fun formatTime(timestamp: Long, timezoneOffset: Int): String {
        val date = Date(timestamp * 1000L)
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val tz = java.util.TimeZone.getTimeZone("UTC")
        val offsetInMillis = timezoneOffset * 1000

        sdf.timeZone = java.util.SimpleTimeZone(offsetInMillis, "CustomCityTime")
        return sdf.format(date)
    }

    fun isNightTime(): Boolean {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return hour >= 18 || hour < 6
    }
}