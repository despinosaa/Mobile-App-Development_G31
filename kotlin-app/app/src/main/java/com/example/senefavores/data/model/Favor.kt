package com.example.senefavores.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Favor(
    val id: Int,  // Default value 0
    val title: String,
    val description: String,
    val category: String,
    val reward: Int,
    val favor_time: String = "",  // Default empty string
    val created_at: String = "",  // Default empty string
    val requested_user_id: Int? = null,  // Default null
    val accept_user_id: Int? = null  // Default null
)


{
    fun parsedFavorTime(): Instant = Instant.parse(favor_time)
    fun parsedCreatedAt(): Instant = Instant.parse(created_at)
}