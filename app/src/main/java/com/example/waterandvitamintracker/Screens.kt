package com.example.waterandvitamintracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.waterandvitamintracker.AppViewModel

@Composable
fun VitaminListScreen(
    viewModel: AppViewModel,
    onVitaminClick: (Int) -> Unit
) {
    val vitamins by viewModel.vitamins.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addRandomVitamin() }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(vitamins) { vitamin ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onVitaminClick(vitamin.id) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = vitamin.name, style = MaterialTheme.typography.titleLarge)
                        Text(text = "Category: ${vitamin.category}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Status: ${vitamin.syncStatus}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
fun VitaminDetailScreen(
    vitaminId: Int,
    viewModel: AppViewModel,
    onBackClick: () -> Unit
) {
    val vitamin by viewModel.getVitaminDetails(vitaminId).collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Button(onClick = onBackClick, modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Back to List")
        }

        if (vitamin != null) {
            Text(text = vitamin!!.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Daily Dosage: ${vitamin!!.dailyDosageMg} mg", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Essential: ${if (vitamin!!.isEssential) "Yes" else "No"}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Category: ${vitamin!!.category}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Sync Status: ${vitamin!!.syncStatus}", style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.deleteVitamin(vitamin!!)
                    onBackClick()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Record")
            }
        } else {
            Text("Vitamin not found or deleted")
        }
    }
}

@Composable
fun WaterScreen(viewModel: AppViewModel) {
    val records by viewModel.waterRecords.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.addWaterRecord() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Water")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(records) { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "+ ${record.amountMl} ml", style = MaterialTheme.typography.titleLarge)
                            Text(text = "Sparkling: ${if (record.isSparkling) "Yes" else "No"}", style = MaterialTheme.typography.bodyMedium)
                            Text(text = "Status: ${record.syncStatus}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.deleteWaterRecord(record) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }
        }
    }
}