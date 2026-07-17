package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

fun intentToBatteryReading(intent: Intent, context: Context): BatteryReading {
    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val percentage = if (level != -1 && scale != -1) (level * 100) / scale else 0
    
    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    val plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
    val voltageMv = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
    val temperatureDeciC = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
    val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
    val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
    
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val currentUa = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
    val currentMah = if (abs(currentUa) > 10000) currentUa / 1000 else currentUa

    return BatteryReading(
        level = percentage,
        isCharging = isCharging,
        currentMah = currentMah,
        voltageMv = voltageMv,
        temperatureDeciC = temperatureDeciC,
        status = status,
        plugType = plugType,
        health = health,
        technology = technology
    )
}

fun getLiveBatteryData(context: Context): Flow<BatteryReading> = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                trySend(intentToBatteryReading(intent, context))
            }
        }
    }
    val intent = context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    intent?.let { trySend(intentToBatteryReading(it, context)) }

    // Start a periodic timer to update every second (1000ms)
    val timerJob = launch {
        while (isActive) {
            delay(1000L)
            val currentIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            currentIntent?.let {
                trySend(intentToBatteryReading(it, context))
            }
        }
    }

    awaitClose {
        context.unregisterReceiver(receiver)
        timerJob.cancel()
    }
}
