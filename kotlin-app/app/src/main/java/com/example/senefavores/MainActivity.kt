package com.example.senefavores

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.senefavores.data.repository.FavorRepository
import com.example.senefavores.data.repository.UserRepository
import com.example.senefavores.navigation.AppNavHost
import com.example.senefavores.ui.theme.SenefavoresTheme
import com.example.senefavores.util.LocationHelper
import com.example.senefavores.util.TelemetryLogger
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    @Inject
    lateinit var locationHelper: LocationHelper

    @Inject
    lateinit var telemetryLogger: TelemetryLogger

    @Inject
    lateinit var favorRepository: FavorRepository

    @Inject
    lateinit var userRepository: UserRepository

    private var hasLocationPermission by mutableStateOf(false)
    private var currentScreen by mutableStateOf("MainActivity")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Set up global uncaught exception handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val crashInfo = throwable.stackTraceToString()
            runBlocking {
                telemetryLogger.logCrash(currentScreen, crashInfo)
            }
            defaultHandler?.uncaughtException(thread, throwable)
        }

        // Request location permissions
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            Log.d("MainActivity", "Location permissions granted: $hasLocationPermission")
            if (hasLocationPermission) {
                locationHelper.getLastLocation()
            }
        }
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )

        setContent {
            SenefavoresTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    // Handle deep link with NavController availability
                    HandleDeepLink(intent, navController)
                    AppNavHost(
                        navController = navController,
                        locationHelper = locationHelper,
                        hasLocationPermission = hasLocationPermission,
                        telemetryLogger = telemetryLogger,
                        favorRepository = favorRepository,
                        userRepository = userRepository,
                        onScreenChange = { screenName ->
                            currentScreen = screenName
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Update the intent
        handleDeepLink(intent, null)
    }

    @Composable
    private fun HandleDeepLink(intent: Intent?, navController: androidx.navigation.NavController) {
        LaunchedEffect(navController) {
            intent?.let { handleDeepLink(it, navController) }
        }
    }

    private fun handleDeepLink(intent: Intent, navController: androidx.navigation.NavController?) {
        val uri: Uri? = intent.data
        Log.d("AmongUs", "Deep link received: $uri")
        if (navController == null) {
            Log.w("AmongUs", "NavController is null, cannot navigate")
            return
        }

        if (uri != null && uri.scheme == "senefavores" && uri.host == "com.example.senefavores") {
            val type = uri.getQueryParameter("type")
            when (type) {
                "recovery" -> {
                    val accessToken = uri.getQueryParameter("access_token") ?: ""
                    Log.d("AmongUs", "Password reset deep link detected, access_token=$accessToken")
                    navController.navigate("resetPassword/$accessToken") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
                else -> {
                    Log.d("AmongUs", "Navigating to signIn for deep link type=$type")
                    navController.navigate("signIn") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        } else {
            Log.d("AmongUs", "Invalid or no deep link, no navigation")
        }
    }
}