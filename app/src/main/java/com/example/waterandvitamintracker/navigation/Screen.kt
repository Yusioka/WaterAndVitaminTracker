package com.example.waterandvitamintracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Home : Screen("home", "Vitamins", Icons.Default.List)
    object Detail : Screen("detail/{itemId}", "Details", null) {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
    object Water : Screen("water", "Water Tracker", Icons.Default.Add)
}