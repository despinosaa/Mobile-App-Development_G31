package com.example.senefavores.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Favor(
    val title: String,
    val description: String,
    val category: String,
    val reward: Int,
    val favor_time: String? = "",  // Default empty string
    val created_at: String = "",  // Default empty string
    val request_user_id: String,  // Default null
    val accept_user_id: String? = ""  // Default null
)

