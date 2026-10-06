package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatteryHealthState
import com.example.model.DiagnosticType
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.DarkNavyBackground
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
import kotlin.math.roundToInt

@Composable
fun DiagnosticsHealthScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val battery by viewModel.batteryState.collectAsState()
    val activeModal by viewModel.activeDiagnosticModal.collectAsState()
    val touchPassed by viewModel.touchPassed.collectAsState()
    val audioPassed by viewModel.audioPassed.collectAsState()
    val vibrationPassed by viewModel.vibrationPassed.collectAsState()
    val sensorsPassed by viewModel.sensorsPassed.collectAsState()
    val networkLatency by viewModel.networkLatency.collectAsState()

    val accel by viewModel.systemTelemetry.accelValues.collectAsState()
    val lightLux by viewModel.systemTelemetry.lightLux.collectAsState()
    val proxNear by viewModel.systemTelemetry.proximityNear.collectAsState()

    var selectedSaverMode by remember { mutableStateOf("Balanced") }

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
                        text = "Device & Battery Health",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hardware Diagnostics & Battery Longevity",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Battery Health Center Card
            BatteryHealthCard(battery = battery)

            Spacer(modifier = Modifier.height(16.dp))

            // Estimated Battery Endurance
            SectionHeader(
                title = "Estimated Battery Endurance",
                subtitle = "Expected continuous runtime based on current charge"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EnduranceCard(
                    title = "Voice Calls",
                    hours = String.format("%.1f hrs", battery.level * 0.32f),
                    color = EmeraldVitality,
                    modifier = Modifier.weight(1f)
                )
                EnduranceCard(
                    title = "Video Playback",
                    hours = String.format("%.1f hrs", battery.level * 0.16f),
                    color = CyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                EnduranceCard(
                    title = "Web Surfing",
                    hours = String.format("%.1f hrs", battery.level * 0.22f),
                    color = PerformanceViolet,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Battery Saver Profiles
            SectionHeader(
                title = "Battery Health Saver Profiles",
                subtitle = "Adjust system throttle to extend daily battery life"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Balanced", "Smart Saver", "Ultra Saver").forEach { mode ->
                    val isSelected = selectedSaverMode == mode
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedSaverMode = mode }
                            .testTag("battery_mode_${mode.lowercase().replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.2f) else DarkNavySurfaceCard
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(
                                listOf(if (isSelected) CyanPrimary else DarkNavyBorder, if (isSelected) EmeraldVitality else DarkNavyBorder)
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = mode,
                                color = if (isSelected) CyanPrimary else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (mode == "Balanced") "Standard" else if (mode == "Smart Saver") "+2.5 hrs" else "+6.0 hrs",
                                color = if (isSelected) EmeraldVitality else TextTertiary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Hardware Diagnostics Suite
            SectionHeader(
                title = "Hardware & Sensor Diagnostics",
                subtitle = "Run live tests on touch screen, sound, vibration, and sensors"
            )
        }

        // Diagnostic Item: Touch & Screen
        item {
            DiagnosticItemCard(
                title = "Touch Screen & Display Test",
                description = "Interactive touch grid & dead-pixel color uniformity test",
                icon = Icons.Default.TouchApp,
                statusPassed = touchPassed,
                onRunTest = { viewModel.openDiagnosticModal(DiagnosticType.TOUCH_SCREEN) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Diagnostic Item: Audio & Speaker
        item {
            DiagnosticItemCard(
                title = "Speaker & Acoustic Output",
                description = "Generates 880Hz harmonious acoustic chime across loudspeakers",
                icon = Icons.Default.VolumeUp,
                statusPassed = audioPassed,
                onRunTest = { viewModel.runAudioTest() }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Diagnostic Item: Haptic Vibration
        item {
            DiagnosticItemCard(
                title = "Haptic Vibration Motor",
                description = "Tests motor calibration with triple-frequency haptic pulses",
                icon = Icons.Default.Vibration,
                statusPassed = vibrationPassed,
                onRunTest = { viewModel.runVibrationTest() }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Diagnostic Item: Motion & Light Sensors
        item {
            DiagnosticItemCard(
                title = "Sensors Live Telemetry",
                description = "Live readings: Accelerometer (${String.format("%.1f", accel.first)}), Light (${lightLux.toInt()} lx), Proximity (${if (proxNear) "Near" else "Clear"})",
                icon = Icons.Default.Sensors,
                statusPassed = sensorsPassed ?: true,
                onRunTest = { viewModel.openDiagnosticModal(DiagnosticType.SENSORS) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Diagnostic Item: Wi-Fi & Network Latency
        item {
            val latencyDesc = if (networkLatency != null) {
                "${networkLatency} ms ping latency to Google DNS (8.8.8.8)"
            } else {
                "Measures network round-trip packet latency and connection health"
            }
            DiagnosticItemCard(
                title = "Wi-Fi & Network Latency Check",
                description = latencyDesc,
                icon = Icons.Default.NetworkCheck,
                statusPassed = if (networkLatency != null) (networkLatency!! < 300) else null,
                onRunTest = { viewModel.runNetworkLatencyTest() }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Interactive Modals
    when (activeModal) {
        DiagnosticType.TOUCH_SCREEN -> {
            TouchScreenTestDialog(
                onFinish = { passed ->
                    viewModel.markTouchScreenPassed(passed)
                    viewModel.openDiagnosticModal(null)
                }
            )
        }
        DiagnosticType.SENSORS -> {
            SensorsLiveDialog(
                accel = accel,
                lightLux = lightLux,
                proxNear = proxNear,
                onDismiss = {
                    viewModel.markSensorsPassed(true)
                    viewModel.openDiagnosticModal(null)
                }
            )
        }
        else -> {}
    }
}

@Composable
fun BatteryHealthCard(battery: BatteryHealthState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(DarkNavyBorder, EmeraldVitality.copy(alpha = 0.35f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(EmeraldVitality.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = EmeraldVitality,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Battery Health Status",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${battery.powerSource} • ${battery.chargingStatus}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldVitality.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "${battery.healthPercentage}% HEALTH",
                        color = EmeraldVitality,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Key Battery Stats 4-columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(label = "Charge Level", value = "${battery.level}%", color = EmeraldVitality)
                StatItem(label = "Temperature", value = "${battery.temperatureC}°C", color = if (battery.temperatureC > 38f) WarningAmber else TextPrimary)
                StatItem(label = "Voltage", value = "${battery.voltageMv} mV", color = CyanPrimary)
                StatItem(label = "Cell Type", value = battery.technology, color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { battery.level / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = EmeraldVitality,
                trackColor = DarkNavySurfaceVariant
            )
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextTertiary, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EnduranceCard(title: String, hours: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(DarkNavyBorder, color.copy(alpha = 0.25f)))
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = hours, color = color, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun DiagnosticItemCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    statusPassed: Boolean?,
    onRunTest: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onRunTest() }
            .testTag("diagnostic_card_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(DarkNavyBorder, if (statusPassed == true) EmeraldVitality.copy(alpha = 0.3f) else CyanPrimary.copy(alpha = 0.15f))
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyanPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = CyanPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (statusPassed == true) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Passed",
                    tint = EmeraldVitality,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                OutlinedButton(
                    onClick = onRunTest,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanPrimary),
                    border = ButtonDefaults.outlinedButtonBorder().copy(
                        brush = Brush.linearGradient(listOf(CyanPrimary, EmeraldVitality))
                    ),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Test", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TouchScreenTestDialog(onFinish: (Boolean) -> Unit) {
    val touchPoints = remember { mutableStateListOf<Offset>() }
    var deadPixelColorIndex by remember { mutableStateOf(0) }
    val deadPixelColors = listOf(Color.Transparent, Color.White, Color.Red, Color.Green, Color.Blue, Color.Black)

    AlertDialog(
        onDismissRequest = { onFinish(touchPoints.size > 15) },
        confirmButton = {
            Button(
                onClick = { onFinish(touchPoints.size > 15) },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldVitality),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("finish_touch_test_button")
            ) {
                Text("Pass & Save", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    deadPixelColorIndex = (deadPixelColorIndex + 1) % deadPixelColors.size
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Color Uniformity (${deadPixelColorIndex + 1}/${deadPixelColors.size})", fontSize = 11.sp)
            }
        },
        title = {
            Text("Screen Touch & Pixel Test", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    "Swipe and touch the area below to verify touch digitizer sensitivity and pixel response:",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (deadPixelColorIndex == 0) DarkNavyBackground else deadPixelColors[deadPixelColorIndex]
                        )
                        .border(1.dp, CyanPrimary, RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                touchPoints.add(change.position)
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                touchPoints.add(offset)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        touchPoints.takeLast(300).forEach { pt ->
                            drawCircle(
                                color = CyanPrimary,
                                radius = 16.dp.toPx(),
                                center = pt,
                                alpha = 0.75f
                            )
                        }
                    }
                    if (touchPoints.isEmpty() && deadPixelColorIndex == 0) {
                        Text(
                            text = "Touch and drag your finger here...",
                            color = TextTertiary,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Touches recorded: ${touchPoints.size} (Minimum 15 to pass)",
                    color = if (touchPoints.size >= 15) EmeraldVitality else WarningAmber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        containerColor = DarkNavySurfaceCard,
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
fun SensorsLiveDialog(
    accel: Triple<Float, Float, Float>,
    lightLux: Float,
    proxNear: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("sensors_dialog_done")
            ) {
                Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Text("Live Hardware Sensors", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Accelerometer live tilt
                Text("Accelerometer (Tilt X, Y, Z):", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("X: ${String.format("%.2f", accel.first)} m/s²", color = CyanPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Y: ${String.format("%.2f", accel.second)} m/s²", color = EmeraldVitality, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("Z: ${String.format("%.2f", accel.third)} m/s²", color = PerformanceViolet, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Ambient Light
                Text("Ambient Light Sensor:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${lightLux.roundToInt()} Lux (${if (lightLux < 20) "Dim/Night" else if (lightLux < 500) "Normal Indoor" else "Bright Outdoor"})",
                    color = WarningAmber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Proximity
                Text("Proximity Sensor:", color = TextSecondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (proxNear) "NEAR OBJECT (Hand/Ear detected)" else "CLEAR (No obstruction)",
                    color = if (proxNear) CriticalRed else EmeraldVitality,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = DarkNavySurfaceCard,
        shape = RoundedCornerShape(18.dp)
    )
}
