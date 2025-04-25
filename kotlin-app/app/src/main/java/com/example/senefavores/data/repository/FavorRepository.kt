package com.example.senefavores.data.repository

<<<<<<< Updated upstream
=======
import android.os.Build
>>>>>>> Stashed changes
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.Review
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
<<<<<<< Updated upstream
=======
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
>>>>>>> Stashed changes
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavorRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun getFavors(userId: String?, offset: Int = 0, limit: Int = 100): List<Favor> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val query = supabaseClient.from("favors").select {
                    filter {
                        eq("status", "pending")
                        if (userId != null) {
                            neq("request_user_id", userId)
                        }
                    }
                    order("created_at", order = Order.DESCENDING)
<<<<<<< Updated upstream
                    range(offset.toLong(), (offset + limit - 1).toLong()) // Convert Int to Long
=======
                    range(offset.toLong(), (offset + limit - 1).toLong())
>>>>>>> Stashed changes
                }
                val favors = query.decodeList<Favor>()
                Log.d("FavorRepository", "Fetched favors: $favors")
                favors
            }.getOrElse {
                Log.e("FavorRepository", "Error fetching favors: ${it.localizedMessage}", it)
                emptyList()
            }
        }
    }

    suspend fun getFavorById(favorId: String): Favor? {
        return try {
            val favor = supabaseClient
                .from("favors")
                .select(
                    columns = Columns.list(
                        "id",
                        "title",
                        "description",
                        "category",
                        "reward",
                        "favor_time",
                        "created_at",
                        "request_user_id",
                        "accept_user_id",
                        "accepted_at",
                        "latitude",
                        "longitude",
                        "status"
                    )
                ) {
                    filter {
                        eq("id", favorId)
                    }
                }
                .decodeSingle<Favor>()
            Log.d("FavorRepository", "Fetched favor: $favor")
            favor
        } catch (e: Exception) {
<<<<<<< Updated upstream
            Log.e("FavorViewModel", "Error fetching favor: ${e.localizedMessage}", e) // Fixed 'it' to 'e'
=======
            Log.e("FavorRepository", "Error fetching favor: ${e.localizedMessage}", e)
>>>>>>> Stashed changes
            null
        }
    }

    suspend fun getAllFavors(): List<Favor> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val favors = supabaseClient.from("favors").select().decodeList<Favor>()
                Log.d("FavorRepository", "Fetched all favors: $favors")
                favors
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
<<<<<<< Updated upstream
=======
            Log.d("FavorRepository", "Added favor: $favor")
>>>>>>> Stashed changes
        }.onFailure {
            Log.e("FavorRepository", "Error adding favor: ${it.localizedMessage}", it)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun updateFavorAcceptUserId(favorId: String, userId: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                val currentTime = LocalDateTime.now(ZoneId.of("UTC"))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                supabaseClient.from("favors").update(
                    {
                        set("accept_user_id", userId)
                        set("status", "accepted")
<<<<<<< Updated upstream
=======
                        set("accepted_at", currentTime)
>>>>>>> Stashed changes
                    }
                ) {
                    filter { eq("id", favorId) }
                }
                Log.d("FavorRepository", "Favor $favorId accepted by user $userId at $currentTime")
            }.onFailure {
                Log.e("FavorRepository", "Error updating favor $favorId: ${it.localizedMessage}", it)
                throw it
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