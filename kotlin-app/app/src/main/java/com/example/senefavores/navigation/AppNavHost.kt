package com.example.senefavores.navigation

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.repository.FavorRepository
import com.example.senefavores.data.repository.UserRepository
import com.example.senefavores.ui.screens.AccountScreen
import com.example.senefavores.ui.screens.CreateFavorScreen
import com.example.senefavores.ui.screens.ForgotPasswordScreen
import com.example.senefavores.ui.screens.HistoryScreen
import com.example.senefavores.ui.screens.HomeScreen
import com.example.senefavores.ui.screens.FavorScreen
import com.example.senefavores.ui.screens.SignInScreen
import com.example.senefavores.ui.screens.RegisterScreen
import com.example.senefavores.ui.screens.ResetPasswordScreen
import com.example.senefavores.util.LocationHelper
import com.example.senefavores.util.TelemetryLogger
import kotlinx.serialization.json.Json
import java.net.URLDecoder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(
    navController: NavHostController,
    locationHelper: LocationHelper,
    hasLocationPermission: Boolean,
    telemetryLogger: TelemetryLogger,
    favorRepository: FavorRepository,
    userRepository: UserRepository,
    onScreenChange: (String) -> Unit
) {
    NavHost(navController = navController, startDestination = "signIn") {
        composable("signIn") {
            SignInScreen(navController)
            onScreenChange("signIn")
            Log.d("AppNavHost", "Navigated to signIn")
        }
        composable("register") {
            RegisterScreen(navController)
            onScreenChange("register")
            Log.d("AppNavHost", "Navigated to register")
        }
        composable("forgot") {
            ForgotPasswordScreen(navController)
            onScreenChange("forgot")
            Log.d("AppNavHost", "Navigated to forgot")
        }
        composable("home") {
            HomeScreen(navController)
            onScreenChange("home")
            Log.d("AppNavHost", "Navigated to home")
        }
        composable("history") {
            HistoryScreen(navController)
            onScreenChange("history")
            Log.d("AppNavHost", "Navigated to history")
        }
        composable("account") {
            AccountScreen(navController)
            onScreenChange("account")
            Log.d("AppNavHost", "Navigated to account")
        }
        composable(
            route = "reset_password?token={token}&type={type}",
            arguments = listOf(
                navArgument("token") { nullable = true },
                navArgument("type") { nullable = true }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "senefavores://com.example.senefavores?token={token}&type={type}"
                    action = Intent.ACTION_VIEW
                }
            )
        ) { backStackEntry ->
            val token = backStackEntry.arguments?.getString("token")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: ""
            val type = backStackEntry.arguments?.getString("type")?.trim() ?: ""
            Log.d("AppNavHost", "Deep link matched: reset_password with token=$token, type=$type")
            ResetPasswordScreen(navController = navController, token = token, type = type)
            onScreenChange("reset_password")
        }
        composable(
            route = "verify_signup?code={code}&type={type}",
            arguments = listOf(
                navArgument("code") { nullable = true },
                navArgument("type") { nullable = true }
            ),
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "senefavores://com.example.senefavores?code={code}&type={type}"
                    action = Intent.ACTION_VIEW
                }
            )
        ) { backStackEntry ->
            val code = backStackEntry.arguments?.getString("code")?.let {
                URLDecoder.decode(it, "UTF-8")
            } ?: ""
            val type = backStackEntry.arguments?.getString("type")?.trim() ?: ""
            Log.d("AppNavHost", "Deep link matched: verify_signup with code=$code, type=$type")
            // Navigate to SignInScreen after verification
            SignInScreen(navController)
            onScreenChange("signIn")
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
                onScreenChange("favorScreen")
                Log.d("AppNavHost", "Navigated to favorScreen with favor=$favor")
            } else {
                Text("Error al cargar el favor")
                Log.e("AppNavHost", "Failed to decode favorJson: $favorJson")
            }
        }
    }
}