package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lifetime_stats")
data class LifetimeStats(
    @PrimaryKey val id: Int = 1,
    val highestCurrentMah: Int = 0,
    val lowestCurrentMah: Int = 0,
    val highestTempDeciC: Int = 0,
    val lowestTempDeciC: Int = 0,
    val highestVoltageMv: Int = 0,
    val lowestVoltageMv: Int = 0,
    val totalSessions: Int = 0,
    val totalWiredSessions: Int = 0,
    val totalWirelessSessions: Int = 0,
    val totalChargingMs: Long = 0L
)
