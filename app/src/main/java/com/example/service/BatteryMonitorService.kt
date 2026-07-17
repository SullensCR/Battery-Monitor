package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.BatteryMonitorApplication
import com.example.MainActivity
import com.example.R
import com.example.data.BatteryReading
import com.example.data.ChargingSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

class BatteryMonitorService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                handleBatteryIntent(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Monitoring battery state..."))
        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        serviceJob.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun handleBatteryIntent(intent: Intent) {
        val reading = com.example.data.intentToBatteryReading(intent, this)

        serviceScope.launch {
            val repository = (application as BatteryMonitorApplication).container.batteryRepository
            repository.insertReading(reading)
            updateChargingSession(reading)
            
            // Update notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val text = "${reading.level}% | ${if (reading.isCharging) "Charging" else "Discharging"} | ${reading.temperatureDeciC / 10f}°C | ${reading.currentMah} mA"
            notificationManager.notify(NOTIFICATION_ID, createNotification(text))
        }
    }

    private suspend fun updateChargingSession(reading: BatteryReading) {
        val repository = (application as BatteryMonitorApplication).container.batteryRepository
        val latestSession = repository.getLatestSession()

        if (reading.isCharging) {
            if (latestSession == null || latestSession.endTime != null) {
                // Start a new session
                repository.insertSession(
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
                repository.updateSession(updatedSession)
            }
        } else {
            if (latestSession != null && latestSession.endTime == null) {
                // End current session
                repository.updateSession(
                    latestSession.copy(
                        endTime = System.currentTimeMillis(),
                        endLevel = reading.level
                    )
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Battery Monitor Service",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Runs in the background to collect battery statistics."
            channel.setShowBadge(false)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Battery Monitor Active")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "battery_monitor_channel"
        private const val NOTIFICATION_ID = 101
    }
}
