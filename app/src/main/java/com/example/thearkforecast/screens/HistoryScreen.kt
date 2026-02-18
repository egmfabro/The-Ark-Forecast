package com.example.thearkforecast.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.thearkforecast.viewmodel.WeatherViewModel

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
            Text("Search History", style = MaterialTheme.typography.headlineMedium)
        }
        items(historyItems) { history ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = history.cityName, style = MaterialTheme.typography.titleLarge)
                    Text(text = "${history.temperature} - ${history.description}")
                    Text(text = history.dateTime, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}