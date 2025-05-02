package com.example.senefavores.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.senefavores.data.model.Favor
import java.time.LocalDateTime

@Entity(tableName = "favors")
data class FavorEntity(
    @PrimaryKey val id: String,
    val favor: Favor,
    val syncedAt: LocalDateTime
)