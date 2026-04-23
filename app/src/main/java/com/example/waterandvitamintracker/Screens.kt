package com.example.waterandvitamintracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.waterandvitamintracker.data.MockData

@Composable
fun VitaminListScreen(onVitaminClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(MockData.vitamins) { vitamin ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onVitaminClick(vitamin.id) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = vitamin.name, style = MaterialTheme.typography.titleLarge)
                    Text(text = "Category: ${vitamin.category}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun VitaminDetailScreen(vitaminId: Int, onBackClick: () -> Unit) {
    val vitamin = MockData.vitamins.find { it.id == vitaminId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onBackClick, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Back to List")
        }
        if (vitamin != null) {
            Text(text = vitamin.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Daily Dosage: ${vitamin.dailyDosageMg} mg", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Essential: ${if (vitamin.isEssential) "Yes" else "No"}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Category: ${vitamin.category}", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text("Vitamin not found")
        }
    }
}

@Composable
fun WaterScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Water Intake Screen", style = MaterialTheme.typography.headlineMedium)
    }
}