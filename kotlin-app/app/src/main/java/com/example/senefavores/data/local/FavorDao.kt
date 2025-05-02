package com.example.senefavores.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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