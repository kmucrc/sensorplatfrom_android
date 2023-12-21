package com.crc.sensorplatform.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Accelerometer(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val createdTime: Long,
    val accelX: Float,
    val accelY: Float,
    val accelZ: Float,
    val accelXp: Float,
    val accelYp: Float,
    val accelZp: Float
)
