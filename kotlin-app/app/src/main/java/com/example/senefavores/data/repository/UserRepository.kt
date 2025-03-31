package com.example.senefavores.data.repository

import android.util.Log
import com.example.senefavores.data.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Azure
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun getUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            runCatching {
                supabaseClient
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
        supabaseClient.auth.signOut()
    }

    suspend fun getClientById(userId: String): User? {
        return try {
            val client = supabaseClient
                .from("clients")
                .select(columns = Columns.list("id", "name", "email", "phone", "profilePic", "stars")) {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<User>()

            Log.d("UserRepository", "Fetched client: $client")
            client
        } catch (e: Exception) {
            Log.e("UserRepository", "Error fetching user: ${e.localizedMessage}", e)
            null
        }
    }

    suspend fun signInWithAzure(): Boolean {
        return runCatching {
            Log.d("AzureAuth", "Starting Azure sign-in...")

            supabaseClient.auth.signInWith(Azure) {
                scopes.addAll(listOf("email", "phone"))
            }

            repeat(5) { attempt ->
                delay(1000) // Wait 1 second before checking session
                val session = supabaseClient.auth.currentSessionOrNull()
                Log.d("AzureAuth", "Attempt $attempt: Session after login: $session")

                session?.let {
                    val userId = it.user?.id ?: return@let
                    val userEmail = it.user?.email ?: return@let

                    Log.d("AzureAuth", "User ID: $userId, Email: $userEmail")

                    // Check if user exists and insert if not
                    ensureUserExists(userId, userEmail)

                    Log.d("AzureAuth", "User existence ensured")
                    return@runCatching true
                }
            }

            Log.e("AzureAuth", "Login failed after 5 attempts.")
            false
        }.getOrElse {
            Log.e("AzureAuth", "Azure sign-in failed: ${it.localizedMessage}", it)
            false
        }
    }

    private suspend fun ensureUserExists(userId: String, email: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("UserCheck", "Checking if user exists: $userId")

                val existingUser = supabaseClient
                    .from("clients")
                    .select(columns = Columns.list("id", "email")) {
                        filter { eq("id", userId) }
                    }
                    .decodeSingleOrNull<User>()

                if (existingUser == null) {
                    Log.d("UserCheck", "User does not exist, inserting: $userId")

                    supabaseClient.from("clients").insert(
                        User(id = userId, email = email)
                    )

                    Log.d("UserCheck", "New user added: $userId")
                } else {
                    Log.d("UserCheck", "User already exists: $userId")
                }
            } catch (e: Exception) {
                Log.e("UserCheck", "Error checking user: ${e.localizedMessage}", e)
            }
        }
    }

    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            runCatching {
                val session = supabaseClient.auth.currentSessionOrNull()
                val authUser = session?.user ?: return@runCatching null

                // Fetch additional user data from "clients" table
                val dbUser = supabaseClient
                    .from("clients")
                    .select(columns = Columns.list("id", "name", "email", "phone", "profilePic", "stars")) {
                        filter {
                            eq("id", authUser.id)
                        }
                    }
                    .decodeSingleOrNull<User>()

                dbUser?.let {
                    authUser.email?.let { email ->
                        User(
                            id = authUser.id,
                            name = it.name,
                            email = email,
                            phone = it.phone,
                            profilePic = it.profilePic,
                            stars = it.stars ?: 0.0f
                        )
                    }
                }
            }.getOrElse {
                Log.e("UserFetch", "Error retrieving user: ${it.localizedMessage}", it)
                null
            }
        }
    }

    suspend fun checkUserSession(): Boolean {
        val session = supabaseClient.auth.currentSessionOrNull()
        println("Session after redirect: $session")
        return session != null
    }

    suspend fun updateClient(clientId: String, phone: String?, name: String?, profilePic: String?, stars: Float?) {
        supabaseClient.from("clients").update(
            {
                name?.let { set("name", it) }
                phone?.let { set("phone", it) }
                profilePic?.let { set("profilePic", it) }
                stars?.let { set("stars", it) }
            }
        ) {
            filter {
                eq("id", clientId)
            }
        }
    }
}