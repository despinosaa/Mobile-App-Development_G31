package com.example.senefavores.ui.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.Review
import com.example.senefavores.data.repository.FavorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private var currentOffset = 0
    private val pageSize = 100
    private var hasMoreFavors = true

    init {
        viewModelScope.launch {
            favorRepository.getLocalFavors().collectLatest { localFavors ->
                _allFavors.value = localFavors
                Log.d("FavorViewModel", "Loaded ${localFavors.size} local favors")
            }
        }
    }

    fun fetchFavors(userId: String?) {
        viewModelScope.launch {
            try {
                currentOffset = 0
                hasMoreFavors = true
                val fetchedFavors = favorRepository.getFavors(userId, currentOffset, pageSize)
                _favors.value = fetchedFavors
                hasMoreFavors = fetchedFavors.size == pageSize
                currentOffset += fetchedFavors.size
                Log.d("DEBUG", "Fetched ${fetchedFavors.size} favors from API (offset: 0), sorted by created_at (descending):")
                fetchedFavors.forEachIndexed { index, favor ->
                    Log.d("DEBUG", "Favor #$index: ${favor.title}, created_at: ${favor.created_at}")
                }
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error fetching favors: ${e.localizedMessage}", e)
                favorRepository.getLocalFavors().collectLatest { localFavors ->
                    _favors.value = localFavors
                }
            }
        }
    }

    fun loadMoreFavors(userId: String?) {
        if (!hasMoreFavors) {
            Log.d("FavorViewModel", "No more favors to load")
            return
        }

        viewModelScope.launch {
            try {
                val newFavors = favorRepository.getFavors(userId, currentOffset, pageSize)
                _favors.value = _favors.value + newFavors
                hasMoreFavors = newFavors.size == pageSize
                currentOffset += newFavors.size
                Log.d("DEBUG", "Loaded ${newFavors.size} more favors (offset: ${currentOffset - newFavors.size}), total: ${_favors.value.size}")
                newFavors.forEachIndexed { index, favor ->
                    Log.d("DEBUG", "Favor #${currentOffset - newFavors.size + index}: ${favor.title}, created_at: ${favor.created_at}")
                }
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error loading more favors: ${e.localizedMessage}", e)
                hasMoreFavors = false
            }
        }
    }

    fun fetchAllFavors() {
        viewModelScope.launch {
            try {
                _allFavors.value = favorRepository.getAllFavors()
                Log.d("DEBUG", "All favors from API: ${_allFavors.value}")
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error fetching all favors: ${e.localizedMessage}", e)
                favorRepository.getLocalFavors().collectLatest { localFavors ->
                    _allFavors.value = localFavors
                }
            }
        }
    }

    fun addFavor(favor: Favor) {
        viewModelScope.launch {
            favorRepository.addFavor(favor)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    fun updateFavorStatus(favorId: String, status: String) {
        viewModelScope.launch {
            try {
                favorRepository.updateFavorStatus(favorId, status)
                Log.d("FavorViewModel", "Updated favor $favorId to status=$status")
                val userId = _favors.value.firstOrNull { it.id == favorId }?.request_user_id
                fetchFavors(userId)
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error updating favor $favorId status: ${e.message}", e)
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

    suspend fun getFavorById(favorId: String): Favor? {
        if (favorId.isBlank()) {
            Log.e("FavorViewModel", "Invalid favorId: empty or blank")
            return null
        }
        return try {
            val favor = favorRepository.getFavorById(favorId)
            Log.d("FavorViewModel", "Fetched favor: $favor")
            favor
        } catch (e: Exception) {
            Log.e("FavorViewModel", "Error fetching favor: ${e.localizedMessage}, cause: ${e.cause}", e)
            null
        }
    }

    fun addReview(review: Review) {
        viewModelScope.launch {
            try {
                if (review.favor_id == null) {
                    Log.w("FavorViewModel", "favor_id is null in review: $review, skipping addition")
                    return@launch
                }
                favorRepository.addReview(review)
                Log.d("FavorViewModel", "Review added: $review")
                val reviews = favorRepository.getReviewsByReviewedId(review.reviewed_id)
                val averageStars = if (reviews.isNotEmpty()) {
                    reviews.map { it.stars }.average().toFloat()
                } else {
                    0.0f
                }
                favorRepository.updateClientStars(review.reviewed_id, averageStars)
                Log.d("FavorViewModel", "Updated client stars for ${review.reviewed_id}: $averageStars")
                fetchReviews()
            } catch (e: Exception) {
                Log.e("FavorViewModel", "Error adding review or updating stars: ${e.localizedMessage}", e)
                throw e
            }
        }
    }

    //Michi
    fun processQueue(){
        viewModelScope.launch {
            favorRepository.processQueuedFavors()
        }
    }
}