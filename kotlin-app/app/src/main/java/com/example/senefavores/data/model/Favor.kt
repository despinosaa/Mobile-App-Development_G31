package com.example.senefavores.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Favor(
    val id: String?,
    val title: String,
    val description: String,
    val category: String,
    val reward: Int,
    val favor_time: String? = "",
    val created_at: String = "",
    val request_user_id: String,
    val accept_user_id: String? = "",
    val latitude: Double?,
    val longitude: Double?
)

