package com.example.senefavores.data.local

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import com.example.senefavores.data.model.Favor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    private val json = Json { encodeDefaults = true }

    @TypeConverter
    fun fromFavor(favor: Favor): String {
        return json.encodeToString(favor)
    }

    @TypeConverter
    fun toFavor(favorString: String): Favor {
        return json.decodeFromString(favorString)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime): Long {
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDateTime(timestamp: Long): LocalDateTime {
        return LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp), ZoneOffset.UTC)
    }
}