package com.example.senefavores.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Favor(
    val id: String,  // Default value 0
    val title: String,
    val description: String,
    val category: String,
    val reward: String,
    val favor_time: String = "",  // Default empty string
    val created_at: String = "",  // Default empty string
    val requested_user_id: String = "",  // Default null
    val accept_user_id: String? = ""  // Default null
)


{
    fun parsedFavorTime(): Instant = Instant.parse(favor_time)
    fun parsedCreatedAt(): Instant = Instant.parse(created_at)
}