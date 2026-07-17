package com.example.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChargingSession
import com.example.ui.AppViewModelProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Charging Sessions", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
        }
        
        items(sessions) { session ->
            SessionCard(session)
        }
        
        if (sessions.isEmpty()) {
            item {
                Text("No charging sessions recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SessionCard(session: ChargingSession) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { /* TODO: show details */ },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            Text(text = dateFormat.format(Date(session.startTime)), style = MaterialTheme.typography.labelMedium)
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Level", style = MaterialTheme.typography.labelSmall)
                    Text("${session.startLevel}% → ${session.endLevel ?: "Ongoing"}", fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Duration", style = MaterialTheme.typography.labelSmall)
                    val durationMins = session.durationMs / 60000
                    Text("$durationMins mins", fontWeight = FontWeight.Bold)
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Max Current", style = MaterialTheme.typography.labelSmall)
                    Text("${session.maxCurrentMah} mA")
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Max Temp", style = MaterialTheme.typography.labelSmall)
                    Text("${session.maxTempDeciC / 10f}°C")
                }
            }
        }
    }
}
