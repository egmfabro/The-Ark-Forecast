package com.example.thearkforecast.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Weather", Icons.Default.Home)
    object History : Screen("history", "History", Icons.Default.DateRange)
}