package com.example.senefavores.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
fun parseDateTime(dateTimeStr: String): LocalDateTime {
    val possibleFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // Added for timestamp without T
        DateTimeFormatter.ISO_DATE_TIME // Handles formats like 2025-04-25T12:34:56+00:00
    )
    for (formatter in possibleFormats) {
        try {
            return LocalDateTime.parse(dateTimeStr, formatter)
        } catch (e: DateTimeParseException) {
            // Try the next format
        }
    }
    throw IllegalArgumentException("Invalid date format: $dateTimeStr")
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime2(favorTime: String): String {
    val possibleFormats = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // Added for timestamp without T
        DateTimeFormatter.ISO_DATE_TIME // Handles formats like 2025-04-25T12:34:56+00:00
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