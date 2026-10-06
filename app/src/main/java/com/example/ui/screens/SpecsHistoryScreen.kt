package com.example.ui.screens

import android.os.Build
import android.os.SystemClock
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.OptimizationLogEntity
import com.example.ui.components.FormatUtils
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavyBorder
import com.example.ui.theme.DarkNavySurfaceCard
import com.example.ui.theme.DarkNavySurfaceVariant
import com.example.ui.theme.EmeraldVitality
import com.example.ui.theme.PerformanceViolet
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.MainViewModel
import java.util.concurrent.TimeUnit

@Composable
fun SpecsHistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.optimizationHistory.collectAsState()
    val storage by viewModel.storageInfo.collectAsState()
    val memory by viewModel.memoryInfo.collectAsState()

    val uptimeHours = TimeUnit.MILLISECONDS.toHours(SystemClock.elapsedRealtime())
    val uptimeMinutes = TimeUnit.MILLISECONDS.toMinutes(SystemClock.elapsedRealtime()) % 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Device Specs & History",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hardware Telemetry & Optimization Logs",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Device Specifications Card
            SectionHeader(
                title = "Hardware & System Specifications",
                subtitle = "Verified device configuration and processor metrics"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(DarkNavyBorder, CyanPrimary.copy(alpha = 0.25f))
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SpecRow(
                        icon = Icons.Default.PhoneAndroid,
                        label = "Device Model",
                        value = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
                        accentColor = CyanPrimary
                    )
                    SpecDivider()
                    SpecRow(
                        icon = Icons.Default.Security,
                        label = "Android Version",
                        value = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                        accentColor = EmeraldVitality
                    )
                    SpecDivider()
                    SpecRow(
                        icon = Icons.Default.Memory,
                        label = "Processor Architecture",
                        value = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
                        accentColor = PerformanceViolet
                    )
                    SpecDivider()
                    SpecRow(
                        icon = Icons.Default.Info,
                        label = "Hardware Capacities",
                        value = "${FormatUtils.formatBytes(storage.totalBytes)} Storage • ${FormatUtils.formatBytes(memory.totalRamBytes)} RAM",
                        accentColor = WarningAmber
                    )
                    SpecDivider()
                    SpecRow(
                        icon = Icons.Default.History,
                        label = "System Uptime",
                        value = "$uptimeHours hrs $uptimeMinutes mins uninterrupted",
                        accentColor = CyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optimization History Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Optimization & Clean Logs",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${history.size} recorded optimization events",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                if (history.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearHistory() },
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear History",
                            tint = TextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (history.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(DarkNavyBorder, DarkNavySurfaceVariant))
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Empty History",
                            tint = TextTertiary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Optimization Logs Yet",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Run 1-Tap Master Boost or Junk Cleaner to record device performance boost events here.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(history, key = { it.id }) { log ->
                HistoryItemCard(log = log)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SpecRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, color = TextTertiary, fontSize = 11.sp)
            Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun SpecDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DarkNavyBorder.copy(alpha = 0.4f))
    )
}

@Composable
fun HistoryItemCard(log: OptimizationLogEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag("history_item_${log.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(DarkNavyBorder, EmeraldVitality.copy(alpha = 0.2f))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when {
                log.actionType.contains("Boost") -> Icons.Default.RocketLaunch
                log.actionType.contains("RAM") -> Icons.Default.Memory
                log.actionType.contains("CPU") -> Icons.Default.Thermostat
                else -> Icons.Default.CleaningServices
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(EmeraldVitality.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = log.actionType,
                    tint = EmeraldVitality,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = log.actionType,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = FormatUtils.formatDate(log.timestamp),
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = log.detail,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.bytesFreed > 0) {
                        Text(
                            text = "+${FormatUtils.formatBytes(log.bytesFreed)} Freed",
                            color = EmeraldVitality,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Score: ${log.healthScore}%",
                        color = CyanPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
