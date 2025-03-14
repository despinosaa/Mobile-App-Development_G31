package com.example.senefavores.data
/*
import io.supabase.kotlin.PostgrestClient
import io.supabase.kotlin.SupabaseClient
import io.supabase.kotlin.auth.Auth
import io.supabase.kotlin.storage.Storage
*/
object SupabaseInstance {
    private const val SUPABASE_URL = "https://your-project.supabase.co"
    private const val SUPABASE_KEY = "your-anon-key"

    // Comment out everything that depends on `client`
    /*
    val client: SupabaseClient by lazy {
        SupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        )
    }

    val auth: Auth
        get() = client.auth

    val database: PostgrestClient
        get() = client.postgrest

    val storage: Storage
        get() = client.storage
    */
}
