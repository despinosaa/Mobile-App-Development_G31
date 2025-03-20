package com.example.senefavores.ui.viewmodel

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senefavores.data.model.User
import com.example.senefavores.data.model.UserInfo
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
@HiltViewModel
class UserViewModel @Inject constructor(
    private val supabaseClient: SupabaseManagement,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user
    private val _hasCompletedInfo = MutableStateFlow(false) // Track completion
    val hasCompletedInfo: StateFlow<Boolean> = _hasCompletedInfo


    suspend fun loadUserInfo(): User? {
        Log.d("UserInfo", "Fetching user data...")

        val userData = userRepository.getCurrentUser()

        Log.d("UserInfo", "User loaded: $userData")

        _user.value = userData
        _hasCompletedInfo.value = !userData?.name.isNullOrEmpty() && !userData?.phone.isNullOrEmpty()

        Log.d("UserInfo", "hasCompletedInfo: ${_hasCompletedInfo.value}")

        return userData
    }



    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        checkUserSession()
    }

    fun updateUserInfo(clientId: String, name: String?, phone: String?, profilePic: String?, stars: Float?) {
        viewModelScope.launch {
            try {
                userRepository.updateClient(clientId, phone, name, profilePic, stars)
                loadUserInfo() // Refresh user data after update
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating user info", e)
            }
        }
    }


    private fun checkUserSession() {
        viewModelScope.launch {
            _isAuthenticated.value = userRepository.checkUserSession()
        }
    }

    fun signInWithAzure(context: Context) {
        viewModelScope.launch {
            val success = userRepository.signInWithAzure()
            _isAuthenticated.value = success
            Toast.makeText(context, if (success) "Login successful!" else "Login failed.", Toast.LENGTH_SHORT).show()
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
}
