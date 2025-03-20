package com.example.senefavores.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val name: String? = "",
    val email: String,
    val phone: String? = "",
    val profilePic: String? = "",
    val stars: Float? = 5.0f
)