package com.example.senefavores.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Estado para almacenar la ubicación
    val currentLocation = mutableStateOf<Location?>(null)

    // Coordenadas del campus de la Universidad de los Andes (Bogotá, Colombia)
    private val campusBounds = listOf(
        Location("").apply { latitude = 4.598; longitude = -74.069 },
        Location("").apply { latitude = 4.605; longitude = -74.062 }
    )

    // Verificar si el usuario está dentro del campus
    fun isInsideCampus(location: Location?): Boolean {
        if (location == null) return false
        val campusMinLat = campusBounds.minOf { it.latitude }
        val campusMaxLat = campusBounds.maxOf { it.latitude }
        val campusMinLon = campusBounds.minOf { it.longitude }
        val campusMaxLon = campusBounds.maxOf { it.longitude }
        return location.latitude in campusMinLat..campusMaxLat &&
                location.longitude in campusMinLon..campusMaxLon
    }

    // Obtener la última ubicación (llamado después de verificar permisos)
    fun getLastLocation() {
        CoroutineScope(Dispatchers.IO).launch {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@launch
            }
            try {
                val location = withContext(Dispatchers.Main) {
                    fusedLocationClient.lastLocation
                }.await()
                currentLocation.value = location
                println("Ubicación actualizada: $location") // Log para depuración
            } catch (e: Exception) {
                println("Error al obtener la ubicación: ${e.message}") // Log para errores
            }
        }
    }
}

// Función composable para usar LocationHelper
@Composable
fun rememberLocationHelper(context: Context): LocationHelper {
    return remember {
        LocationHelper(context)
    }
}