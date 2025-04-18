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

    private val _userReviews = MutableStateFlow<List<Review>>(emptyList())
    val userReviews: StateFlow<List<Review>> = _userReviews

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

    fun fetchUserReviews(reviewedId: String) {
        viewModelScope.launch {
            try {
                val reviews = favorRepository.getReviewsByReviewedId(reviewedId)
                _userReviews.value = reviews
                Log.d("FavorViewModel", "Fetched ${reviews.size} reviews for reviewed_id=$reviewedId")
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error fetching user reviews for reviewed_id=$reviewedId: ${e.message}", e)
            }
        }
    }

    fun fetchReviews() {
        viewModelScope.launch {
            try {
                val reviews = favorRepository.getReviews()
                _reviews.value = reviews
                Log.d("FavorViewModel", "Fetched ${reviews.size} reviews")
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error fetching reviews: ${e.message}", e)
            }
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            try {
                // Insert the review
                favorRepository.addReview(review)
                Log.d("FavorViewModel", "Review added: $review")

                // Update client stars
                val reviews = favorRepository.getReviewsByReviewedId(review.reviewed_id)
                val averageStars = if (reviews.isNotEmpty()) {
                    reviews.map { it.stars }.average().toFloat()
                } else {
                    0.0f
                }
                favorRepository.updateClientStars(review.reviewed_id, averageStars)
                Log.d("FavorViewModel", "Updated client stars for ${review.reviewed_id}: $averageStars")

                // Refresh reviews to keep UI in sync
                fetchReviews()
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error adding review or updating stars: ${e.localizedMessage}", e)
                throw e // Propagate to ReviewScreen for Snackbar
            }
        }
    }

}