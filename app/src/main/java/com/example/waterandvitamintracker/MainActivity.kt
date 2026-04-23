package com.example.waterandvitamintracker

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.example.waterandvitamintracker.security.RealBiometricManager
import com.example.waterandvitamintracker.ui.LockScreen
import com.example.waterandvitamintracker.ui.LoginScreen
import com.example.waterandvitamintracker.ui.ProfileScreen
import com.example.waterandvitamintracker.ui.StatsScreen
import com.example.waterandvitamintracker.ui.VitaminDetailScreen
import com.example.waterandvitamintracker.ui.VitaminListScreen
import com.example.waterandvitamintracker.ui.WaterScreen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "tracker_database"
        )
            .fallbackToDestructiveMigration()
            .build()

        val apiService = MockApiService()
        val repository = AppRepository(database.vitaminDao(), database.waterDao(), apiService)
        val biometricManager = RealBiometricManager(this)
        val sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val factory = AppViewModelFactory(repository, biometricManager, sharedPreferences)

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
    val items = listOf(Screen.Home, Screen.Water, Screen.Stats, Screen.Profile)

    val lifecycleOwner = LocalLifecycleOwner.current
    val isAppLocked by appViewModel.isAppLocked.collectAsState()

    DisposableEffect(lifecycleOwner) {
        var backgroundTime = 0L
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                backgroundTime = System.currentTimeMillis()
                appViewModel.disconnectWs()
            } else if (event == Lifecycle.Event.ON_RESUME) {
                if (backgroundTime > 0) {
                    val timeout = appViewModel.biometricManager.getLockTimeoutSeconds()
                    val diff = (System.currentTimeMillis() - backgroundTime) / 1000
                    if (appViewModel.biometricManager.isEnabledByUser() && diff >= timeout) {
                        appViewModel.lockApp()
                    }
                }
                appViewModel.connectWs()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (isAppLocked) {
        LockScreen(appViewModel)
    } else {
        Scaffold(
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                if (items.any { it.route == currentRoute } || currentRoute == Screen.Detail.route) {
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
                startDestination = Screen.Login.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(
                        viewModel = appViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }
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
                composable(Screen.Stats.route) {
                    StatsScreen(viewModel = appViewModel)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(viewModel = appViewModel)
                }
            }
        }
    }
}