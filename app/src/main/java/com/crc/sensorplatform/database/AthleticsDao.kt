package com.crc.sensorplatform.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AthleticsDao {
    @Query("SELECT * FROM Athletics WHERE createdTime BETWEEN :startTime AND :startTime + 180000") // 3 minutes = 180000 milliseconds
    fun getAll(startTime: Long): List<Athletics>

    @Insert
    fun insertAll(vararg Athleticses: Athletics)
}