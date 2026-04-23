package com.example.waterandvitamintracker

import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterandvitamintracker.data.AppRepository
import com.example.waterandvitamintracker.models.Vitamin
import com.example.waterandvitamintracker.models.WaterRecord
import com.example.waterandvitamintracker.models.WsMessage
import com.example.waterandvitamintracker.network.MockSocketManager
import com.example.waterandvitamintracker.network.SocketState
import com.example.waterandvitamintracker.security.AppBiometricManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class AppViewModel(
    private val repository: AppRepository,
    val biometricManager: AppBiometricManager,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val socketManager = MockSocketManager()
    val socketState: StateFlow<SocketState> = socketManager.socketState
    val wsMessages = MutableStateFlow<List<WsMessage>>(emptyList())

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked

    private val _waterGoal = MutableStateFlow(prefs.getInt("water_goal", 2000))
    val waterGoal: StateFlow<Int> = _waterGoal

    val vitamins: StateFlow<List<Vitamin>> = repository.allVitamins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterRecords: StateFlow<List<WaterRecord>> = repository.allWaterRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalWater: StateFlow<Int> = repository.allWaterRecords
        .map { records -> records.sumOf { it.amountMl } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        socketManager.onMessage { msg ->
            wsMessages.value = wsMessages.value + msg
        }
    }

    fun updateWaterGoal(newGoal: Int) {
        _waterGoal.value = newGoal
        prefs.edit().putInt("water_goal", newGoal).apply()
    }

    fun lockApp() {
        _isAppLocked.value = true
    }

    fun unlockApp() {
        _isAppLocked.value = false
    }

    fun loginWithPassword(user: String, pass: String): Boolean {
        return user == "admin" && pass == "1234"
    }

    fun connectWs() {
        socketManager.connect("wss://mock.server")
    }

    fun disconnectWs() {
        socketManager.disconnect()
    }

    fun forceReconnect() {
        socketManager.simulateDisconnectForReconnect()
    }

    fun getVitaminDetails(id: Int): StateFlow<Vitamin?> {
        return repository.getVitaminById(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun addCustomVitamin(name: String, dosage: Int) {
        viewModelScope.launch {
            try {
                val newVitamin = Vitamin(
                    name = name,
                    dailyDosageMg = dosage,
                    isEssential = true,
                    category = "Custom",
                    isTaken = false
                )
                repository.addVitamin(newVitamin)
            } finally {
            }
        }
    }

    fun toggleVitaminTaken(vitamin: Vitamin) {
        viewModelScope.launch {
            try {
                val updated = vitamin.copy(isTaken = !vitamin.isTaken)
                repository.addVitamin(updated)
            } finally {
            }
        }
    }

    fun deleteVitamin(vitamin: Vitamin) {
        viewModelScope.launch {
            try {
                repository.deleteVitamin(vitamin)
            } finally {
            }
        }
    }

    fun addCustomWater(amount: Int) {
        viewModelScope.launch {
            try {
                val newRecord = WaterRecord(
                    amountMl = amount,
                    timeAdded = "Now",
                    isSparkling = false,
                    temperature = 20.0f
                )
                repository.addWaterRecord(newRecord)
            } finally {
            }
        }
    }

    fun deleteWaterRecord(record: WaterRecord) {
        viewModelScope.launch {
            try {
                repository.deleteWaterRecord(record)
            } finally {
            }
        }
    }

    fun authenticateUser(activity: FragmentActivity, reason: String) {
        biometricManager.authenticate(activity, reason)
    }
}

class AppViewModelFactory(
    private val repository: AppRepository,
    private val biometricManager: AppBiometricManager,
    private val prefs: SharedPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, biometricManager, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}