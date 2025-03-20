package com.example.senefavores.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.senefavores.data.model.Favor
import com.example.senefavores.ui.screens.AccountScreen
import com.example.senefavores.ui.screens.CreateFavorScreen
import com.example.senefavores.ui.screens.ForgotPasswordScreen
import com.example.senefavores.ui.screens.HistoryScreen
import com.example.senefavores.ui.screens.HomeScreen
import com.example.senefavores.ui.screens.LogInScreen
import com.example.senefavores.ui.screens.FavorScreen
import com.example.senefavores.util.LocationHelper
import kotlinx.serialization.json.Json

@Composable
fun AppNavHost(
    navController: NavHostController,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean
) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LogInScreen(navController) }
        composable("forgot") { ForgotPasswordScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("history") { HistoryScreen(navController) }
        composable("account") { AccountScreen(navController) }
        composable("createFavor") {
            CreateFavorScreen(
                navController = navController,
                locationHelper = locationHelper,
                hasLocationPermission = hasLocationPermission
            )
        }
        composable(
            route = "favorScreen/{favorJson}",
            arguments = listOf(navArgument("favorJson") {})
        ) { backStackEntry ->
            val favorJson = backStackEntry.arguments?.getString("favorJson")
            val favor = favorJson?.let { Json.decodeFromString<Favor>(it) }
            if (favor != null) {
                FavorScreen(
                    navController = navController,
                    favor = favor,
                    locationHelper = locationHelper,
                    hasLocationPermission = hasLocationPermission
                )
            } else {
                Text("Error al cargar el favor")
            }
        }
    }
}
