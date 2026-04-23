package com.example.waterandvitamintracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.waterandvitamintracker.data.AppDatabase
import com.example.waterandvitamintracker.data.MockApiService
import com.example.waterandvitamintracker.data.AppRepository
import com.example.waterandvitamintracker.navigation.Screen
import com.example.waterandvitamintracker.ui.VitaminDetailScreen
import com.example.waterandvitamintracker.ui.VitaminListScreen
import com.example.waterandvitamintracker.ui.WaterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tracker_database"
        ).build()

        val apiService = MockApiService()
        val repository = AppRepository(database.vitaminDao(), database.waterDao(), apiService)
        val factory = AppViewModelFactory(repository)

        setContent {
            MaterialTheme {
                MainApp(factory)
            }
        }
    }
}

@Composable
fun MainApp(factory: AppViewModelFactory) {
    val navController = rememberNavController()
    val appViewModel: AppViewModel = viewModel(factory = factory)
    val items = listOf(Screen.Home, Screen.Water)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            if (currentRoute == Screen.Home.route || currentRoute == Screen.Water.route) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let { Icon(it, contentDescription = screen.title) }
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                VitaminListScreen(
                    viewModel = appViewModel,
                    onVitaminClick = { id ->
                        navController.navigate(Screen.Detail.createRoute(id))
                    }
                )
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("itemId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("itemId") ?: 0
                VitaminDetailScreen(
                    vitaminId = id,
                    viewModel = appViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Water.route) {
                WaterScreen(viewModel = appViewModel)
            }
        }
    }
}