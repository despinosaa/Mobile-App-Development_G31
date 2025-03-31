package com.example.senefavores

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import io.github.jan.supabase.auth.handleDeeplinks
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.lifecycle.lifecycleScope
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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

        // Handle deep link for OAuth login
        intent?.data?.let { handleDeepLink(intent) }

        // Request location permissions
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            println("DEBUG: Location permissions granted: $hasLocationPermission")
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
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri: Uri? = intent.data
        if (uri != null) {
            println("DEBUG: Deep link received: $uri")
            lifecycleScope.launch {
                val sessionExists = userRepository.checkUserSession()
                println("DEBUG: Session exists after deep link: $sessionExists")
            }
        } else {
            println("DEBUG: No deep link found in intent")
        }
    }
}