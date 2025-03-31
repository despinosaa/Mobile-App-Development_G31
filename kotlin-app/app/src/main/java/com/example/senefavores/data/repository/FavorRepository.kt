package com.example.senefavores.data.repository

import android.util.Log
import com.example.senefavores.data.model.Favor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavorRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {

    suspend fun getFavors(): List<Favor> {
        return withContext(Dispatchers.IO) {
            runCatching {
                supabaseClient
                    .from("favors")
                    .select()
                    .decodeList<Favor>()
            }.getOrElse {
                Log.e("FavorRepository", "Error fetching favors: ${it.localizedMessage}", it)
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
}