package com.example.senefavores.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun formatTime2(favorTime: String): String {
    val possibleFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    )

    for (formatter in possibleFormats) {
        try {
            val dateTime = LocalDateTime.parse(favorTime, formatter)
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: DateTimeParseException) {
            // Ignore and try the next format
        }
    }

    throw IllegalArgumentException("Invalid date format: $favorTime")
}

fun truncateText(text: String, maxLength: Int = 32): String {
    return if (text.length > maxLength) {
        text.take(maxLength) + "..."
    } else {
        text
    }
}