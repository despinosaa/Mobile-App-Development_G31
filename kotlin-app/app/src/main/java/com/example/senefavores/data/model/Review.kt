package com.example.senefavores.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val title: String,
    val description: String,
    val stars: String,
    val reviewer_id: Int,
    val reviewed_id: String? = "",
    val created_at: String = "",
)