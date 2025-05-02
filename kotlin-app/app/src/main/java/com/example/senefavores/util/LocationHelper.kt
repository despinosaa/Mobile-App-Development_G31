package com.example.senefavores.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationCache: LocationCache
) {

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLastLocation(userId: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            if (userId == null) {
                Log.w("Location", "No user ID provided, skipping cache")
                fetchNewLocation()
                return@launch
            }

            // Check cache first
            val cachedEntry = locationCache.getLocation(userId)
            if (cachedEntry != null) {
                val (cachedLocation, _) = cachedEntry
                withContext(Dispatchers.Main) {
                    currentLocation.value = cachedLocation
                    Log.i("Location", "Using cached location for user $userId: $cachedLocation")
                }
                return@launch
            }

            // No valid cache, fetch new location
            fetchNewLocation(userId)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun fetchNewLocation(userId: String? = null) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("Location", "Location permissions not granted")
            return
        }
        try {
            val location = withContext(Dispatchers.Main) {
                fusedLocationClient.lastLocation
            }.await()
            if (location != null) {
                val timestamp = LocalDateTime.now()
                withContext(Dispatchers.Main) {
                    currentLocation.value = location
                    if (userId != null) {
                        locationCache.putLocation(userId, location, timestamp)
                        Log.i("Location", "Fetched new location for user $userId: $location, timestamp: $timestamp")
                    } else {
                        Log.i("Location", "Fetched new location (no user): $location")
                    }
                }
            } else {
                Log.w("Location", "No location available")
            }
        } catch (e: Exception) {
            Log.e("Location", "Error fetching location: ${e.message}")
        }
    }
}