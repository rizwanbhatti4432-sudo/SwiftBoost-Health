package com.example.model

enum class DiagnosticType(val title: String, val icon: String) {
    TOUCH_SCREEN("Screen & Multi-Touch", "touch"),
    SPEAKER_AUDIO("Audio & Speakers", "volume"),
    VIBRATION("Haptic Motor", "vibrate"),
    SENSORS("Motion & Light Sensors", "sensors"),
    NETWORK("Wi-Fi & Latency Ping", "network")
}

data class StorageInfo(
    val totalBytes: Long = 64L * 1024 * 1024 * 1024,
    val availableBytes: Long = 24L * 1024 * 1024 * 1024,
    val usedBytes: Long = 40L * 1024 * 1024 * 1024,
    val percentUsed: Float = 0.62f,
    val cacheJunkBytes: Long = 420L * 1024 * 1024,
    val tempJunkBytes: Long = 185L * 1024 * 1024,
    val apkResidualBytes: Long = 310L * 1024 * 1024,
    val logFilesBytes: Long = 95L * 1024 * 1024,
    val largeFilesBytes: Long = 1840L * 1024 * 1024
) {
    val totalJunkBytes: Long
        get() = cacheJunkBytes + tempJunkBytes + apkResidualBytes + logFilesBytes
}

data class AppProcessItem(
    val id: String,
    val name: String,
    val packageName: String,
    val memoryUsageMb: Int,
    val category: String,
    val isSelected: Boolean = true
)

data class MemoryInfoState(
    val totalRamBytes: Long = 6L * 1024 * 1024 * 1024,
    val availRamBytes: Long = 2300L * 1024 * 1024,
    val usedRamBytes: Long = 3700L * 1024 * 1024,
    val percentUsed: Float = 0.61f,
    val isLowMemory: Boolean = false,
    val thresholdBytes: Long = 500L * 1024 * 1024,
    val runningProcesses: List<AppProcessItem> = emptyList()
)

data class BatteryHealthState(
    val level: Int = 85,
    val temperatureC: Float = 29.5f,
    val voltageMv: Int = 4150,
    val healthStatus: String = "Good",
    val chargingStatus: String = "Discharging",
    val powerSource: String = "Battery",
    val technology: String = "Li-ion",
    val healthPercentage: Int = 98,
    val estimatedHoursRemaining: Float = 16.4f
)

data class CpuThermalState(
    val temperatureC: Float = 32.0f,
    val status: String = "Normal",
    val cpuLoadPercent: Int = 28,
    val coreCount: Int = 8
)

data class JunkCategoryItem(
    val id: String,
    val title: String,
    val description: String,
    val sizeBytes: Long,
    val isSelected: Boolean,
    val iconType: String,
    val fileCount: Int
)

data class DeviceHealthScore(
    val score: Int,
    val rating: String,
    val summary: String,
    val recommendations: List<String>
)

data class LargeFileItem(
    val id: String,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val category: String,
    val modifiedDate: String,
    val isSelected: Boolean = false
)
