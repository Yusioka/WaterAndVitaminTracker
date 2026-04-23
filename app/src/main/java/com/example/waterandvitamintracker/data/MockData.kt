package com.example.waterandvitamintracker.data

import com.example.waterandvitamintracker.models.Vitamin

object MockData {
    val vitamins = listOf(
        Vitamin(1, "Vitamin C", 500, true, "Immunity"),
        Vitamin(2, "Vitamin D3", 2000, true, "Bones"),
        Vitamin(3, "Omega 3", 1000, false, "Heart"),
        Vitamin(4, "Magnesium", 400, true, "Nervous System")
    )
}