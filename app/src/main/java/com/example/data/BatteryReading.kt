package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_readings")
data class BatteryReading(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val level: Int, // 0-100
    val isCharging: Boolean,
    val currentMah: Int, // e.g. 1850 mA
    val voltageMv: Int,
    val temperatureDeciC: Int, // tenths of a degree C
    val status: Int,
    val plugType: Int,
    val health: Int,
    val technology: String?
)
