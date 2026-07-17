package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [BatteryReading::class, ChargingSession::class, LifetimeStats::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao
}
