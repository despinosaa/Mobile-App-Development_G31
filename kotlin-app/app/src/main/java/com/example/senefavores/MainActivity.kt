package com.example.senefavores

import android.Manifest
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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var locationHelper: LocationHelper // Injected by Hilt
    private var hasLocationPermission by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Register callback for permissions
        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            println("Permisos concedidos: $hasLocationPermission") // Debug log
            if (hasLocationPermission) {
                locationHelper.getLastLocation()
            }
        }

        // Request permissions when activity starts
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
}
