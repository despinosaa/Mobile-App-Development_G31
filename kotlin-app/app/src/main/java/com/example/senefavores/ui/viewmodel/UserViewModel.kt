package com.example.senefavores.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Azure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.senefavores.data.remote.SupabaseClient

@HiltViewModel
class UserViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    fun signInWithAzure(context: Context) {
        viewModelScope.launch {
            try {
                supabaseClient.supabase.auth.signInWith(Azure) {
                    scopes.add("email")
                }
                _isAuthenticated.value = true
                Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
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
