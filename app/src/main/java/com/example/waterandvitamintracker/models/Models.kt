package com.example.waterandvitamintracker.models

data class Vitamin(
    val id: Int,
    val name: String,
    val dailyDosageMg: Int,
    val isEssential: Boolean,
    val category: String
)

data class WaterRecord(
    val id: Int,
    val amountMl: Int,
    val timeAdded: String,
    val isSparkling: Boolean,
    val temperature: Float
)