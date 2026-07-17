package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.BatteryMonitorApplication
import com.example.data.BatteryRepository

fun CreationExtras.batteryMonitorApplication(): BatteryMonitorApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as BatteryMonitorApplication)

class AppViewModelProvider {
    companion object {
        val Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val app = extras.batteryMonitorApplication()
                val repo = app.container.batteryRepository
                
                return when {
                    modelClass.isAssignableFrom(com.example.ui.home.HomeViewModel::class.java) -> {
                        com.example.ui.home.HomeViewModel(repo) as T
                    }
                    modelClass.isAssignableFrom(com.example.ui.stats.StatsViewModel::class.java) -> {
                        com.example.ui.stats.StatsViewModel(repo) as T
                    }
                    modelClass.isAssignableFrom(com.example.ui.history.HistoryViewModel::class.java) -> {
                        com.example.ui.history.HistoryViewModel(repo) as T
                    }
                    modelClass.isAssignableFrom(com.example.ui.settings.SettingsViewModel::class.java) -> {
                        com.example.ui.settings.SettingsViewModel(repo) as T
                    }
                    else -> throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}
