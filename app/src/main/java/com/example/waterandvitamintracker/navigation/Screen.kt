package com.example.waterandvitamintracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector?) {
    object Login : Screen("login", "Login", null)
    object Home : Screen("home", "Vitamins", Icons.Default.List)
    object Detail : Screen("detail/{itemId}", "Details", null) {
        fun createRoute(itemId: Int) = "detail/$itemId"
    }
    object Water : Screen("water", "Water", Icons.Default.Add)
    object Stats : Screen("stats", "Live Stats", Icons.Default.Info)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}