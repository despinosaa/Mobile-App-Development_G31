package com.example.senefavores.data.repository

import android.util.Log
import com.example.senefavores.data.model.Favor
import com.example.senefavores.data.remote.SupabaseManagement
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavorRepository @Inject constructor(private val supabaseClient: SupabaseManagement) {

    suspend fun getFavors(): List<Favor> {
        return withContext(Dispatchers.IO) {
            runCatching {
                supabaseClient.supabase
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
            supabaseClient.supabase
                .from("favors")
                .insert(favor)
        }.onFailure {
            println("Error adding favor: ${it.localizedMessage}")
        }
    }
}
