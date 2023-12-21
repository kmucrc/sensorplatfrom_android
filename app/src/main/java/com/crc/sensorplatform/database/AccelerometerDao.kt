package com.crc.sensorplatform.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AccelerometerDao {
    @Query("SELECT * FROM Accelerometer WHERE createdTime BETWEEN :startTime AND :startTime + 180000") // 3 minutes = 180000 milliseconds
    fun getAll(startTime: Long): List<Accelerometer>

    @Insert
    fun insertAll(vararg accelerometers: Accelerometer)
}