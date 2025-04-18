package com.example.senefavores.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Review(
    val id: String,
    val title: String,
    val description: String,
    val stars: Int,
    val reviewer_id: String,
    val reviewed_id: String,
    val created_at: String = ""
)