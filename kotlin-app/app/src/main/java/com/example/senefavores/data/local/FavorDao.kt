package com.example.senefavores.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import androidx.room.Delete


@Dao
interface FavorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favor: FavorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favors: List<FavorEntity>)

    @Query("SELECT * FROM favors ORDER BY syncedAt DESC")
    fun getAllFavorsFlow(): Flow<List<FavorEntity>>

    @Query("SELECT * FROM favors WHERE id = :id")
    suspend fun getFavorById(id: String): FavorEntity?

    @Query("SELECT * FROM favors")
    suspend fun getAllFavors(): List<FavorEntity>
}

//Michi
@Dao
interface FavorQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueFavor(queuedFavor: QueuedFavorEntity)

    @Query("SELECT * FROM favor_queue")
    suspend fun getAllQueuedFavors(): List<QueuedFavorEntity>

    @Delete
    suspend fun removeFavorFromQueue(queuedFavor: QueuedFavorEntity)

    @Query("DELETE FROM favor_queue")
    suspend fun clearQueue()
}