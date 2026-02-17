package com.example.thearkforecast

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val _weatherData = MutableStateFlow<WeatherResponse?>(null)
    val weatherData: StateFlow<WeatherResponse?> = _weatherData
    private val weatherApi = WeatherApi.create()
    private val fusedLocationClient =
        com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(application)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading


    fun fetchWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = weatherApi.getWeather(city, apiKey)
                _weatherData.value = response
            } catch (e: Exception) {
                e.printStackTrace()
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
                        } catch (e: Exception) {
                            e.printStackTrace()
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