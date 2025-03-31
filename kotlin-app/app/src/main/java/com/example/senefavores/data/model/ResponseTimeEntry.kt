package com.example.senefavores.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ResponseTimeEntry(
    val screen: String,
    val response_time: Int,
    val device: String,
    val os_version: String
)