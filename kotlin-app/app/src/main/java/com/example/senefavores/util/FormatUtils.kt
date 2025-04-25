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
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
    )

    for (formatter in possibleFormats) {
        try {
            return LocalDateTime.parse(dateTimeStr, formatter)
        } catch (e: DateTimeParseException) {
            // Ignore and try the next format
        }
    }

    throw IllegalArgumentException("Invalid date format: $dateTimeStr")
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatTime2(favorTime: String): String {
    val dateTime = parseDateTime(favorTime)
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDateTimeFull(dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    return dateTime.format(formatter)
}

fun truncateText(text: String, maxLength: Int = 32): String {
    return if (text.length > maxLength) {
        text.take(maxLength) + "..."
    } else {
        text
    }
}