package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.BatteryDao
import com.example.data.BatteryRepository
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class AppContainer(private val context: Context) {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "battery_monitor_db"
        ).build()
    }

    val batteryRepository: BatteryRepository by lazy {
        BatteryRepository(database.batteryDao(), context.dataStore, context)
    }
}
