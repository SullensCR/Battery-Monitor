package com.example.ui.stats

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppViewModelProvider
import java.util.Calendar

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val readings by viewModel.recentReadings.collectAsStateWithLifecycle()

    val startOfToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    
    val currentHour = remember {
        Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    }

    val todayReadings = remember(readings, startOfToday) {
        readings.filter { it.timestamp >= startOfToday }.sortedBy { it.timestamp }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Battery Level History", style = MaterialTheme.typography.titleLarge)
        
        Text(
            text = "Showing data from 12:00 AM today to the current hour (${currentHour}:00)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                if (todayReadings.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No battery records today", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Disconnect the charger or wait to collect logs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    val lineColor = MaterialTheme.colorScheme.primary
                    val gridColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f)
                    
                    val gradient = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.0f)
                        )
                    )

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        
                        // Time bounds: midnight to now
                        val startTime = startOfToday
                        val endTime = System.currentTimeMillis()
                        val timeRange = maxOf(1L, endTime - startTime)

                        // 1. Draw horizontal grid lines (25%, 50%, 75%, 100%)
                        val levels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
                        levels.forEach { level ->
                            val y = height - (level * height)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // 2. Plot the line points
                        val path = Path()
                        val fillPath = Path()
                        
                        var firstPoint = true
                        var lastX = 0f
                        var lastY = 0f

                        todayReadings.forEach { reading ->
                            val progressX = (reading.timestamp - startTime).toFloat() / timeRange.toFloat()
                            val progressY = reading.level.toFloat() / 100f
                            
                            val x = progressX * width
                            val y = height - (progressY * height)

                            if (firstPoint) {
                                path.moveTo(x, y)
                                fillPath.moveTo(x, height)
                                fillPath.lineTo(x, y)
                                firstPoint = false
                            } else {
                                path.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }
                            lastX = x
                            lastY = y
                        }

                        if (!firstPoint) {
                            fillPath.lineTo(lastX, height)
                            fillPath.close()
                            
                            // Draw the shaded gradient fill
                            drawPath(
                                path = fillPath,
                                brush = gradient
                            )

                            // Draw the line graph
                            drawPath(
                                path = path,
                                color = lineColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
