package com.example.senefavores.data.repository

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.room.withTransaction
import com.example.senefavores.data.local.FavorDao
import com.example.senefavores.data.local.FavorQueueDao
import com.example.senefavores.data.local.FavorEntity
import com.example.senefavores.data.local.QueuedFavorEntity
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.model.Review
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavorRepository @Inject constructor(
    private val supabaseClient: SupabaseClient,
    private val favorDao: FavorDao,
    private val favorQueueDao: FavorQueueDao
) {

    suspend fun getFavors(userId: String?, offset: Int = 0, limit: Int = 100): List<Favor> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val query = supabaseClient.from("favors").select {
                    filter {
                        eq("status", "pending") // Ensure status is non-null
                        if (userId != null) {
                            neq("request_user_id", userId)
                        }
                    }
                    order("created_at", order = Order.DESCENDING)
                    range(offset.toLong(), (offset + limit - 1).toLong())
                }
                val favors = query.decodeList<Favor>().filter { it.id != null && it.status != null }
                favorDao.insertAll(favors.map { favor -> FavorEntity(favor.id ?: "", favor, getCurrentTime()) })
                Log.d("FavorRepository", "Fetched and synced favors: $favors")
                favors
            }.getOrElse {
                Log.e("FavorRepository", "Error fetching favors: ${it.localizedMessage}", it)
                favorDao.getAllFavors().map { it.favor }
            }
        }
    }

    suspend fun getFavorById(favorId: String): Favor? {
        return withContext(Dispatchers.IO) {
            favorDao.getFavorById(favorId)?.favor ?: run {
                try {
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
                    if (favor.id == null || favor.status == null) {
                        Log.w("FavorRepository", "Favor with id $favorId has null id or status")
                        null
                    } else {
                        favorDao.insert(FavorEntity(favor.id, favor, getCurrentTime()))
                        Log.d("FavorRepository", "Fetched and synced favor: $favor")
                        favor
                    }
                } catch (e: Exception) {
                    Log.e("FavorRepository", "Error fetching favor: ${e.localizedMessage}", e)
                    null
                }
            }
        }
    }

    suspend fun getAllFavors(): List<Favor> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val favors = supabaseClient.from("favors").select().decodeList<Favor>().filter { it.id != null && it.status != null }
                favorDao.insertAll(favors.map { favor -> FavorEntity(favor.id ?: "", favor, getCurrentTime()) })
                Log.d("FavorRepository", "Fetched and synced all favors: $favors")
                favors
            }.getOrElse {
                Log.e("FavorRepository", "Error fetching all favors: ${it.localizedMessage}", it)
                favorDao.getAllFavors().map { it.favor }
            }
        }
    }

    suspend fun addFavor(favor: Favor) {
        withContext(Dispatchers.IO) {
            runCatching {
                val validFavor = favor.copy(id = favor.id ?: UUID.randomUUID().toString(), status = favor.status ?: "pending")
                supabaseClient.from("favors").insert(validFavor)
                favorDao.insert(FavorEntity(validFavor.id!!, validFavor, getCurrentTime()))
                Log.d("FavorRepository", "Added and synced favor: $validFavor")
            }.onFailure {
                Log.e("FavorRepository", "Error adding favor: ${it.localizedMessage}", it)
            }
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
                        set("accepted_at", currentTime)
                    }
                ) {
                    filter { eq("id", favorId) }
                }
                val favor = getFavorById(favorId)
                favor?.let {
                    favorDao.insert(FavorEntity(it.id!!, it, getCurrentTime()))
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
            val favor = getFavorById(favorId)
            favor?.let {
                favorDao.insert(FavorEntity(it.id!!, it, getCurrentTime()))
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
                val validReview = if (review.favor_id == null) {
                    Log.w("FavorRepository", "favor_id is null in review: $review, setting to empty string")
                    review.copy(favor_id = "") // Use copy to create a new instance
                } else {
                    review
                }
                Log.d("FavorRepository", "Adding review: id=${validReview.id}, favor_id=${validReview.favor_id}, title=${validReview.title}, reviewer_id=${validReview.reviewer_id}, reviewed_id=${validReview.reviewed_id}")
                supabaseClient.from("reviews").insert(validReview)
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

    fun getLocalFavors(): Flow<List<Favor>> {
        return favorDao.getAllFavorsFlow().map { entities -> entities.map { it.favor } }
    }

    // Helper function updated to return LocalDateTime with API compatibility
    @RequiresApi(Build.VERSION_CODES.O)
    @Suppress("DEPRECATION")
    private fun getCurrentTime(): LocalDateTime {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now(ZoneId.of("UTC"))
        } else {
            // Fallback for API < 26
            val calendar = Calendar.getInstance()
            LocalDateTime.of(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND)
            )
        }
    }

    //Michi
    suspend fun enqueueFavor(favor: Favor) {
        Log.d("FavorRepository", "pre $favor")
        val validFavor = favor.copy(id = favor.id ?: UUID.randomUUID().toString(), status = favor.status ?: "pending")
        val queuedFavor = QueuedFavorEntity(validFavor.id!!, validFavor, System.currentTimeMillis())
        Log.d("FavorRepository", "post $favor")
        favorQueueDao.enqueueFavor(queuedFavor)
        Log.d("FavorRepository", "Favor enqueued: ${favor}")
    }

    suspend fun getQueuedFavors(): List<QueuedFavorEntity> {
        return favorQueueDao.getAllQueuedFavors()
    }

    suspend fun removeQueuedFavor(queuedFavor: QueuedFavorEntity) {
        favorQueueDao.removeFavorFromQueue(queuedFavor)
        Log.d("FavorRepository", "Favor removed from queue: ${queuedFavor.favor.id}")
    }

    suspend fun processQueuedFavors() {
        val currentTime = System.currentTimeMillis()
        val queuedFavors = getQueuedFavors()

        if (queuedFavors.isEmpty()) {
            Log.d("FavorRepository", "No queued favors to process")
            return  // Exit early if there are no queued favors
        }

        for (queued in queuedFavors) {
            if (currentTime - queued.enqueuedAt <= 5 * 60_000) {
                // Check de los 5 minutos porque o si no chistoso
                addFavor(queued.favor)
                removeQueuedFavor(queued)
            } else {
                removeQueuedFavor(queued)
                Log.d("FavorRepository", "Favor ${queued.favor.id} discarded due to timeout")
            }
        }
        emptyQueue()
    }

    suspend fun emptyQueue() {
        favorQueueDao.clearQueue()
    }
}