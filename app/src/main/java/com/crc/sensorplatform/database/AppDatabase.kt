package com.crc.sensorplatform.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Accelerometer::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accelerometerDao(): AccelerometerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "Accelerometer"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}