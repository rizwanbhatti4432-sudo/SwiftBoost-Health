package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.FormatUtils
import com.example.ui.components.HealthScoreRadialGauge
import com.example.ui.components.MetricQuickCard
import com.example.ui.components.ScanningProgressCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.CyanPrimaryDark
import com.example.ui.theme.DarkNavyBorder
import com.example.ui.theme.DarkNavySurfaceCard
import com.example.ui.theme.DarkNavySurfaceVariant
import com.example.ui.theme.EmeraldVitality
import com.example.ui.theme.PerformanceViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.MainViewModel

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val storage by viewModel.storageInfo.collectAsState()
    val memory by viewModel.memoryInfo.collectAsState()
    val battery by viewModel.batteryState.collectAsState()
    val cpu by viewModel.cpuState.collectAsState()
    val health by viewModel.healthScore.collectAsState()
    val isCleaning by viewModel.isCleaning.collectAsState()
    val isBoosting by viewModel.isBoostingMemory.collectAsState()
    val isCooling by viewModel.isCoolingDown.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanPath by viewModel.scanningFilePath.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // App Top Bar inside Screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(CyanPrimary, EmeraldVitality))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "App Logo",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Clean Master & Health",
                        color = TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Peak Performance & Vitality",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(
                onClick = { viewModel.refreshAllMetrics() },
                modifier = Modifier.testTag("refresh_metrics_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Metrics",
                    tint = CyanPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Hero Health Vitality Gauge Center
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(DarkNavySurfaceCard, DarkNavySurfaceVariant)
                    )
                )
                .border(1.dp, DarkNavyBorder, RoundedCornerShape(24.dp))
                .padding(vertical = 20.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HealthScoreRadialGauge(
                    score = health.score,
                    rating = health.rating,
                    onClick = { viewModel.runOneTapMasterBoost() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = health.summary,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanning Progress if active
        AnimatedVisibility(
            visible = isCleaning,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ScanningProgressCard(
                currentPath = scanPath,
                progress = scanProgress
            )
        }

        // 1-Tap Master Boost Big Action Button
        Button(
            onClick = { viewModel.runOneTapMasterBoost() },
            enabled = !isCleaning && !isBoosting && !isCooling,
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .testTag("one_tap_master_boost_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                disabledContainerColor = CyanPrimary.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isCleaning || isBoosting || isCooling) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "OPTIMIZING PERFORMANCE...",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = "Turbo Clean",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "1-TAP MASTER BOOST",
                            color = Color.Black,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Purge Cache • Trim RAM • Cool CPU",
                            color = Color(0xFF003038),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4 Core System Metrics Grid
        SectionHeader(
            title = "Device Health & Telemetry",
            subtitle = "Real-time hardware vitality status"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricQuickCard(
                title = "STORAGE",
                valueText = "${FormatUtils.formatBytes(storage.usedBytes)} / ${FormatUtils.formatBytes(storage.totalBytes)}",
                subText = "${(storage.percentUsed * 100).toInt()}% Used (${FormatUtils.formatBytes(storage.totalJunkBytes)} junk)",
                progress = storage.percentUsed,
                icon = Icons.Default.SdStorage,
                accentColor = CyanPrimary,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setTab(AppTab.CLEANER) }
            )

            MetricQuickCard(
                title = "RAM MEMORY",
                valueText = "${FormatUtils.formatBytes(memory.usedRamBytes)} / ${FormatUtils.formatBytes(memory.totalRamBytes)}",
                subText = "${(memory.percentUsed * 100).toInt()}% Used (${FormatUtils.formatBytes(memory.availRamBytes)} free)",
                progress = memory.percentUsed,
                icon = Icons.Default.Memory,
                accentColor = PerformanceViolet,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.boostMemory() }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricQuickCard(
                title = "BATTERY HEALTH",
                valueText = "${battery.level}% • ${battery.temperatureC}°C",
                subText = "${battery.healthStatus} (${battery.chargingStatus})",
                progress = battery.level / 100f,
                icon = Icons.Default.BatteryChargingFull,
                accentColor = EmeraldVitality,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setTab(AppTab.DIAGNOSTICS) }
            )

            MetricQuickCard(
                title = "CPU THERMAL",
                valueText = "${cpu.temperatureC}°C",
                subText = "${cpu.status} (${cpu.coreCount} Cores Active)",
                progress = (cpu.temperatureC / 60f).coerceIn(0f, 1f),
                icon = Icons.Default.Thermostat,
                accentColor = WarningAmber,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.coolDownCpu() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick Hub Shortcuts
        SectionHeader(title = "Performance Master Tools")

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickToolButton(
                title = "Clean Junk",
                subtitle = FormatUtils.formatBytes(storage.totalJunkBytes),
                icon = Icons.Default.CleaningServices,
                accentColor = CyanPrimary,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setTab(AppTab.CLEANER) }
            )

            QuickToolButton(
                title = "RAM Boost",
                subtitle = "Trim Cache",
                icon = Icons.Default.Speed,
                accentColor = PerformanceViolet,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.boostMemory() }
            )

            QuickToolButton(
                title = "CPU Cooler",
                subtitle = "${cpu.temperatureC}°C",
                icon = Icons.Default.AcUnit,
                accentColor = WarningAmber,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.coolDownCpu() }
            )

            QuickToolButton(
                title = "Diagnostics",
                subtitle = "Hardware",
                icon = Icons.Default.HealthAndSafety,
                accentColor = EmeraldVitality,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.setTab(AppTab.DIAGNOSTICS) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Illustration Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(DarkNavyBorder, CyanPrimary.copy(alpha = 0.2f)))
            )
        ) {
            Column {
                Image(
                    painter = painterResource(id = R.drawable.img_device_health_hero_1788706104152),
                    contentDescription = "Device Health Core",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Device Health",
                                tint = EmeraldVitality,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Device Health & Longevity Insights",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• 20%-80% Charging Rule: Avoid leaving your device plugged at 100% overnight to prevent Li-ion battery cathode degradation.\n• Thermal Defense: Running CPU cooler routines when temperature exceeds 36°C protects memory chips.\n• Digital Ergonomics: Rest eyes 20 seconds every 20 minutes to preserve visual focus.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun QuickToolButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("quick_tool_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(DarkNavyBorder, accentColor.copy(alpha = 0.25f)))
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                color = TextTertiary,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
