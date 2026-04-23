package com.example.waterandvitamintracker.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.example.waterandvitamintracker.models.Vitamin
import com.example.waterandvitamintracker.models.WaterRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface VitaminDao {
    @Query("SELECT * FROM vitamins")
    fun getAllVitamins(): Flow<List<Vitamin>>

    @Query("SELECT * FROM vitamins WHERE id = :vitaminId")
    fun getVitaminById(vitaminId: Int): Flow<Vitamin?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVitamin(vitamin: Vitamin): Long

    @Delete
    suspend fun deleteVitamin(vitamin: Vitamin): Int

    @Query("UPDATE vitamins SET syncStatus = :status WHERE id = :vitaminId")
    suspend fun updateSyncStatus(vitaminId: Int, status: String): Int
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_records")
    fun getAllWaterRecords(): Flow<List<WaterRecord>>

    @Query("SELECT * FROM water_records WHERE id = :recordId")
    fun getWaterRecordById(recordId: Int): Flow<WaterRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterRecord(record: WaterRecord): Long

    @Delete
    suspend fun deleteWaterRecord(record: WaterRecord): Int

    @Query("UPDATE water_records SET syncStatus = :status WHERE id = :recordId")
    suspend fun updateSyncStatus(recordId: Int, status: String): Int
}

@Database(entities = [Vitamin::class, WaterRecord::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vitaminDao(): VitaminDao
    abstract fun waterDao(): WaterDao
}