package com.example.thearkforecast.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thearkforecast.R
import com.example.thearkforecast.viewmodel.WeatherViewModel
import com.example.thearkforecast.components.WeatherCard
import com.example.thearkforecast.data.Constants


@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var city by remember { mutableStateOf("") }
    val errorMessage by viewModel.errorMessage.collectAsState()
    val apiKey = Constants.apiKey

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            viewModel.fetchWeatherByLocation(apiKey)
        }
    }

    // Logic for Dynamic Background and Icons
    val weatherMain = weatherData?.weather?.firstOrNull()?.main ?: ""
    val currentTime = System.currentTimeMillis() / 1000
    val sunrise = weatherData?.sys?.sunrise ?: 0
    val sunset = weatherData?.sys?.sunset ?: 0
    val isNight = if (sunrise != 0L && sunset != 0L) {
        currentTime < sunrise || currentTime > sunset
    } else {
        viewModel.isNightTime()
    }
    val backgroundResource = if (isNight) R.drawable.weather_night else R.drawable.weather_day
    // Determine which custom drawable to use
    val weatherIconRes = when {
        // 1. Any hint of rain takes priority
        weatherMain.contains("Rain", ignoreCase = true) -> R.drawable.icon_weather_rainy

        // 2. Night Time Logic
        isNight -> {
            if (weatherMain.contains("Cloud", ignoreCase = true)) {
                R.drawable.icon_weather_night_cloudy // Custom Night Cloudy
            } else {
                // Your default Clear Night icon (e.g., a moon)
                Icons.Default.WbTwilight
            }
        }

        // 3. Day Time Logic
        else -> {
            if (weatherMain.contains("Cloud", ignoreCase = true)) {
                R.drawable.icon_weather_sunny_cloudy // Custom Sunny Cloudy
            } else {
                // Your default Clear Sunny icon (e.g., a sun)
                Icons.Default.WbSunny
            }
        }
    }


    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Connection Error") },
            text = { Text(errorMessage!!) },
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) { Text("OK") }
            }
        )
    }

    LaunchedEffect(Unit) {
        if (viewModel.weatherData.value == null) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(backgroundResource),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Search Bar
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("Search City...", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.fetchWeather(
                        city,
                        apiKey
                    )
                }),
                trailingIcon = {
                    IconButton(onClick = { viewModel.fetchWeather(city, apiKey) }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    // Container Tint
                    focusedContainerColor = if (isNight) Color.White.copy(alpha = 0.15f) else Color.Black.copy(
                        alpha = 0.05f
                    ),
                    unfocusedContainerColor = if (isNight) Color.White.copy(alpha = 0.1f) else Color.Black.copy(
                        alpha = 0.05f
                    ),
                    // Border/Indicator
                    focusedIndicatorColor = Color.White.copy(alpha = 0.4f),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                    // Text Color
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    // Label/Hint Color
                    focusedLabelColor = Color.White.copy(alpha = 0.7f),
                    unfocusedLabelColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                weatherData?.let { data ->
                    // City and Country Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp, bottom = 16.dp, start = 8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "${data.name}, ${data.sys.country}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = "${data.main.temp.toInt()}°",
                            fontSize = 130.sp,
                            fontWeight = FontWeight.Light,
                            color = Color.White,
                            letterSpacing = (-10).sp,
                            modifier = Modifier.offset(y = (-10).dp)
                        )
                        Text(
                            text = data.weather[0].description.replaceFirstChar { it.uppercase() },
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.offset(y = (-20).dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Weather Cards Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherCard(
                                label = "Weather",
                                value = weatherMain,
                                icon = weatherIconRes,
                                isNight = isNight
                            )
                            WeatherCard(
                                label = "Humidity",
                                value = "${data.main.humidity}%",
                                icon = Icons.Default.Opacity,
                                isNight = isNight
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherCard(
                                label = "Sunrise",
                                value = viewModel.formatTime(data.sys.sunrise,data.timezone),
                                icon = Icons.Default.WbTwilight,
                                isNight = isNight
                            )
                            WeatherCard(
                                label = "Sunset",
                                value = viewModel.formatTime(data.sys.sunset, data.timezone),
                                icon = R.drawable.icon_sunset,
                                isNight = isNight
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            WeatherCard(
                                label = "Pressure",
                                value = "${data.main.pressure} hPa",
                                icon = Icons.Default.Compress,
                                isNight = isNight
                            )
                            WeatherCard(
                                label = "Wind Speed",
                                value = "${data.wind.speed.toInt()} km/h",
                                icon = Icons.Default.Air,
                                isNight = isNight
                            )
                        }
                    }
                }
            }
        }
    }
}