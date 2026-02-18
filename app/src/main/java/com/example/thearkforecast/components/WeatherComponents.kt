package com.example.thearkforecast.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherCard(label: String, value: String, icon: Any, isNight: Boolean) {
    val containerColor =
        if (isNight) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)

    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                when (icon) {
                    is Int -> Icon(painter = painterResource(id = icon), contentDescription = null, tint = Color.White)
                    is ImageVector -> Icon(imageVector = icon, contentDescription = null, tint = Color.White)
                }
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }
    }
}