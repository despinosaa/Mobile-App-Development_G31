package com.example.senefavores.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [FavorEntity::class, QueuedFavorEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class FavorDatabase : RoomDatabase() {
    abstract fun favorDao(): FavorDao

    //Michi
    abstract fun favorQueueDao(): FavorQueueDao

    companion object {
        @Volatile
        private var INSTANCE: FavorDatabase? = null

        fun getDatabase(context: Context): FavorDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavorDatabase::class.java,
                    "favor_database"
                )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}