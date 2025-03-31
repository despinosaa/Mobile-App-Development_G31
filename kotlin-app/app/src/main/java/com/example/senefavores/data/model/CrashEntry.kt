package com.example.senefavores.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CrashEntry(
    val screen: String,
    val crash_info: String
)