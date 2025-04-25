package com.example.senefavores.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Favor(
    val id: String? = null,
    val title: String,
    val description: String,
    val category: String,
    val reward: Int,
    val favor_time: String? = null,
    val created_at: String? = null,
    val request_user_id: String,
    val accept_user_id: String? = null,
    val accepted_at: String? = null,
    val latitude: Double?,
    val longitude: Double?,
    val status: String? = "pending"
)