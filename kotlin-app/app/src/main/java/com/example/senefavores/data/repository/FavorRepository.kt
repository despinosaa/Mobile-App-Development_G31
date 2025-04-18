package com.example.senefavores.data.repository

import android.util.Log
import com.example.senefavores.data.model.Favor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
                        exact("accept_user_id", null)
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
                    { set("accept_user_id", userId) }
                ) {
                    filter { eq("id", favorId) }
                }
            }.onFailure {
                Log.e("FavorRepository", "Error updating favor $favorId: ${it.localizedMessage}", it)
                throw it // Propagate error to ViewModel
            }
        }
    }
}