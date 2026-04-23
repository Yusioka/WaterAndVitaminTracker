package com.example.waterandvitamintracker.data

import com.example.waterandvitamintracker.models.Vitamin
import com.example.waterandvitamintracker.models.WaterRecord
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class MockApiService {
    suspend fun postVitamin(vitamin: Vitamin): Boolean {
        delay(1000)
        return true
    }

    suspend fun deleteVitamin(id: Int): Boolean {
        delay(1000)
        return true
    }

    suspend fun postWaterRecord(record: WaterRecord): Boolean {
        delay(1000)
        return true
    }

    suspend fun deleteWaterRecord(id: Int): Boolean {
        delay(1000)
        return true
    }
}

class AppRepository(
    private val vitaminDao: VitaminDao,
    private val waterDao: WaterDao,
    private val api: MockApiService
) {
    val allVitamins: Flow<List<Vitamin>> = vitaminDao.getAllVitamins()
    val allWaterRecords: Flow<List<WaterRecord>> = waterDao.getAllWaterRecords()

    fun getVitaminById(id: Int): Flow<Vitamin?> = vitaminDao.getVitaminById(id)

    suspend fun addVitamin(vitamin: Vitamin) {
        try {
            vitaminDao.insertVitamin(vitamin)
            val success = api.postVitamin(vitamin)
            if (success) {
                vitaminDao.updateSyncStatus(vitamin.id, "synced")
            } else {
                vitaminDao.updateSyncStatus(vitamin.id, "error")
            }
        } finally {
        }
    }

    suspend fun deleteVitamin(vitamin: Vitamin) {
        try {
            vitaminDao.deleteVitamin(vitamin)
            api.deleteVitamin(vitamin.id)
        } finally {
        }
    }

    suspend fun addWaterRecord(record: WaterRecord) {
        try {
            waterDao.insertWaterRecord(record)
            val success = api.postWaterRecord(record)
            if (success) {
                waterDao.updateSyncStatus(record.id, "synced")
            } else {
                waterDao.updateSyncStatus(record.id, "error")
            }
        } finally {
        }
    }

    suspend fun deleteWaterRecord(record: WaterRecord) {
        try {
            waterDao.deleteWaterRecord(record)
            api.deleteWaterRecord(record.id)
        } finally {
        }
    }
}