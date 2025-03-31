package com.example.senefavores.util

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.senefavores.data.model.CrashEntry
import com.example.senefavores.data.model.ResponseTimeEntry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TelemetryLogger @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val context: Context
) {
    suspend fun logResponseTime(screenName: String, responseTimeMs: Long) {
        try {
            val device = Build.MODEL
            val osVersion = Build.VERSION.RELEASE
            Log.d("TelemetryLogger", "Logging response time for screen: $screenName, time: $responseTimeMs ms")
            supabaseClient.from("kotlin_response_times").insert(
                ResponseTimeEntry(
                    screen = screenName,
                    response_time = responseTimeMs.toInt(),
                    device = device,
                    os_version = osVersion
                )
            )
            Log.d("TelemetryLogger", "Response time logged successfully")
        } catch (e: Exception) {
            Log.e("TelemetryLogger", "Failed to log response time: ${e.message}", e)
        }
    }

    suspend fun logCrash(screenName: String, crashInfo: String) {
        try {
            Log.d("TelemetryLogger", "Logging crash for screen: $screenName")
            supabaseClient.from("kotlin_crashes").insert(
                CrashEntry(
                    screen = screenName,
                    crash_info = crashInfo
                )
            )
            Log.d("TelemetryLogger", "Crash logged successfully")
        } catch (e: Exception) {
            Log.e("TelemetryLogger", "Failed to log crash: ${e.message}", e)
        }
    }
}