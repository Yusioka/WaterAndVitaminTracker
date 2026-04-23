package com.example.waterandvitamintracker.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vitamins")
data class Vitamin(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dailyDosageMg: Int,
    val isEssential: Boolean,
    val category: String,
    val syncStatus: String = "pending"
)

@Entity(tableName = "water_records")
data class WaterRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amountMl: Int,
    val timeAdded: String,
    val isSparkling: Boolean,
    val temperature: Float,
    val syncStatus: String = "pending"
)