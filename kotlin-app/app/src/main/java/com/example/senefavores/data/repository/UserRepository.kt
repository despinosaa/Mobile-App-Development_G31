package com.example.senefavores.data.repository

import com.example.senefavores.data.model.User
import com.example.senefavores.data.remote.SupabaseManagement
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Azure
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(private val supabaseClient: SupabaseManagement) {

    suspend fun getUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            runCatching {
                supabaseClient.supabase
                    .from("users")
                    .select()
                    .decodeList<User>()
            }.getOrElse {
                println("Error fetching users: ${it.localizedMessage}")
                emptyList()
            }
        }
    }

    suspend fun logout() {
        supabaseClient.supabase.auth.signOut()
    }

    suspend fun signInWithAzure() {
        runCatching {
            supabaseClient.supabase.auth.signInWith(Azure) {
                scopes.add("offline_access")
            }
        }.onFailure {
            println("Azure sign-in failed: ${it.localizedMessage}")
        }
    }
}
