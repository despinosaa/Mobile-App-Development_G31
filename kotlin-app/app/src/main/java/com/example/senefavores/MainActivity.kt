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
import com.example.senefavores.navigation.AppNavHost
import com.example.senefavores.ui.theme.SenefavoresTheme
import com.example.senefavores.util.LocationHelper
import com.example.senefavores.data.remote.SupabaseManagement
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.auth.handleDeeplinks
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationHelper: LocationHelper

    private var hasLocationPermission by mutableStateOf(false)

    // Create only one instance of SupabaseClient
    private val supabaseClient by lazy { SupabaseManagement() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                        hasLocationPermission = hasLocationPermission
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
            supabaseClient.supabase.handleDeeplinks(intent)
        } else {
            println("DEBUG: No deep link found in intent")
        }
    }
}
