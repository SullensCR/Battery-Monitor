package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BatteryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: BatteryReading)

    @Query("SELECT * FROM battery_readings ORDER BY timestamp DESC LIMIT 1000")
    fun getRecentReadings(): Flow<List<BatteryReading>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChargingSession): Long

    @Update
    suspend fun updateSession(session: ChargingSession)

    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<ChargingSession>>

    @Query("SELECT * FROM charging_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Int): ChargingSession?
    
    @Query("SELECT * FROM charging_sessions ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestSession(): ChargingSession?

    @Query("SELECT * FROM lifetime_stats WHERE id = 1 LIMIT 1")
    fun getLifetimeStats(): Flow<LifetimeStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLifetimeStats(stats: LifetimeStats)
}
