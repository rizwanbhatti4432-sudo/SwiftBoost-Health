package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.model.JunkCategoryItem
import com.example.model.LargeFileItem
import com.example.ui.components.FormatUtils
import com.example.ui.components.ScanningProgressCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CriticalRed
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

@Composable
fun CleanerMasterScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val storage by viewModel.storageInfo.collectAsState()
    val memory by viewModel.memoryInfo.collectAsState()
    val junkCategories by viewModel.junkCategories.collectAsState()
    val largeFiles by viewModel.largeFiles.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isCleaning by viewModel.isCleaning.collectAsState()
    val isBoosting by viewModel.isBoostingMemory.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()
    val scanPath by viewModel.scanningFilePath.collectAsState()

    val selectedJunkBytes = junkCategories.filter { it.isSelected }.sumOf { it.sizeBytes }

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
                        text = "Cleaner Master",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Junk Purge & Memory Reclaim",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                IconButton(
                    onClick = { viewModel.startJunkScan() },
                    modifier = Modifier.testTag("rescan_junk_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Scan Junk",
                        tint = CyanPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Junk Overview Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(DarkNavyBorder, CyanPrimary.copy(alpha = 0.3f))
                    )
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "JUNK DETECTED",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FormatUtils.formatBytes(selectedJunkBytes),
                                color = if (selectedJunkBytes > 0) CyanPrimary else EmeraldVitality,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Button(
                            onClick = { viewModel.startJunkScan() },
                            enabled = !isScanning && !isCleaning,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkNavySurfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("scan_now_button")
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = CyanPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scanning...", color = CyanPrimary, fontSize = 12.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = "Scan",
                                    tint = CyanPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Scan Storage", color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                    }

                    if (isScanning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ScanningProgressCard(
                            currentPath = scanPath,
                            progress = scanProgress
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clean Junk Button
                    Button(
                        onClick = { viewModel.cleanSelectedJunk() },
                        enabled = selectedJunkBytes > 0 && !isCleaning && !isScanning,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("clean_selected_junk_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldVitality,
                            disabledContainerColor = EmeraldVitality.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        if (isCleaning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "PURGING JUNK FILES...",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clean",
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedJunkBytes > 0)
                                    "CLEAN ${FormatUtils.formatBytes(selectedJunkBytes)} JUNK"
                                else
                                    "STORAGE IS CLEAN & OPTIMAL",
                                color = Color.Black,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionHeader(
                title = "Junk Categories",
                subtitle = "Select items to safely delete without affecting personal data"
            )
        }

        // Junk Categories Items
        items(junkCategories, key = { it.id }) { item ->
            JunkCategoryCard(
                item = item,
                onToggle = { viewModel.toggleJunkCategory(item.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            // RAM Memory Booster Section
            SectionHeader(
                title = "RAM Memory Performance",
                subtitle = "Free memory caches to speed up active apps"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(
                        listOf(DarkNavyBorder, PerformanceViolet.copy(alpha = 0.3f))
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(PerformanceViolet.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = "RAM",
                                    tint = PerformanceViolet,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Active Memory Load",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${FormatUtils.formatBytes(memory.usedRamBytes)} used of ${FormatUtils.formatBytes(memory.totalRamBytes)}",
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.boostMemory() },
                            enabled = !isBoosting,
                            colors = ButtonDefaults.buttonColors(containerColor = PerformanceViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("boost_ram_button")
                        ) {
                            if (isBoosting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Boosting...", fontSize = 12.sp, color = Color.White)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Boost",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Boost RAM", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { memory.percentUsed },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = PerformanceViolet,
                        trackColor = DarkNavySurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Terminating idle background processes frees up to 500MB of RAM for games and video streaming.",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Large Files Section
            SectionHeader(
                title = "Large Files Analyzer",
                subtitle = "Review high-capacity media files occupying device space"
            )
        }

        // Large Files Items
        items(largeFiles, key = { it.id }) { file ->
            LargeFileCard(
                item = file,
                onToggle = { viewModel.toggleLargeFile(file.id) }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            val selectedLargeFiles = largeFiles.filter { it.isSelected }
            if (selectedLargeFiles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.deleteSelectedLargeFiles() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_large_files_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CriticalRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Selected (${FormatUtils.formatBytes(selectedLargeFiles.sumOf { it.sizeBytes })})",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun JunkCategoryCard(
    item: JunkCategoryItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() }
            .testTag("junk_category_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isSelected && item.sizeBytes > 0)
                DarkNavySurfaceCard
            else
                DarkNavySurfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(DarkNavyBorder, if (item.isSelected) CyanPrimary.copy(alpha = 0.3f) else Color.Transparent)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconVector = when (item.iconType) {
                "cache" -> Icons.Default.Cached
                "apk" -> Icons.Default.Android
                "logs" -> Icons.Default.Archive
                else -> Icons.Default.Folder
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = item.title,
                    tint = CyanPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (item.fileCount > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${item.fileCount} items)",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = FormatUtils.formatBytes(item.sizeBytes),
                    color = if (item.sizeBytes > 0) TextPrimary else EmeraldVitality,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Checkbox(
                    checked = item.isSelected,
                    onCheckedChange = { onToggle() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CyanPrimary,
                        checkmarkColor = Color.Black,
                        uncheckedColor = DarkNavyBorder
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun LargeFileCard(
    item: LargeFileItem,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onToggle() }
            .testTag("large_file_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(DarkNavyBorder, if (item.isSelected) CriticalRed.copy(alpha = 0.4f) else Color.Transparent)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CriticalRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = item.name,
                    tint = CriticalRed,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "${item.category} • ${item.modifiedDate}",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = FormatUtils.formatBytes(item.sizeBytes),
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Checkbox(
                checked = item.isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = CriticalRed,
                    checkmarkColor = Color.White,
                    uncheckedColor = DarkNavyBorder
                )
            )
        }
    }
}
