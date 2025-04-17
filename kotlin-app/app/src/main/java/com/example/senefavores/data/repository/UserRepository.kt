package com.example.senefavores.data.repository

import android.util.Log
import com.example.senefavores.data.model.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Azure
import io.github.jan.supabase.auth.providers.Github
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
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

            supabaseClient.auth.signInWith(Github){
                scopes.add("email")
            }

            supabaseClient.auth.awaitInitialization()
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

    suspend fun signInWithEmail(emailS: String, passwordS: String): Boolean {
        return try {
            supabaseClient.auth.signInWith(Email) {
                email = emailS
                password = passwordS
            }
            supabaseClient.auth.currentSessionOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Boolean {
        return try {
            supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            true // Supabase sign-up succeeds if no exception
        } catch (e: Exception) {
            false
        }
    }



    suspend fun waitForSessionAndEnsureUser(): Boolean {
        supabaseClient.auth.awaitInitialization()
        return runCatching {
            repeat(5) { attempt ->
                delay(1000)
                val session = supabaseClient.auth.currentSessionOrNull()
                Log.d("AzureAuth", "Attempt $attempt: Session after login: $session")
                session?.let {
                    val userId = it.user?.id ?: return@let
                    val userEmail = it.user?.email ?: return@let
                    Log.d("AzureAuth", "User ID: $userId, Email: $userEmail")
                    ensureUserExists(userId, userEmail)
                    Log.d("AzureAuth", "User existence ensured")
                    return@runCatching true
                }
            }
            Log.e("AzureAuth", "Session not found after 5 attempts.")
            false
        }.getOrElse {
            Log.e("AzureAuth", "Session check failed: ${it.localizedMessage}", it)
            false
        }
    }

    suspend fun funkySignInWithAzure(): Boolean {
        supabaseClient.auth.signInWith(Azure) {
            scopes.addAll(listOf("email"))
        }
        return false
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
        try {
            // Get user from Supabase DB
            val authUser = supabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d("UserFetch", "Auth user: id=${authUser?.id ?: "none"}, email=${authUser?.email ?: "none"}, metadata=${authUser?.userMetadata ?: "none"}")

            if (authUser == null || authUser.email == null) {
                Log.d("UserFetch", "No auth user or email found")
                return null
            }

            // Fetch from clients table
            val dbUser = supabaseClient
                .from("clients")
                .select(columns = Columns.list("id", "name", "email", "phone", "profilePic", "stars")) {
                    filter {
                        eq("id", authUser.id)
                    }
                }
                .decodeSingleOrNull<User>()

            Log.d("UserFetch", "DB user: ${dbUser?.toString() ?: "none"}")

            // Create User object
            val user = User(
                id = authUser.id,
                name = dbUser?.name ?: authUser.email!!.substringBefore("@"), // Fallback to email prefix
                email = authUser.email!!,
                phone = dbUser?.phone ?: "",
                profilePic = dbUser?.profilePic ?: "",
                stars = dbUser?.stars ?: 0.0f
            )
            Log.d("UserFetch", "Extracted User: id=${user.id}, name=${user.name}, email=${user.email}, phone=${user.phone}, profilePic=${user.profilePic}, stars=${user.stars}")
            return user
        } catch (e: Exception) {
            Log.e("UserFetch", "Error retrieving user: ${e.localizedMessage}", e)
            return null
        }
    }

    suspend fun getCurrentClient(): User? {
        try {
            // Get current session's user
            val authUser = supabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d("ClientFetch", "Session user: id=${authUser?.id ?: "none"}, email=${authUser?.email ?: "none"}")

            if (authUser == null) {
                Log.d("ClientFetch", "No auth user found")
                return null
            }

            // Query clients table
            val client = supabaseClient
                .from("clients")
                .select(columns = Columns.list("id", "name", "email", "phone", "profilePic", "stars")) {
                    filter {
                        eq("id", authUser.id)
                    }
                }
                .decodeSingleOrNull<User>()

            Log.d("ClientFetch", "Client from DB: ${client?.toString() ?: "none"}")
            return client
        } catch (e: Exception) {
            Log.e("ClientFetch", "Error fetching client: ${e.localizedMessage}", e)
            return null
        }
    }

    suspend fun insertUserInClients() {
        try {
            val authUser = supabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d("ClientInsert", "Session user: id=${authUser?.id ?: "none"}, email=${authUser?.email ?: "none"}")

            if (authUser == null || authUser.email == null) {
                Log.d("ClientInsert", "No auth user or email found")
                return
            }

            val client = User(
                id = authUser.id,
                name = "",
                email = authUser.email!!,
                phone = "",
                profilePic = "",
                stars = 0.0f
            )

            supabaseClient.from("clients").insert(client)
            Log.d("ClientInsert", "Inserted client: id=${authUser.id}, email=${authUser.email}")
        } catch (e: Exception) {
            Log.e("ClientInsert", "Error inserting client: ${e.localizedMessage}", e)
            throw e
        }
    }

    suspend fun checkUserSession(): Boolean {
        val session = supabaseClient.auth.currentSessionOrNull()
        Log.e("UserInfo","Session after redirect: $session")
        val user = supabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
        Log.e("UserInfo","User after redirect: $user")
        return session != null
    }

    suspend fun updateAuthUser(name: String, phone: String) {
        try {
            Log.d("UserRepository", "Updating user metadata: name=$name, phone=$phone")
            val metadata = buildJsonObject {
                put("name", name)
                put("phone", phone)
            }
            Log.d("UserRepository", "Metadata JsonObject: $metadata")
            val user = supabaseClient.auth.updateUser {
                data { metadata }
            }
            Log.d("UserRepository", "User metadata updated successfully")
            Log.d("UserRepository", "$user")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating user metadata: ${e.localizedMessage}", e)
            throw e // Let ViewModel handle
        }
    }

    suspend fun updateClientsUser(name: String, phone: String) {
        try {
            Log.d("UserRepository", "Updating clients table: name=$name, phone=$phone")
            val authUser = supabaseClient.auth.retrieveUserForCurrentSession(updateSession = true)
            Log.d("UserRepository", "Session user: id=${authUser?.id ?: "none"}")

            if (authUser == null) {
                Log.d("UserRepository", "No auth user found")
                throw Exception("No authenticated user")
            }

            supabaseClient.from("clients").update(
                {
                    set("name", name)
                    set("phone", phone)
                }
            ) {
                filter {
                    eq("id", authUser.id)
                }
            }

            Log.d("UserRepository", "Clients table updated successfully: id=${authUser.id}, name=$name, phone=$phone")
        } catch (e: Exception) {
            Log.e("UserRepository", "Error updating clients table: ${e.localizedMessage}", e)
            throw e // Let ViewModel handle
        }
    }

    suspend fun signInHELP(): Boolean {
        return try {
            supabaseClient.auth.signInWith(Github)
            true
        } catch (e: Exception) {
            false
        }
    }
}