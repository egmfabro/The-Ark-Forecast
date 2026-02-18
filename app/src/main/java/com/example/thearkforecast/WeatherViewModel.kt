package com.example.thearkforecast

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData
    private val weatherApi = WeatherApi.create()
    private val fusedLocationClient =
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(application)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val db = AppDataBase.getDatabase(application)
    private val weatherDao = db.weatherDao()
    val historyList: StateFlow<List<WeatherHistory>> = weatherDao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun clearError() { _errorMessage.value = null }

    fun saveSearchToHistory(response: WeatherResponse) {
        val timestamp = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
            .format(java.util.Date())

        viewModelScope.launch {
            weatherDao.insert(
                WeatherHistory(
                    cityName = response.name,
                    temperature = "${response.main.temp.toInt()}°C",
                    description = response.weather[0].description,
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
            } catch (e: java.io.IOException) {
                _errorMessage.value =
                    "You are not connected to the internet. Please check your connection and try again."
            } catch (e: Exception) {
                _errorMessage.value = "An error occurred: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun fetchWeatherByLocation(apiKey: String) {
        _isLoading.value = true
        // 1. Use Priority.PRIORITY_HIGH_ACCURACY to force a fresh reading
        val priority = com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY

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
                        } catch (e: java.io.IOException) {
                            _errorMessage.value = "You are not connected to the internet. Please check your connection and try again."
                        } catch (e: Exception) {
                            _errorMessage.value = "An error occurred: ${e.localizedMessage}"
                        } finally {
                            _isLoading.value = false
                        }
                    }
                } else {
                    // If it's still null, Ortigas it is!
                    fetchWeather("Ortigas", apiKey)
                }
            }
            .addOnFailureListener {
                _isLoading.value = false
            }
    }
}