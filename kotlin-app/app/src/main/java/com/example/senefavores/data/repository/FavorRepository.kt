package com.example.senefavores.data.repository

import android.provider.SyncStateContract.Columns
import android.util.Log
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.Review
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import java.util.Objects.isNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavorRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun getFavors(userId: String?): List<Favor> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val query = supabaseClient.from("favors").select(
                    ) {
                    filter {
                        eq("status", "pending")
                        if (userId != null) {
                            neq("request_user_id", userId)
                        }
                    }
                }
                query.decodeList<Favor>()
            }.getOrElse {
                Log.e("FavorRepository", "Error fetching favors: ${it.localizedMessage}", it)
                emptyList()
            }
        }
    }


    suspend fun getAllFavors(): List<Favor> {
        return withContext(Dispatchers.IO) {
            runCatching {
                supabaseClient.from("favors").select().decodeList<Favor>()
            }.getOrElse {
                Log.e("FavorRepository", "Error fetching all favors: ${it.localizedMessage}", it)
                emptyList()
            }
        }
    }

    suspend fun addFavor(favor: Favor) {
        runCatching {
            supabaseClient
                .from("favors")
                .insert(favor)
        }.onFailure {
            Log.e("FavorRepository", "Error adding favor: ${it.localizedMessage}")
        }
    }

    suspend fun updateFavorAcceptUserId(favorId: String, userId: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                supabaseClient.from("favors").update(
                    { set("accept_user_id", userId)
                        set("status", "accepted")}
                ) {
                    filter { eq("id", favorId) }
                }
            }.onFailure {
                Log.e("FavorRepository", "Error updating favor $favorId: ${it.localizedMessage}", it)
                throw it // Propagate error to ViewModel
            }
        }
    }

    suspend fun updateFavorStatus(favorId: String, status: String) {
        try {
            supabaseClient.from("favors").update(
                {
                    set("status", status)
                }
            ) {
                filter {
                    eq("id", favorId)
                }
            }
            Log.d("FavorRepository", "Updated favor $favorId to status=$status")
        } catch (e: Exception) {
            Log.e("FavorRepository", "Error updating favor $favorId status: ${e.message}", e)
            throw e
        }
    }

    suspend fun getReviews(): List<Review> {
        return withContext(Dispatchers.IO) {
            runCatching {
                Log.d("FavorRepository", "Fetching all reviews")
                val reviews = supabaseClient.from("reviews").select().decodeList<Review>()
                Log.d("FavorRepository", "Fetched ${reviews.size} reviews")
                reviews
            }.getOrElse {
                Log.e("FavorRepository", "Error fetching reviews: ${it.localizedMessage}", it)
                emptyList()
            }
        }
    }

    suspend fun addReview(review: Review) {
        withContext(Dispatchers.IO) {
            runCatching {
                Log.d("FavorRepository", "Adding review: id=${review.id}, title=${review.title}, reviewer_id=${review.reviewer_id}, reviewed_id=${review.reviewed_id}")
                supabaseClient.from("reviews").insert(review)
                Log.d("FavorRepository", "Review added successfully")
            }.onFailure {
                Log.e("FavorRepository", "Error adding review: ${it.localizedMessage}, cause: ${it.cause}", it)
                throw it
            }
        }
    }
    suspend fun getReviewsByReviewedId(reviewedId: String): List<Review> = withContext(Dispatchers.IO) {
        try {
            val reviews = supabaseClient.postgrest["reviews"].select {
                filter {
                    eq("reviewed_id", reviewedId)
                }
            }.decodeList<Review>()
            Log.d("FavorRepository", "Fetched ${reviews.size} reviews for reviewed_id=$reviewedId")
            reviews
        } catch (e: Exception) {
            Log.e("FavorRepository", "Error fetching reviews for reviewed_id=$reviewedId: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun updateClientStars(clientId: String, stars: Float) = withContext(Dispatchers.IO) {
        try {
            supabaseClient.from("clients").update(
                {
                    set("stars", stars)
                }
            ) {
                filter {
                    eq("id", clientId)
                }
            }
            Log.d("FavorRepository", "Updated client stars: id=$clientId, stars=$stars")
        } catch (e: Exception) {
            Log.e("FavorRepository", "Error updating client stars for id=$clientId: ${e.message}", e)
            throw e
        }
    }
}