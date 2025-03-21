package com.example.senefavores.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.repository.FavorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavorViewModel @Inject constructor(
    private val favorRepository: FavorRepository
) : ViewModel() {

    private val _favors = MutableStateFlow<List<Favor>>(emptyList())
    val favors: StateFlow<List<Favor>> = _favors

    init {
        fetchFavors() // Fetch favors as soon as ViewModel is created
    }


    fun fetchFavors() {
        viewModelScope.launch {
            _favors.value = favorRepository.getFavors()
            Log.d("DEBUG", "Favor time from API: $_favors")
        }
    }

    fun addFavor(favor: Favor) {
        viewModelScope.launch {
            favorRepository.addFavor(favor)
        }
    }
}
