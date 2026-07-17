package com.example.ui.home

import android.content.Context
import android.os.BatteryManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModelProvider
import com.example.data.BatteryReading

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val reading by viewModel.currentReading.collectAsStateWithLifecycle()

    Surface(modifier = modifier.fillMaxSize()) {
        if (reading == null) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BatteryHeroCard(reading = reading!!)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        title = "Temperature",
                        value = "${reading!!.temperatureDeciC / 10f}°C",
                        icon = Icons.Default.DeviceThermostat
                    )
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        title = "Voltage",
                        value = "${reading!!.voltageMv / 1000f} V",
                        icon = Icons.Default.ElectricBolt
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        title = "Health",
                        value = getHealthString(reading!!.health),
                        icon = Icons.Default.BatteryFull
                    )
                    InfoCard(
                        modifier = Modifier.weight(1f),
                        title = "Chemistry",
                        value = reading!!.technology ?: "Unknown",
                        icon = Icons.Default.BatteryFull
                    )
                }
            }
        }
    }
}

@Composable
fun BatteryHeroCard(reading: BatteryReading) {
    val progressTarget = reading.level / 100f
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "CircularProgressAnimation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "ChargingBreathing")
    val breathingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingAlpha"
    )

    val chargingColor = MaterialTheme.colorScheme.primary
    val displayColor = MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    strokeWidth = 12.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedNumericValue(
                        value = "${reading.level}%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = displayColor
                    )
                    if (reading.isCharging) {
                        Text(
                            text = "Charging",
                            style = MaterialTheme.typography.titleMedium,
                            color = chargingColor.copy(alpha = breathingAlpha)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.size(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Current", style = MaterialTheme.typography.labelMedium)
                    AnimatedNumericValue(
                        value = "${reading.currentMah} mA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Power", style = MaterialTheme.typography.labelMedium)
                    val watts = (reading.voltageMv / 1000f) * (reading.currentMah / 1000f)
                    AnimatedNumericValue(
                        value = "${String.format("%.1f", kotlin.math.abs(watts))} W",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            // Real-time capacity loading bar
            BatteryCapacityBar(reading = reading)
        }
    }
}

fun getBatteryCapacityMah(context: Context): Double {
    var capacity = 0.0
    try {
        val powerProfileClass = Class.forName("com.android.internal.os.PowerProfile")
        val powerProfile = powerProfileClass.getConstructor(Context::class.java).newInstance(context)
        val batteryCapacity = powerProfileClass.getMethod("getBatteryCapacity").invoke(powerProfile)
        if (batteryCapacity is Double) {
            capacity = batteryCapacity
        } else if (batteryCapacity is Float) {
            capacity = batteryCapacity.toDouble()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    
    if (capacity <= 0) {
        // Fallback to a typical smartphone battery capacity if reflection fails
        capacity = 4000.0
    }
    return capacity
}

fun getCurrentCapacityMah(context: Context, level: Int, maxCapacity: Double): Double {
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val chargeCounterUah = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
    val chargeCounterMah = if (chargeCounterUah > 0) chargeCounterUah / 1000.0 else 0.0
    
    // Some devices report charge counter as positive, some might not support it or report empty.
    // If charge counter is extremely low, invalid, or larger than design capacity, fallback to calculating via percentage.
    return if (chargeCounterMah in 1.0..(maxCapacity * 1.1)) {
        chargeCounterMah
    } else {
        (maxCapacity * level) / 100.0
    }
}

@Composable
fun BatteryCapacityBar(reading: BatteryReading) {
    val context = LocalContext.current
    val maxCapacity = remember(context) { getBatteryCapacityMah(context) }
    val currentCapacity = getCurrentCapacityMah(context, reading.level, maxCapacity)
    
    val progress = (currentCapacity / maxCapacity).coerceIn(0.0, 1.0).toFloat()
    val animatedBarProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "CapacityBarAnimation"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.material3.HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Charge Capacity",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            AnimatedNumericValue(
                value = "${String.format("%,.0f", currentCapacity)} / ${String.format("%,.0f", maxCapacity)} mAh",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        // Beautiful modern linear loading bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedBarProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    )
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedNumericValue(
                value = "${String.format("%.1f", progress * 100)}% Capacity",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
            Text(
                text = "Real-time",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun InfoCard(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(text = title, style = MaterialTheme.typography.labelMedium)
            AnimatedNumericValue(
                value = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun getHealthString(healthValue: Int): String {
    return when(healthValue) {
        android.os.BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        android.os.BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        android.os.BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        android.os.BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
        android.os.BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
        android.os.BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }
}

@Composable
fun AnimatedNumericValue(
    value: String,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified
) {
    var previousNumeric by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(0.0) }
    val currentNumeric = remember(value) {
        value.filter { it.isDigit() || it == '.' || it == '-' }.toDoubleOrNull() ?: 0.0
    }
    
    val isIncrement = currentNumeric >= previousNumeric
    
    androidx.compose.runtime.SideEffect {
        previousNumeric = currentNumeric
    }

    AnimatedContent(
        targetState = value,
        transitionSpec = {
            if (isIncrement) {
                (slideInVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) { it } + fadeIn(animationSpec = tween(200)))
                    .togetherWith(slideOutVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) { -it } + fadeOut(animationSpec = tween(200)))
                    .using(SizeTransform(clip = false))
            } else {
                (slideInVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) { -it } + fadeIn(animationSpec = tween(200)))
                    .togetherWith(slideOutVertically(animationSpec = tween(400, easing = FastOutSlowInEasing)) { it } + fadeOut(animationSpec = tween(200)))
                    .using(SizeTransform(clip = false))
            }
        },
        label = "NumericValueTransition",
        modifier = modifier
    ) { targetValue ->
        Text(
            text = targetValue,
            style = style,
            fontWeight = fontWeight,
            color = color
        )
    }
}
