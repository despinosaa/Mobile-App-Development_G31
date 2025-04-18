package com.example.senefavores.ui.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senefavores.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Azure
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.senefavores.data.remote.SupabaseManagement
import com.example.senefavores.data.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

@HiltViewModel
class UserViewModel @Inject constructor(
    private val supabaseClient: SupabaseManagement,
    private val userRepository: UserRepository,

    ): ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    private val _isAuthenticated = MutableStateFlow(false)
    private val _hasCompletedInfo = MutableStateFlow(false)
    val user: StateFlow<User?> = _user
    val hasCompletedInfo: StateFlow<Boolean> = _hasCompletedInfo
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    suspend fun loadUserAuthInfo(): User? {
        Log.d("UserInfo", "Fetching user data...")
        val userData = userRepository.getCurrentUser()
        Log.d("UserInfo", "User loaded: $userData")
        _user.value = userData
        _hasCompletedInfo.value = !userData?.name.isNullOrEmpty() && !userData?.phone.isNullOrEmpty()
        Log.d("UserInfo", "hasCompletedInfo: ${_hasCompletedInfo.value}")
        return userData
    }

    suspend fun loadUserClientInfo(): User? {
        Log.d("UserInfo", "Fetching user data...")
        val userData = userRepository.getCurrentClient()
        Log.d("UserInfo", "User loaded: $userData")
        _user.value = userData
        _hasCompletedInfo.value = !userData?.name.isNullOrEmpty() && !userData?.phone.isNullOrEmpty()
        Log.d("UserInfo", "hasCompletedInfo: ${_hasCompletedInfo.value}")
        return userData
    }

    suspend fun insertUserInClients() {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Inserting user into clients table")
                userRepository.insertUserInClients()
                Log.d("UserViewModel", "User inserted successfully")
                loadInfo() // Refresh user data
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error inserting user: ${e.localizedMessage}", e)
            }
        }
    }

    suspend fun getClientById(userId: String): User? {
        return try {
            val client = supabaseClient.supabase
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



    fun signInWithAzure() {
        viewModelScope.launch {
            val resp = userRepository.signInHELP()
            Log.d("AzureAuth", "hasCompletedInfo: ${resp}")
            //checkUserSession()
            Log.d("AzureAuth", "hasData: ${_isAuthenticated}")
        }
    }

    fun signInWithEmail(email: String, password: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        Log.d("AzureAuth", "Starting email sign-in: $email")
        viewModelScope.launch {
            try {
                userRepository.signInWithEmail(email, password)
                _isAuthenticated.value = true
                loadUserClientInfo()
                Log.d("AzureAuth", "Email sign-in successful")
                onResult(true, null)
            } catch (e: Exception) {
                Log.e("AzureAuth", "Email sign-in error: ${e.message}")
                _isAuthenticated.value = false
                val errorMessage = if (e.message?.contains("Invalid login credentials", ignoreCase = true) == true) {
                    "Credenciales inválidas"
                } else {
                    "Error al iniciar sesión: ${e.message}"
                }
                onResult(false, errorMessage)
            }
        }
    }

    fun signUpWithEmail(email: String, password: String) {
        Log.d("AzureAuth", "Starting email sign-in: $email")
        viewModelScope.launch {
            try {
                val success = userRepository.signUpWithEmail(email, password)
                Log.d("AzureAuth", "Email sign-in result: $success")
                //checkUserSession()
                _isAuthenticated.value = success
                if (_isAuthenticated.value) {
                    Log.d("AzureAuth", "Email sign-in successful")
                } else {
                    Log.e("AzureAuth", "Email sign-in failed: Invalid credentials")
                }
            } catch (e: Exception) {
                Log.e("AzureAuth", "Email sign-in error")
                _isAuthenticated.value = false
            }
        }
    }

    fun updateAuthUser(name: String, phone: String) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Updating user: name=$name, phone=$phone")
                userRepository.updateAuthUser(name, phone)
                loadInfo() // Refresh user data
                Log.d("UserViewModel", "User update successful")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user info: ${e.localizedMessage}", e)
            }
        }
    }


    fun updateClientsUser(name: String, phone: String) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Updating user: name=$name, phone=$phone")
                userRepository.updateClientsUser(name, phone)
                loadInfo() // Refresh user data
                Log.d("UserViewModel", "User update successful")
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user info: ${e.localizedMessage}", e)
            }
        }
    }

    private fun loadInfo() {
        // Placeholder: Fetch user data (e.g., from getCurrentUser)
        viewModelScope.launch {
            // Update state (not shown, assumed to exist)
            Log.d("UserViewModel", "Refreshing user info")
        }
    }

    fun checkUserSession(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val hasSession = userRepository.checkUserSession()
                if (hasSession) {
                    _isAuthenticated.value = true
                    loadUserClientInfo()
                    Log.d("UserViewModel", "Session check successful")
                } else {
                    Log.e("UserViewModel", "No active session found")
                }
                onResult(hasSession)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Session check error: ${e.message}")
                onResult(false)
            }
        }
    }

    fun checkAuthSession(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val hasSession = userRepository.checkUserSession()
                if (hasSession) {
                    _isAuthenticated.value = true
                    loadUserClientInfo()
                    Log.d("UserViewModel", "Session check successful")
                } else {
                    Log.e("UserViewModel", "No active session found")
                }
                onResult(hasSession)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Session check error: ${e.message}")
                onResult(false)
            }
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            try {
                supabaseClient.supabase.auth.signOut()
                _isAuthenticated.value = false
                Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Logout failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    suspend fun resetPassword(email: String): Boolean {
        return try {
            Log.d("UserViewModel", "Attempting password reset for email: $email")
            val success = userRepository.resetPassword(email)
            Log.d("UserViewModel", "Password reset result: $success")
            success
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error resetting password: ${e.localizedMessage}", e)
            false
        }
    }

    suspend fun verifyRecoveryOtp(email: String, token: String) {
        try {
            Log.d("UserViewModel", "Verifying recovery OTP for email: $email, token: $token")
            userRepository.verifyRecoveryOtp(email, token)
            _isAuthenticated.value = true
            Log.d("UserViewModel", "Recovery OTP verified")
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error verifying recovery OTP: ${e.localizedMessage}", e)
            throw e
        }
    }

    suspend fun updatePassword(newPassword: String) {
        try {
            Log.d("UserViewModel", "Updating password")
            userRepository.updatePassword(newPassword)
            Log.d("UserViewModel", "Password updated successfully")
        } catch (e: Exception) {
            Log.e("UserViewModel", "Error updating password: ${e.localizedMessage}", e)
            throw e
        }
    }

    fun resetPasswordFinal(newPassword: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.resetPasswordFinal(newPassword)
                Log.d("UserViewModel", "Password reset successful")
                onResult(true)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Password reset error: ${e.message}")
                onResult(false)
            }
        }
    }

    fun verifyEmailOtp(email: String, token: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.verifyEmailOtp(email, token)
                _isAuthenticated.value = true
                loadUserClientInfo()
                Log.d("UserViewModel", "Email verification successful for $email")
                onResult(true)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Email verification error: ${e.message}")
                onResult(false)
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.sendPasswordResetEmail(email)
                Log.d("UserViewModel", "Password reset email sent to $email")
                onResult(true)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error sending password reset email: ${e.message}")
                onResult(false)
            }
        }
    }

    fun exchangeCodeForSession(code: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                userRepository.exchangeCodeForSession(code)
                _isAuthenticated.value = true
                loadUserClientInfo()
                Log.d("UserViewModel", "Session exchanged successfully for code=$code")
                onResult(true)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error exchanging code for session: ${e.message}")
                _isAuthenticated.value = false
                onResult(false)
            }
        }
    }



    fun getCurrentUserId(): String? {
        return _user.value?.id
    }
}