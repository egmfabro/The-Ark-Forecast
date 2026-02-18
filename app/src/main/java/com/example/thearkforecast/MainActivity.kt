package com.example.thearkforecast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.ActivityNavigatorExtras
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.thearkforecast.ui.theme.Blue
import com.example.thearkforecast.ui.theme.DarkBlue
import com.example.thearkforecast.ui.theme.TheArkForecastTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TheArkForecastTheme {
                MainScreen()
            }
        }
    }
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Weather", Icons.Default.Home)
    object History: Screen("history", "History", Icons.Default.DateRange)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val sharedViewModel: WeatherViewModel = viewModel()


    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = DarkBlue
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(Screen.Home, Screen.History)

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                WeatherScreen(sharedViewModel)
            }
            composable(Screen.History.route) {
                HistoryScreen(sharedViewModel)
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: WeatherViewModel) {
    val historyItems by viewModel.historyList.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Search History",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        items(historyItems) { history ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = history.cityName,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${history.temperature} - ${history.description}"
                    )
                    Text(
                        text = history.dateTime,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}


@Composable
fun WeatherScreen(viewModel: WeatherViewModel) {
    val weatherData by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var city by remember {
        mutableStateOf("")
    }
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

    if (errorMessage != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Connection Error") },
            text = { Text(errorMessage!!)},
            confirmButton = {
                Button(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }


    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(R.drawable.weather_rainy),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(
                modifier = Modifier
                    .height(90.dp)
            )
            OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Blue,
                    unfocusedIndicatorColor = Blue,
                    focusedLabelColor = DarkBlue
                )
            )
            Spacer(
                modifier = Modifier
                    .height(16.dp)
            )
            Button(
                onClick = { viewModel.fetchWeather(city, apiKey) },
                colors = ButtonDefaults.buttonColors(Blue)
            ) {
                Text(text = "Check Weather")
            }
            Spacer(
                modifier = Modifier
                    .height(16.dp)
            )
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(color = Color.White)
                }
            } else {
                weatherData?.let {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherCard(label = "City", value = it.name, icon = Icons.Default.Place)
                        WeatherCard(
                            label = "Temperature",
                            value = "${it.main.temp}°C",
                            icon = Icons.Default.Star
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherCard(
                            label = "Humidity",
                            value = "${it.main.humidity}%",
                            icon = Icons.Default.Warning
                        )
                        WeatherCard(
                            label = "Description",
                            value = it.weather[0].description,
                            icon = Icons.Default.Info
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun WeatherCard(label: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkBlue,
                    modifier = Modifier
                        .size(20.dp)
                )
                Spacer(
                    modifier = Modifier
                        .width(4.dp)
                )
                Text(text = label, fontSize = 14.sp, color = DarkBlue)
            }
            Spacer(
                modifier = Modifier
                    .height(8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Blue
                )
            }
        }
    }
}
