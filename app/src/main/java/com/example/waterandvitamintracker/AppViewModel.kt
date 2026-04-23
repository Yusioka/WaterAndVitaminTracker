package com.example.waterandvitamintracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterandvitamintracker.data.AppRepository
import com.example.waterandvitamintracker.models.Vitamin
import com.example.waterandvitamintracker.models.WaterRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class AppViewModel(private val repository: AppRepository) : ViewModel() {

    val vitamins: StateFlow<List<Vitamin>> = repository.allVitamins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val waterRecords: StateFlow<List<WaterRecord>> = repository.allWaterRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getVitaminDetails(id: Int): StateFlow<Vitamin?> {
        return repository.getVitaminById(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun addRandomVitamin() {
        viewModelScope.launch {
            try {
                val randomId = Random.nextInt(100, 999)
                val newVitamin = Vitamin(
                    name = "Vitamin X-$randomId",
                    dailyDosageMg = Random.nextInt(100, 1000),
                    isEssential = Random.nextBoolean(),
                    category = "Custom"
                )
                repository.addVitamin(newVitamin)
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

    fun addWaterRecord() {
        viewModelScope.launch {
            try {
                val newRecord = WaterRecord(
                    amountMl = 250,
                    timeAdded = "Now",
                    isSparkling = Random.nextBoolean(),
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
}

class AppViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}