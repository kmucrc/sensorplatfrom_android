package com.crc.sensorplatform.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Athletics::class], version = 1)
abstract class AthleticsDatabase : RoomDatabase() {
    abstract fun AthleticsDao(): AthleticsDao

    companion object {
        @Volatile
        private var INSTANCE: AthleticsDatabase? = null

        fun getInstance(context: Context): AthleticsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AthleticsDatabase::class.java,
                    "Athletics"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}