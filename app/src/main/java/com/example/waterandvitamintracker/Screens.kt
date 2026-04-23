package com.example.waterandvitamintracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.waterandvitamintracker.AppViewModel
import com.example.waterandvitamintracker.security.AuthState

@Composable
fun LoginScreen(viewModel: AppViewModel, onLoginSuccess: () -> Unit) {
    val authState by viewModel.biometricManager.authState.collectAsState()
    val context = LocalContext.current as FragmentActivity
    val isBiometricsEnabled = viewModel.biometricManager.isEnabledByUser()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState == AuthState.Success) {
            onLoginSuccess()
            viewModel.biometricManager.resetState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Water & Vitamin Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; showError = false },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; showError = false },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (viewModel.loginWithPassword(username, password)) {
                    onLoginSuccess()
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        if (showError) {
            Text(text = "Invalid credentials. Use admin / 1234", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        if (isBiometricsEnabled) {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.authenticateUser(context, "Sign In") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Login with Biometrics")
            }
            if (authState == AuthState.Failed) {
                Text(text = "Authentication failed.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
            if (authState == AuthState.Unavailable) {
                Text(text = "Sensor unavailable.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
fun LockScreen(viewModel: AppViewModel) {
    val authState by viewModel.biometricManager.authState.collectAsState()
    val context = LocalContext.current as FragmentActivity

    LaunchedEffect(authState) {
        if (authState == AuthState.Success) {
            viewModel.unlockApp()
            viewModel.biometricManager.resetState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, contentDescription = "Locked", modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("App Locked", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = { viewModel.authenticateUser(context, "Unlock Application") }) {
            Text("Unlock with Biometrics")
        }
    }
}

@Composable
fun ProfileScreen(viewModel: AppViewModel) {
    var isEnabled by remember { mutableStateOf(viewModel.biometricManager.isEnabledByUser()) }
    var timeout by remember { mutableStateOf(viewModel.biometricManager.getLockTimeoutSeconds()) }
    val sensorType = viewModel.biometricManager.checkAvailability()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "User Profile", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Name: Admin", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(32.dp))
        Text(text = "Security Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Biometric Login", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Supported sensor: ${sensorType.name}", style = MaterialTheme.typography.bodySmall)
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = { checked ->
                    isEnabled = checked
                    viewModel.biometricManager.setEnabledByUser(checked)
                }
            )
        }

        if (isEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Lock app in background after:", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                listOf(0L, 5L, 15L).forEach { seconds ->
                    FilterChip(
                        selected = timeout == seconds,
                        onClick = {
                            timeout = seconds
                            viewModel.biometricManager.setLockTimeoutSeconds(seconds)
                        },
                        label = { Text("${seconds}s") }
                    )
                }
            }
        }
    }
}

@Composable
fun VitaminListScreen(viewModel: AppViewModel, onVitaminClick: (Int) -> Unit) {
    val vitamins by viewModel.vitamins.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var dosage by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Add New Vitamin") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Vitamin Name (e.g. Omega-3)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage (mg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val dsg = dosage.toIntOrNull() ?: 0
                    if (name.isNotBlank()) viewModel.addCustomVitamin(name, dsg)
                    showDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add") }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues), contentPadding = PaddingValues(16.dp)) {
            item {
                Text(text = "My Vitamins", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))
            }
            items(vitamins) { vitamin ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onVitaminClick(vitamin.id) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Vitamin: ${vitamin.name}", style = MaterialTheme.typography.titleLarge)
                            Text(text = "Dosage: ${vitamin.dailyDosageMg} mg", style = MaterialTheme.typography.bodyMedium)
                        }
                        Checkbox(
                            checked = vitamin.isTaken,
                            onCheckedChange = { viewModel.toggleVitaminTaken(vitamin) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VitaminDetailScreen(vitaminId: Int, viewModel: AppViewModel, onBackClick: () -> Unit) {
    val vitamin by viewModel.getVitaminDetails(vitaminId).collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBackClick, modifier = Modifier.padding(bottom = 16.dp)) { Text("Back to List") }
        if (vitamin != null) {
            Text(text = vitamin!!.name, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Daily Dosage: ${vitamin!!.dailyDosageMg} mg", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Status: ${if (vitamin!!.isTaken) "Taken" else "Not Taken"}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { viewModel.deleteVitamin(vitamin!!); onBackClick() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Record")
            }
        } else {
            Text("Vitamin not found")
        }
    }
}

@Composable
fun WaterScreen(viewModel: AppViewModel) {
    val records by viewModel.waterRecords.collectAsState()
    val totalWater by viewModel.totalWater.collectAsState()
    val goal by viewModel.waterGoal.collectAsState()

    var showGoalDialog by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableFloatStateOf(150f) }

    val isGoalReached = totalWater >= goal && goal > 0
    val progress = if (goal > 0) (totalWater.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val progressColor = if (isGoalReached) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

    if (showGoalDialog) {
        var newGoalStr by remember { mutableStateOf(goal.toString()) }
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text("Set Daily Goal (ml)") },
            text = {
                OutlinedTextField(
                    value = newGoalStr,
                    onValueChange = { newGoalStr = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    newGoalStr.toIntOrNull()?.let { viewModel.updateWaterGoal(it) }
                    showGoalDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Water Intake", style = MaterialTheme.typography.headlineMedium)
                TextButton(onClick = { showGoalDialog = true }) {
                    Text("Goal: $goal ml")
                }
            }
        }

        item {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 12.dp,
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "$totalWater", style = MaterialTheme.typography.displaySmall)
                    Text(text = "/ $goal ml", style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (isGoalReached) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Daily Goal Reached! 🎉", color = Color(0xFF4CAF50), style = MaterialTheme.typography.titleLarge)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = { viewModel.addCustomWater(100) }) { Text("+ 100 ml") }
                Button(onClick = { viewModel.addCustomWater(250) }) { Text("+ 250 ml") }
                Button(onClick = { viewModel.addCustomWater(500) }) { Text("+ 500 ml") }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Text(text = "Custom amount: ${sliderValue.toInt()} ml", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 50f..1000f,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Button(onClick = { viewModel.addCustomWater(sliderValue.toInt()) }) {
                Text("Add ${sliderValue.toInt()} ml")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text("Today's Records", style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(records.reversed()) { record ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "+ ${record.amountMl} ml", style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { viewModel.deleteWaterRecord(record) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun StatsScreen(viewModel: AppViewModel) {
    val socketState by viewModel.socketState.collectAsState()
    val messages by viewModel.wsMessages.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Live Stats", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Connection: $socketState", style = MaterialTheme.typography.bodyLarge)
        Button(onClick = { viewModel.forceReconnect() }, modifier = Modifier.padding(top = 8.dp)) { Text("Simulate Connection Drop") }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(messages.reversed()) { msg ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(text = msg.type, style = MaterialTheme.typography.titleMedium)
                        Text(text = msg.text, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}