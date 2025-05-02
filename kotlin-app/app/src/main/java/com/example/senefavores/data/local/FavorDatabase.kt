package com.example.senefavores.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [FavorEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FavorDatabase : RoomDatabase() {
    abstract fun favorDao(): FavorDao

    companion object {
        @Volatile
        private var INSTANCE: FavorDatabase? = null

        fun getDatabase(context: Context): FavorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavorDatabase::class.java,
                    "favor_database"
                ).fallbackToDestructiveMigration() // Allows destructive migration on version mismatch
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}