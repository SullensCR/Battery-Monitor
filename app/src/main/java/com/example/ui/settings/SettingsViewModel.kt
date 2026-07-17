package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BatteryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: BatteryRepository) : ViewModel() {
    val isExpressiveModeEnabled: StateFlow<Boolean> = repository.isExpressiveModeEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
        
    val isDynamicColorsEnabled: StateFlow<Boolean> = repository.isDynamicColorsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isServiceEnabled: StateFlow<Boolean> = repository.isServiceEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeMode: StateFlow<String> = repository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "SYSTEM")

    val colorScheme: StateFlow<String> = repository.colorScheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "MATERIAL_3")

    fun setExpressiveMode(enabled: Boolean) {
        viewModelScope.launch { repository.setExpressiveMode(enabled) }
    }
    
    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch { repository.setDynamicColors(enabled) }
    }

    fun setServiceEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setServiceEnabled(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setColorScheme(scheme: String) {
        viewModelScope.launch { repository.setColorScheme(scheme) }
    }
}
