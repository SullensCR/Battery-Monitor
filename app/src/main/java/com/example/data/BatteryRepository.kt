package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

class BatteryRepository(
    private val batteryDao: BatteryDao,
    private val dataStore: DataStore<Preferences>,
    private val context: Context
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO)

    val liveBatteryData: Flow<BatteryReading> = getLiveBatteryData(context)
        .shareIn(repositoryScope, SharingStarted.WhileSubscribed(5000), 1)

    val recentReadings: Flow<List<BatteryReading>> = batteryDao.getRecentReadings()
    val allSessions: Flow<List<ChargingSession>> = batteryDao.getAllSessions()
    val lifetimeStats: Flow<LifetimeStats?> = batteryDao.getLifetimeStats()

    private object PreferencesKeys {
        val EXPRESSIVE_MODE = booleanPreferencesKey("expressive_mode")
        val SERVICE_ENABLED = booleanPreferencesKey("service_enabled")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val COLOR_SCHEME = stringPreferencesKey("color_scheme")
    }

    val isExpressiveModeEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.EXPRESSIVE_MODE] ?: true
    }
    
    val isDynamicColorsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLORS] ?: true
    }

    val isServiceEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SERVICE_ENABLED] ?: false
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
    }

    val colorScheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.COLOR_SCHEME] ?: "MATERIAL_3"
    }

    suspend fun setExpressiveMode(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.EXPRESSIVE_MODE] = enabled }
    }
    
    suspend fun setDynamicColors(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DYNAMIC_COLORS] = enabled }
    }

    suspend fun setServiceEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.SERVICE_ENABLED] = enabled }
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode }
    }

    suspend fun setColorScheme(scheme: String) {
        dataStore.edit { it[PreferencesKeys.COLOR_SCHEME] = scheme }
    }

    suspend fun insertReading(reading: BatteryReading) = batteryDao.insertReading(reading)
    
    suspend fun insertSession(session: ChargingSession) = batteryDao.insertSession(session)
    
    suspend fun updateSession(session: ChargingSession) = batteryDao.updateSession(session)
    
    suspend fun getLatestSession() = batteryDao.getLatestSession()

    suspend fun updateChargingSession(reading: BatteryReading) {
        val latestSession = getLatestSession()

        if (reading.isCharging) {
            if (latestSession == null || latestSession.endTime != null) {
                // Start a new session
                insertSession(
                    ChargingSession(
                        startLevel = reading.level,
                        maxCurrentMah = reading.currentMah,
                        minCurrentMah = reading.currentMah,
                        maxVoltageMv = reading.voltageMv,
                        maxTempDeciC = reading.temperatureDeciC,
                        avgCurrentMah = reading.currentMah,
                        plugType = reading.plugType
                    )
                )
            } else {
                // Update ongoing session
                val count = latestSession.readingCount + 1
                val newAvg = ((latestSession.avgCurrentMah * latestSession.readingCount) + reading.currentMah) / count
                
                val updatedSession = latestSession.copy(
                    maxCurrentMah = maxOf(latestSession.maxCurrentMah, reading.currentMah),
                    minCurrentMah = minOf(latestSession.minCurrentMah, reading.currentMah),
                    maxVoltageMv = maxOf(latestSession.maxVoltageMv, reading.voltageMv),
                    maxTempDeciC = maxOf(latestSession.maxTempDeciC, reading.temperatureDeciC),
                    avgCurrentMah = newAvg,
                    readingCount = count
                )
                updateSession(updatedSession)
            }
        } else {
            if (latestSession != null && latestSession.endTime == null) {
                // End current session
                updateSession(
                    latestSession.copy(
                        endTime = System.currentTimeMillis(),
                        endLevel = reading.level
                    )
                )
            }
        }
    }

    suspend fun updateLifetimeStats(reading: BatteryReading) {
        // Complex logic to safely update lifetime stats... for now just stub or simple update
        // We will fetch current stats, compare, update
        // (In a real scenario, this runs in a transaction or Mutex, but Room handles basic concurrency)
    }
}
