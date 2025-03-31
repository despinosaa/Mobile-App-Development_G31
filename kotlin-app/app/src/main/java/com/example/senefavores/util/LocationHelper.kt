package com.example.senefavores.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationHelper @Inject constructor(@ApplicationContext private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val currentLocation = mutableStateOf<Location?>(null)

    private val campusBounds = listOf(
        Location("").apply { latitude = 4.598; longitude = -74.069 },
        Location("").apply { latitude = 4.605; longitude = -74.062 }
    )

    fun isInsideCampus(location: Location?): Boolean {
        if (location == null) return false
        val campusMinLat = campusBounds.minOf { it.latitude }
        val campusMaxLat = campusBounds.maxOf { it.latitude }
        val campusMinLon = campusBounds.minOf { it.longitude }
        val campusMaxLon = campusBounds.maxOf { it.longitude }
        return location.latitude in campusMinLat..campusMaxLat &&
                location.longitude in campusMinLon..campusMaxLon
    }

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
                Log.i("Location","Ubicación actualizada: $location")
            } catch (e: Exception) {
                Log.e("Location","Error al obtener la ubicación: ${e.message}")
            }
        }
    }
}

@Composable
fun rememberLocationHelper(context: Context): LocationHelper {
    return remember { LocationHelper(context) }
}
