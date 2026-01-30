package com.pragament.employerreportsviewer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pragament.employerreportsviewer.ui.screens.ReportsDashboard
import com.pragament.employerreportsviewer.ui.screens.SettingsScreen
import com.pragament.employerreportsviewer.viewmodel.ReportsViewModel
import com.pragament.employerreportsviewer.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    object Reports : Screen("reports")
    object Settings : Screen("settings")
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    reportsViewModel: ReportsViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Reports.route
    ) {
        composable(Screen.Reports.route) {
            ReportsDashboard(
                viewModel = reportsViewModel,
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = {
                    // Refresh reports when coming back from settings
                    reportsViewModel.checkConfiguration()
                    navController.popBackStack()
                }
            )
        }
    }
}
