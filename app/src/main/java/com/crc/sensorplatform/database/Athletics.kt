package com.crc.sensorplatform.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Athletics(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val createdTime: Long,
    val fBodyTemp: Float,
    val fTemp: Float,
    val fHumi: Float,
    val fHba1c: Float,
    val nSpo2: Int,
    val nHeartRate: Int
)