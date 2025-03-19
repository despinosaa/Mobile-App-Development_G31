package com.example.senefavores.data.repository

import com.example.senefavores.data.model.User
import com.example.senefavores.data.model.UserInfo
import com.example.senefavores.data.remote.SupabaseManagement
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Azure
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.put
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
            supabaseClient.supabase.auth.getOAuthUrl(Azure) {
                scopes.add("openid email full_name phone")
            }
        }.onFailure {
            println("Azure sign-in failed: ${it.localizedMessage}")
        }
    }

    suspend fun getCurrentUser(): UserInfo? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val user =
                    supabaseClient.supabase.auth.retrieveUserForCurrentSession(updateSession = true)
                UserInfo(user.id, user.email ?: "No email")
            }.getOrElse {
                println("Error retrieving user: ${it.localizedMessage}")
                null
            }
        }
    }

    suspend fun updateUser(phone: String?, name: String?, profilePic: String?) {
        supabaseClient.supabase.auth.updateUser {
            this.phone = phone // Correcting assignment

            data {
                name?.let { put("name", it) }
                profilePic?.let { put("profilePic", it) }
            }
        }
    }


}
