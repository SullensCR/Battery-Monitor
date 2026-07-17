package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "charging_sessions")
data class ChargingSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val startLevel: Int,
    val endLevel: Int? = null,
    val maxCurrentMah: Int = 0,
    val minCurrentMah: Int = 0,
    val maxVoltageMv: Int = 0,
    val maxTempDeciC: Int = 0,
    val avgCurrentMah: Int = 0,
    val readingCount: Int = 1,
    val plugType: Int = 0
) {
    val durationMs: Long
        get() = (endTime ?: System.currentTimeMillis()) - startTime
}
