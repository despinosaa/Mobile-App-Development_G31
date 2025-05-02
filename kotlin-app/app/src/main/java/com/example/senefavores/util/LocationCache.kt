package com.example.senefavores.util

import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.collection.LruCache
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationCache @Inject constructor() {
    private val cache = LruCache<String, Pair<Location, LocalDateTime>>(10)
    private val FRESHNESS_WINDOW_MS = 5_000L // 5 seconds
    private val TAG = "LocationCache"

    @RequiresApi(Build.VERSION_CODES.O)
    fun getLocation(userId: String): Pair<Location, LocalDateTime>? {
        val entry = cache.get(userId)
        if (entry == null) {
            Log.d(TAG, "Cache miss for userId: $userId - No entry found")
            return null
        }
        val (location, timestamp) = entry
        val currentTime = LocalDateTime.now()
        val timestampMillis = timestamp.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        val currentMillis = currentTime.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        val ageMillis = currentMillis - timestampMillis

        return if (ageMillis <= FRESHNESS_WINDOW_MS) {
            Log.i(TAG, "Cache hit for userId: $userId - Location: (${location.latitude}, ${location.longitude}), Timestamp: $timestamp, Age: ${ageMillis}ms")
            entry
        } else {
            Log.d(TAG, "Cache miss for userId: $userId - Entry stale, age: ${ageMillis}ms, removing from cache")
            cache.remove(userId)
            null
        }
    }

    fun putLocation(userId: String, location: Location, timestamp: LocalDateTime) {
        Log.i(TAG, "Storing location for userId: $userId - Location: (${location.latitude}, ${location.longitude}), Timestamp: $timestamp")
        cache.put(userId, Pair(location, timestamp))
    }
}