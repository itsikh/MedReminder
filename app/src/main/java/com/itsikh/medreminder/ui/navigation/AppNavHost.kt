package com.itsikh.medreminder.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.itsikh.medreminder.bugreport.ScreenshotHolder
import com.itsikh.medreminder.ui.components.DebugOverlayViewModel
import com.itsikh.medreminder.ui.components.FloatingBugButton
import com.itsikh.medreminder.ui.screens.bugreport.BugReportScreen
import com.itsikh.medreminder.ui.screens.bugreport.ReportMode
import com.itsikh.medreminder.ui.screens.home.HomeScreen
import com.itsikh.medreminder.ui.screens.log.LogScreen
import com.itsikh.medreminder.ui.screens.medication.AddEditScreen
import com.itsikh.medreminder.ui.screens.medication.MedicationListScreen
import com.itsikh.medreminder.ui.screens.settings.SettingsScreen
import com.itsikh.medreminder.ui.screens.snooze.SnoozeSettingsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val overlayVm: DebugOverlayViewModel = hiltViewModel()
    val showBugButton by overlayVm.showBugButton.collectAsState()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val bottomRoutes = listOf("home", "medications", "log")

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (currentRoute in bottomRoutes) {
                    NavigationBar {
                        NavigationBarItem(
                            selected = currentRoute == "home",
                            onClick = {
                                navController.navigate("home") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.Home, null) },
                            label = { Text("Today") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "medications",
                            onClick = {
                                navController.navigate("medications") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.List, null) },
                            label = { Text("Medications") }
                        )
                        NavigationBarItem(
                            selected = currentRoute == "log",
                            onClick = {
                                navController.navigate("log") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true; restoreState = true
                                }
                            },
                            icon = { Icon(Icons.Default.DateRange, null) },
                            label = { Text("History") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeScreen(onOpenSettings = { navController.navigate("settings") })
                }
                composable("medications") {
                    MedicationListScreen(
                        onAddNew = { navController.navigate("add_edit/0") },
                        onEdit = { id -> navController.navigate("add_edit/$id") }
                    )
                }
                composable("add_edit/{medId}") { back ->
                    val medId = back.arguments?.getString("medId")?.toIntOrNull()
                    AddEditScreen(
                        medId = medId,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("log") {
                    LogScreen()
                }
                composable("snooze_settings") {
                    SnoozeSettingsScreen(onBack = { navController.popBackStack() })
                }
                composable("settings") {
                    SettingsScreen(
                        onBack = { navController.popBackStack() },
                        onOpenBugReport = { mode -> navController.navigate("bug_report/${mode.name}") },
                        onOpenSnoozeSettings = { navController.navigate("snooze_settings") }
                    )
                }
                composable("bug_report/{mode}") { backStackEntry ->
                    val modeName = backStackEntry.arguments?.getString("mode")
                    val mode = modeName?.let { runCatching { ReportMode.valueOf(it) }.getOrNull() }
                        ?: ReportMode.BUG_REPORT
                    BugReportScreen(
                        mode = mode,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        FloatingBugButton(
            visible = showBugButton,
            onScreenshotCaptured = { bitmap ->
                ScreenshotHolder.store(bitmap)
                navController.navigate("bug_report/BUG_REPORT")
            }
        )
    }
}
