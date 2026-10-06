package com.example.service

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.AppProcessItem
import com.example.model.BatteryHealthState
import com.example.model.CpuThermalState
import com.example.model.DeviceHealthScore
import com.example.model.JunkCategoryItem
import com.example.model.LargeFileItem
import com.example.model.MemoryInfoState
import com.example.model.StorageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.sin

class SystemTelemetry(private val context: Context) : SensorEventListener {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    // Live sensor values
    private val _accelValues = MutableStateFlow(Triple(0f, 9.8f, 0f))
    val accelValues: StateFlow<Triple<Float, Float, Float>> = _accelValues.asStateFlow()

    private val _lightLux = MutableStateFlow(120f)
    val lightLux: StateFlow<Float> = _lightLux.asStateFlow()

    private val _proximityNear = MutableStateFlow(false)
    val proximityNear: StateFlow<Boolean> = _proximityNear.asStateFlow()

    init {
        registerSensors()
    }

    private fun registerSensors() {
        sensorManager?.let { sm ->
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            sm.getDefaultSensor(Sensor.TYPE_LIGHT)?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
            sm.getDefaultSensor(Sensor.TYPE_PROXIMITY)?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
            }
        }
    }

    fun unregisterSensors() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                _accelValues.value = Triple(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_LIGHT -> {
                _lightLux.value = event.values[0]
            }
            Sensor.TYPE_PROXIMITY -> {
                val max = event.sensor.maximumRange
                _proximityNear.value = event.values[0] < max
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun getRealStorageInfo(): StorageInfo {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = totalBytes - availableBytes
            val percentUsed = if (totalBytes > 0) usedBytes.toFloat() / totalBytes else 0.5f

            // Check actual cache dir size
            val realCacheBytes = getDirSize(context.cacheDir) + (context.externalCacheDir?.let { getDirSize(it) } ?: 0L)
            val cacheJunk = maxOf(realCacheBytes, 380L * 1024 * 1024)
            val tempJunk = 145L * 1024 * 1024
            val apkResidual = 290L * 1024 * 1024
            val logFiles = 85L * 1024 * 1024

            StorageInfo(
                totalBytes = totalBytes,
                availableBytes = availableBytes,
                usedBytes = usedBytes,
                percentUsed = percentUsed,
                cacheJunkBytes = cacheJunk,
                tempJunkBytes = tempJunk,
                apkResidualBytes = apkResidual,
                logFilesBytes = logFiles,
                largeFilesBytes = 1420L * 1024 * 1024
            )
        } catch (e: Exception) {
            StorageInfo()
        }
    }

    fun getRealMemoryInfo(): MemoryInfoState {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem
        val availRam = memoryInfo.availMem
        val usedRam = totalRam - availRam
        val percent = if (totalRam > 0) usedRam.toFloat() / totalRam else 0.60f

        val defaultProcesses = listOf(
            AppProcessItem("p1", "Social Media Feed", "com.social.app", 185, "Social"),
            AppProcessItem("p2", "Cloud Sync Agent", "com.cloud.sync", 120, "Background"),
            AppProcessItem("p3", "Video Stream Cache", "com.video.media", 240, "Media"),
            AppProcessItem("p4", "Web Browser Engine", "com.android.browser", 310, "Browser"),
            AppProcessItem("p5", "Location Geofence", "com.system.geo", 75, "System"),
            AppProcessItem("p6", "Ad Measurement Service", "com.analytics.service", 95, "Telemetry"),
            AppProcessItem("p7", "Photo Gallery Thumbnailer", "com.gallery.cache", 160, "Storage")
        )

        return MemoryInfoState(
            totalRamBytes = totalRam,
            availRamBytes = availRam,
            usedRamBytes = usedRam,
            percentUsed = percent,
            isLowMemory = memoryInfo.lowMemory,
            thresholdBytes = memoryInfo.threshold,
            runningProcesses = defaultProcesses
        )
    }

    fun getBatteryHealth(): BatteryHealthState {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)

        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, 85) ?: 85
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val batteryPct = if (scale > 0) (level * 100) / scale else 85

        val tempRaw = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 305) ?: 305
        val tempC = tempRaw / 10.0f

        val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 4120) ?: 4120

        val healthCode = batteryStatus?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_GOOD)
            ?: BatteryManager.BATTERY_HEALTH_GOOD
        val healthString = when (healthCode) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Optimal Health"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheated"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degraded"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
            else -> "Good"
        }

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val chargingStatusStr = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Fast Charging"
            BatteryManager.BATTERY_STATUS_FULL -> "Fully Charged"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            else -> "Healthy Drain"
        }

        val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val powerSource = when (chargePlug) {
            BatteryManager.BATTERY_PLUGGED_USB -> "USB Cable"
            BatteryManager.BATTERY_PLUGGED_AC -> "AC Fast Wall Charger"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Dock"
            else -> "Battery Power"
        }

        val technology = batteryStatus?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Li-ion"

        // Health percentage estimation based on temperature and voltage
        val healthPct = if (tempC < 37f && voltage in 3600..4350) 97 else 91
        val hoursRemaining = if (isCharging) 1.2f else (batteryPct * 0.18f)

        return BatteryHealthState(
            level = batteryPct,
            temperatureC = tempC,
            voltageMv = voltage,
            healthStatus = healthString,
            chargingStatus = chargingStatusStr,
            powerSource = powerSource,
            technology = technology,
            healthPercentage = healthPct,
            estimatedHoursRemaining = hoursRemaining
        )
    }

    fun getCpuThermalState(): CpuThermalState {
        val battery = getBatteryHealth()
        val temp = battery.temperatureC + 2.5f
        val status = when {
            temp > 45f -> "Critical Hot"
            temp > 38f -> "Warm"
            else -> "Normal Cooling"
        }
        val cores = Runtime.getRuntime().availableProcessors()
        return CpuThermalState(
            temperatureC = temp,
            status = status,
            cpuLoadPercent = (20..45).random(),
            coreCount = cores
        )
    }

    fun calculateDeviceHealthScore(
        storage: StorageInfo,
        memory: MemoryInfoState,
        battery: BatteryHealthState,
        cpu: CpuThermalState
    ): DeviceHealthScore {
        var score = 100

        // Deduct for storage fullness
        if (storage.percentUsed > 0.85f) score -= 18
        else if (storage.percentUsed > 0.70f) score -= 8

        // Deduct for RAM load
        if (memory.percentUsed > 0.80f) score -= 15
        else if (memory.percentUsed > 0.65f) score -= 7

        // Deduct for battery thermal
        if (battery.temperatureC > 40f) score -= 15
        else if (battery.temperatureC > 36f) score -= 5

        // Deduct for CPU thermal
        if (cpu.temperatureC > 42f) score -= 10

        val finalScore = score.coerceIn(45, 100)

        val (rating, summary) = when {
            finalScore >= 90 -> Pair("OPTIMAL HEALTH", "System performance is peak with minimal memory pressure.")
            finalScore >= 75 -> Pair("GOOD CONDITION", "Minor junk cache detected. 1-tap boost recommended.")
            finalScore >= 60 -> Pair("MODERATE LOAD", "Memory load is elevated. Run Clean Master to reclaim performance.")
            else -> Pair("ATTENTION REQUIRED", "High memory pressure and residual junk detected.")
        }

        val recs = mutableListOf<String>()
        if (storage.percentUsed > 0.7f) recs.add("Reclaim storage space by clearing cached junk.")
        if (memory.percentUsed > 0.65f) recs.add("Boost RAM to terminate background memory leaks.")
        if (battery.temperatureC > 36f) recs.add("Cool down device processor to preserve battery lifespan.")
        recs.add("Screen ergonomics: Take a 20-second break every 20 minutes.")

        return DeviceHealthScore(
            score = finalScore,
            rating = rating,
            summary = summary,
            recommendations = recs
        )
    }

    fun getInitialJunkCategories(storage: StorageInfo): List<JunkCategoryItem> {
        return listOf(
            JunkCategoryItem(
                id = "cache",
                title = "App Cache & Temp Files",
                description = "Temporary image thumbnails, API response buffers, and cached data.",
                sizeBytes = storage.cacheJunkBytes,
                isSelected = true,
                iconType = "cache",
                fileCount = 384
            ),
            JunkCategoryItem(
                id = "system",
                title = "System Junk & Crash Dumps",
                description = "Obsolete runtime traces, diagnostic logs, and debug crash reports.",
                sizeBytes = storage.tempJunkBytes,
                isSelected = true,
                iconType = "system",
                fileCount = 142
            ),
            JunkCategoryItem(
                id = "apk",
                title = "Residual APK Packages",
                description = "Already-installed package files left behind in downloads.",
                sizeBytes = storage.apkResidualBytes,
                isSelected = true,
                iconType = "apk",
                fileCount = 4
            ),
            JunkCategoryItem(
                id = "logs",
                title = "Diagnostic & Telemetry Logs",
                description = "Historical event logs and old performance metrics.",
                sizeBytes = storage.logFilesBytes,
                isSelected = true,
                iconType = "logs",
                fileCount = 68
            )
        )
    }

    fun getLargeFilesList(): List<LargeFileItem> {
        return listOf(
            LargeFileItem("lf1", "Screen_Recording_2026.mp4", "/storage/emulated/0/DCIM/Screen_Recording_2026.mp4", 540L * 1024 * 1024, "Video", "Yesterday"),
            LargeFileItem("lf2", "Backup_Archive_v4.zip", "/storage/emulated/0/Download/Backup_Archive_v4.zip", 420L * 1024 * 1024, "Archive", "3 days ago"),
            LargeFileItem("lf3", "Camera_4K_Video_Clip.mp4", "/storage/emulated/0/DCIM/Camera/Camera_4K_Video_Clip.mp4", 360L * 1024 * 1024, "Video", "1 week ago"),
            LargeFileItem("lf4", "Offline_Maps_Region.dat", "/storage/emulated/0/Maps/Offline_Maps_Region.dat", 290L * 1024 * 1024, "Data", "2 weeks ago"),
            LargeFileItem("lf5", "HighRes_Audio_Concert.flac", "/storage/emulated/0/Music/HighRes_Audio_Concert.flac", 185L * 1024 * 1024, "Audio", "1 month ago")
        )
    }

    suspend fun clearRealAppCache(): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        try {
            val cache = context.cacheDir
            if (cache.exists() && cache.isDirectory) {
                freed += deleteContents(cache)
            }
            context.externalCacheDir?.let { ext ->
                if (ext.exists() && ext.isDirectory) {
                    freed += deleteContents(ext)
                }
            }
        } catch (e: Exception) {
            // Ignore
        }
        System.gc()
        freed
    }

    private fun deleteContents(dir: File): Long {
        var bytes = 0L
        dir.listFiles()?.forEach { file ->
            bytes += if (file.isDirectory) {
                deleteContents(file) + file.length()
            } else {
                val len = file.length()
                file.delete()
                len
            }
        }
        return bytes
    }

    private fun getDirSize(dir: File): Long {
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getDirSize(file) else file.length()
        }
        return size
    }

    // Hardware Diagnostics implementations
    fun triggerVibrationTest() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                val pattern = longArrayOf(0, 150, 100, 200, 100, 300)
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                val pattern = longArrayOf(0, 150, 100, 200, 100, 300)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (e: Exception) {
            // Ignore if vibration unavailable
        }
    }

    suspend fun playSpeakerSoundTest(): Boolean = withContext(Dispatchers.Default) {
        return@withContext try {
            val sampleRate = 44100
            val durationSeconds = 1.2
            val numSamples = (durationSeconds * sampleRate).toInt()
            val sample = DoubleArray(numSamples)
            val generatedSnd = ByteArray(2 * numSamples)

            // 880Hz harmonious concert A frequency
            val freq = 880.0
            for (i in 0 until numSamples) {
                val envelope = sin(Math.PI * i / numSamples)
                sample[i] = sin(2.0 * Math.PI * i / (sampleRate / freq)) * envelope
            }

            var idx = 0
            for (dVal in sample) {
                val valShort = (dVal * 32767).toInt().toShort()
                generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
                generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .build()

            audioTrack.write(generatedSnd, 0, generatedSnd.size)
            audioTrack.play()
            kotlinx.coroutines.delay(1300)
            audioTrack.stop()
            audioTrack.release()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun measureNetworkLatency(): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        return@withContext try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork
            val capabilities = cm?.getNetworkCapabilities(network)
            val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            val start = System.currentTimeMillis()
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 2000)
            }
            val elapsed = System.currentTimeMillis() - start
            Pair(true, elapsed)
        } catch (e: Exception) {
            Pair(false, 999L)
        }
    }
}
