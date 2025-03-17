package com.example.senefavores.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseClient @Inject constructor() {

    private val SUPABASE_URL = "https://kebumzcxttyquorhiicf.supabase.co"
    private val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtlYnVtemN4dHR5cXVvcmhpaWNmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDE2NDM1MDQsImV4cCI6MjA1NzIxOTUwNH0.PiAnATAnWk_7Brz6XzZqQMkaCoGOItFGKhy1EZ8OnVg"

    val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        // install other modules as needed
    }
}
