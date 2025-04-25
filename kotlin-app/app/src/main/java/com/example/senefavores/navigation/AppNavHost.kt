package com.example.senefavores.navigation

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.repository.FavorRepository
import com.example.senefavores.data.repository.UserRepository
import com.example.senefavores.ui.screens.AccountScreen
import com.example.senefavores.ui.screens.CreateFavorScreen
import com.example.senefavores.ui.screens.ForgotPasswordScreen
import com.example.senefavores.ui.screens.HistoryScreen
import com.example.senefavores.ui.screens.HomeScreen
import com.example.senefavores.ui.screens.FavorScreen
import com.example.senefavores.ui.screens.ReviewScreen
import com.example.senefavores.ui.screens.SignInScreen
import com.example.senefavores.ui.screens.RegisterScreen
import com.example.senefavores.ui.screens.ResetPasswordScreen
import com.example.senefavores.util.LocationHelper
import com.example.senefavores.util.NetworkChecker
import com.example.senefavores.util.TelemetryLogger
import kotlinx.serialization.json.Json

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean,
    telemetryLogger: TelemetryLogger,
    favorRepository: FavorRepository,
    userRepository: UserRepository,
    networkChecker: NetworkChecker,
    onScreenChange: (String) -> Unit
) {
    NavHost(navController = navController, startDestination = "signIn") {
        composable("signIn") {
            SignInScreen(
                navController = navController,
                userRepository = userRepository,
                telemetryLogger = telemetryLogger,
                onScreenChange = onScreenChange
            )
            Log.d("AppNavHost", "Navigated to signIn")
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                userRepository = userRepository,
                telemetryLogger = telemetryLogger,
                onScreenChange = onScreenChange
            )
            Log.d("AppNavHost", "Navigated to register")
        }
        composable("forgot") {
            ForgotPasswordScreen(
                navController = navController,
                userRepository = userRepository,
                telemetryLogger = telemetryLogger,
                onScreenChange = onScreenChange
            )
            Log.d("AppNavHost", "Navigated to forgot")
        }
        composable("home") {
            HomeScreen(
                navController = navController,
                userViewModel = hiltViewModel(),
                favorViewModel = hiltViewModel(),
                telemetryLogger = telemetryLogger,
                networkChecker = networkChecker,
                onScreenChange = onScreenChange
            )
        }
        composable("history") {
            HistoryScreen(
                navController = navController,
                userViewModel = hiltViewModel(),
                favorViewModel = hiltViewModel(),
                telemetryLogger = telemetryLogger,
                favorRepository = favorRepository,
                networkChecker = networkChecker,
                onScreenChange = onScreenChange
            )
            Log.d("AppNavHost", "Navigated to history")
        }
        composable("account") {
            AccountScreen(
                navController = navController,
                userRepository = userRepository,
                telemetryLogger = telemetryLogger,
                onScreenChange = onScreenChange
            )
            Log.d("AppNavHost", "Navigated to account")
        }
        composable("resetPassword") {
            ResetPasswordScreen(
                navController = navController,
                userRepository = userRepository,
                telemetryLogger = telemetryLogger,
                onScreenChange = onScreenChange
            )
            Log.d("AppNavHost", "Navigated to resetPassword")
        }
        composable("createFavor") {
            CreateFavorScreen(
                navController = navController,
                locationHelper = locationHelper,
                hasLocationPermission = hasLocationPermission,
                telemetryLogger = telemetryLogger,
                favorRepository = favorRepository,
                onScreenChange = onScreenChange
            )
            onScreenChange("createFavor")
            Log.d("AppNavHost", "Navigated to createFavor")
        }
        composable(
            route = "favorScreen/{favorJson}",
            arguments = listOf(navArgument("favorJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val favorJson = backStackEntry.arguments?.getString("favorJson")
            val favor = favorJson?.let { Json.decodeFromString<Favor>(it) }
            if (favor != null) {
                FavorScreen(
                    navController = navController,
                    favor = favor,
                    locationHelper = locationHelper,
                    hasLocationPermission = hasLocationPermission,
                    telemetryLogger = telemetryLogger,
                    onScreenChange = onScreenChange
                )
                Log.d("AppNavHost", "Navigated to favorScreen with favor=$favor")
            } else {
                Text("Error al cargar el favor")
                Log.e("AppNavHost", "Failed to decode favorJson: $favorJson")
            }
        }
        composable(
            route = "review/{favorId}/{requestUserId}/{acceptUserId}",
            arguments = listOf(
                navArgument("favorId") { type = NavType.StringType },
                navArgument("requestUserId") { type = NavType.StringType },
                navArgument("acceptUserId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val favorId = backStackEntry.arguments?.getString("favorId") ?: ""
            val requestUserId = backStackEntry.arguments?.getString("requestUserId") ?: ""
            val acceptUserId = backStackEntry.arguments?.getString("acceptUserId") ?: ""
            if (favorId.isEmpty() || requestUserId.isEmpty() || acceptUserId.isEmpty()) {
                Text("Error: IDs inválidos")
                Log.e("AppNavHost", "Invalid arguments: favorId=$favorId, requestUserId=$requestUserId, acceptUserId=$acceptUserId")
            } else {
                ReviewScreen(
                    navController = navController,
                    favorId = favorId,
                    requestUserId = requestUserId,
                    acceptUserId = acceptUserId,
                    telemetryLogger = telemetryLogger,
                    onScreenChange = onScreenChange
                )
                Log.d("AppNavHost", "Navigated to review with favorId=$favorId, requestUserId=$requestUserId, acceptUserId=$acceptUserId")
            }
        }
    }
}