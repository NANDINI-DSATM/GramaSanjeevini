package com.dsatm.gramasanjeevini.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dsatm.gramasanjeevini.ui.screens.EmergencyScreen
import com.dsatm.gramasanjeevini.ui.screens.ExpiryAlertScreen
import com.dsatm.gramasanjeevini.ui.screens.HomeScreen
import com.dsatm.gramasanjeevini.ui.screens.PharmacistDashboardScreen
import com.dsatm.gramasanjeevini.ui.screens.PharmacistLoginScreen
import com.dsatm.gramasanjeevini.ui.screens.SearchScreen
import com.dsatm.gramasanjeevini.ui.viewmodel.PharmacistViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Search : Screen("search")
    object Emergency : Screen("emergency")
    object PharmacistLogin : Screen("pharmacist_login")
    object PharmacistDashboard : Screen("pharmacist_dashboard")
    object ExpiryAlerts : Screen("expiry_alerts")
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    // Shared ViewModel for pharmacist login → dashboard → expiry flow
    val pharmacistViewModel: PharmacistViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.Emergency.route) {
            EmergencyScreen(navController = navController)
        }
        composable(Screen.PharmacistLogin.route) {
            PharmacistLoginScreen(
                navController = navController,
                viewModel = pharmacistViewModel
            )
        }
        composable(Screen.PharmacistDashboard.route) {
            PharmacistDashboardScreen(
                navController = navController,
                viewModel = pharmacistViewModel
            )
        }
        composable(Screen.ExpiryAlerts.route) {
            ExpiryAlertScreen(
                navController = navController,
                viewModel = pharmacistViewModel
            )
        }
    }
}
