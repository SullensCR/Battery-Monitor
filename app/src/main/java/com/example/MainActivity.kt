package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.service.BatteryMonitorService
import com.example.ui.history.HistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.History
import com.example.ui.navigation.Home
import com.example.ui.navigation.Settings
import com.example.ui.navigation.Statistics
import com.example.ui.settings.SettingsScreen
import com.example.ui.stats.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permission results if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }

        // Dynamically listen to service state toggle
        lifecycleScope.launch {
            val app = application as BatteryMonitorApplication
            app.container.batteryRepository.isServiceEnabled.collectLatest { isEnabled ->
                val serviceIntent = Intent(this@MainActivity, BatteryMonitorService::class.java)
                if (isEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(serviceIntent)
                    } else {
                        startService(serviceIntent)
                    }
                } else {
                    stopService(serviceIntent)
                }
            }
        }

        setContent {
            val app = application as BatteryMonitorApplication
            val repository = app.container.batteryRepository
            
            val themeMode by repository.themeMode.collectAsState(initial = "SYSTEM")
            val colorSchemePref by repository.colorScheme.collectAsState(initial = "MATERIAL_3")
            
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> systemDark
            }

            MyApplicationTheme(
                darkTheme = darkTheme,
                colorSchemeName = colorSchemePref
            ) {
                val navController = rememberNavController()
                var selectedItem by rememberSaveable { mutableIntStateOf(0) }

                Box(modifier = Modifier.fillMaxSize()) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = Home,
                            modifier = Modifier.padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = 96.dp // Generous space to fully accommodate floating bar without overlaps
                            )
                        ) {
                            composable<Home> { HomeScreen() }
                            composable<Statistics> { StatsScreen() }
                            composable<History> { HistoryScreen() }
                            composable<Settings> { SettingsScreen() }
                        }
                    }

                    // Smooth animated floating navigation bar
                    var isVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        isVisible = true
                    }

                    AnimatedVisibility(
                        visible = isVisible,
                        enter = fadeIn(animationSpec = tween(500)) + slideInVertically(
                            initialOffsetY = { it / 2 },
                            animationSpec = tween(500)
                        ),
                        exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(
                            targetOffsetY = { it / 2 },
                            animationSpec = tween(500)
                        ),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        FloatingNavigationBar(
                            selectedItem = selectedItem,
                            onItemSelected = { index ->
                                selectedItem = index
                                when (index) {
                                    0 -> navController.navigate(Home) { popUpTo(Home) { inclusive = true } }
                                    1 -> navController.navigate(Statistics) { popUpTo(Home) }
                                    2 -> navController.navigate(History) { popUpTo(Home) }
                                    3 -> navController.navigate(Settings) { popUpTo(Home) }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun FloatingNavigationBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit
) {
    val icons = listOf(
        R.drawable.ic_nav_home,
        R.drawable.ic_nav_stats,
        R.drawable.ic_nav_history,
        R.drawable.ic_nav_settings
    )
    val itemNames = listOf("Home", "Stats", "History", "Settings")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .height(68.dp),
            shape = CircleShape, // 100% perfect pill rounding matching the picture
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            tonalElevation = 10.dp,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                icons.forEachIndexed { index, iconRes ->
                    val isSelected = selectedItem == index
                    
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
                        } else {
                            androidx.compose.ui.graphics.Color.Transparent
                        },
                        animationSpec = tween(250)
                    )
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        },
                        animationSpec = tween(250)
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .padding(horizontal = 4.dp)
                            .clip(CircleShape) // Capsule active background highlight matching the picture
                            .background(backgroundColor)
                            .clickable { onItemSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = itemNames[index],
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
