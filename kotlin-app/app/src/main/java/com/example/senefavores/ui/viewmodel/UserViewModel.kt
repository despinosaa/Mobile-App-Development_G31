package com.example.senefavores.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo

    fun loadUserInfo() {
        viewModelScope.launch {
            _userInfo.value = userRepository.getCurrentUser()
        }
    }

    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    init {
        checkUserSession()
    }

    private fun checkUserSession() {
        viewModelScope.launch { // Allow Supabase time to restore session
            val session = supabaseClient.supabase.auth.currentSessionOrNull()
            println("Session after redirect: $session")
            _isAuthenticated.value = session != null
        }
    }

    fun signInWithAzure(context: Context) {
        viewModelScope.launch {
            try {
                supabaseClient.supabase.auth.signInWith(Azure) {
                    scopes.add("email")
                }

                repeat(5) { attempt ->
                    delay(1000) // Wait 1 second before checking session
                    val session = supabaseClient.supabase.auth.currentSessionOrNull()
                    println("Attempt $attempt: Session after login: $session")
                    if (session != null) {
                        _isAuthenticated.value = true
                        Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }

                _isAuthenticated.value = false
                Toast.makeText(context, "Login failed.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                _isAuthenticated.value = false
                Toast.makeText(context, "Login failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
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
}
