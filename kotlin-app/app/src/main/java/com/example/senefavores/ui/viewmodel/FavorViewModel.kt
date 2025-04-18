package com.example.senefavores.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.Review
import com.example.senefavores.data.repository.FavorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavorViewModel @Inject constructor(
    private val favorRepository: FavorRepository
) : ViewModel() {

    private val _favors = MutableStateFlow<List<Favor>>(emptyList())
    val favors: StateFlow<List<Favor>> = _favors.asStateFlow()

    private val _allFavors = MutableStateFlow<List<Favor>>(emptyList())
    val allFavors: StateFlow<List<Favor>> = _allFavors.asStateFlow()


    private val _reviews= MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    fun fetchFavors(userId: String?) {
        viewModelScope.launch {
            _favors.value = favorRepository.getFavors(userId)
            Log.d("DEBUG", "Favor time from API: ${_favors.value}")
        }
    }

    fun fetchAllFavors() {
        viewModelScope.launch {
            _allFavors.value = favorRepository.getAllFavors()
            Log.d("DEBUG", "All favors from API: ${_allFavors.value}")
        }
    }

    fun addFavor(favor: Favor) {
        viewModelScope.launch {
            favorRepository.addFavor(favor)
        }
    }

    fun acceptFavor(favorId: String, userId: String) {
        viewModelScope.launch {
            try {
                favorRepository.updateFavorAcceptUserId(favorId, userId)
                Log.d("FavorViewModel", "Favor $favorId accepted by user $userId")

                fetchFavors(userId)
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error accepting favor $favorId: ${e.localizedMessage}", e)
                throw e
            }
        }
    }

    fun fetchReviews() {
        viewModelScope.launch {
            try {
                Log.d("FavorViewModel", "Fetching all reviews")
                _reviews.value = favorRepository.getReviews()
                Log.d("FavorViewModel", "Fetched reviews: ${reviews.value.size} items")
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error fetching reviews: ${e.localizedMessage}", e)
                _reviews.value = emptyList()
            }
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            try {
                Log.d("FavorViewModel", "Adding review: id=${review.id}, title=${review.title}")
                favorRepository.addReview(review)
                Log.d("FavorViewModel", "Review added successfully")
                fetchReviews() // Refresh reviews
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error adding review: ${e.localizedMessage}", e)
                throw e
            }
        }
    }

}