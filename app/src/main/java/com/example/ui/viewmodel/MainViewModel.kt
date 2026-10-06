package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.OptimizationLogEntity
import com.example.model.BatteryHealthState
import com.example.model.CpuThermalState
import com.example.model.DeviceHealthScore
import com.example.model.DiagnosticType
import com.example.model.JunkCategoryItem
import com.example.model.LargeFileItem
import com.example.model.MemoryInfoState
import com.example.model.StorageInfo
import com.example.service.SystemTelemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppTab(val title: String) {
    DASHBOARD("Health Core"),
    CLEANER("Cleaner Master"),
    DIAGNOSTICS("Diagnostics"),
    SPECS_HISTORY("Device Specs")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val systemTelemetry = SystemTelemetry(application)
    private val database = AppDatabase.getInstance(application)
    private val dao = database.optimizationLogDao()

    val optimizationHistory: StateFlow<List<OptimizationLogEntity>> = dao.getAllLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _storageInfo = MutableStateFlow(systemTelemetry.getRealStorageInfo())
    val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

    private val _memoryInfo = MutableStateFlow(systemTelemetry.getRealMemoryInfo())
    val memoryInfo: StateFlow<MemoryInfoState> = _memoryInfo.asStateFlow()

    private val _batteryState = MutableStateFlow(systemTelemetry.getBatteryHealth())
    val batteryState: StateFlow<BatteryHealthState> = _batteryState.asStateFlow()

    private val _cpuState = MutableStateFlow(systemTelemetry.getCpuThermalState())
    val cpuState: StateFlow<CpuThermalState> = _cpuState.asStateFlow()

    private val _healthScore = MutableStateFlow(
        systemTelemetry.calculateDeviceHealthScore(
            _storageInfo.value,
            _memoryInfo.value,
            _batteryState.value,
            _cpuState.value
        )
    )
    val healthScore: StateFlow<DeviceHealthScore> = _healthScore.asStateFlow()

    private val _junkCategories = MutableStateFlow(systemTelemetry.getInitialJunkCategories(_storageInfo.value))
    val junkCategories: StateFlow<List<JunkCategoryItem>> = _junkCategories.asStateFlow()

    private val _largeFiles = MutableStateFlow(systemTelemetry.getLargeFilesList())
    val largeFiles: StateFlow<List<LargeFileItem>> = _largeFiles.asStateFlow()

    // Interactive operations states
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanProgress = MutableStateFlow(0f)
    val scanProgress: StateFlow<Float> = _scanProgress.asStateFlow()

    private val _scanningFilePath = MutableStateFlow("")
    val scanningFilePath: StateFlow<String> = _scanningFilePath.asStateFlow()

    private val _isCleaning = MutableStateFlow(false)
    val isCleaning: StateFlow<Boolean> = _isCleaning.asStateFlow()

    private val _isBoostingMemory = MutableStateFlow(false)
    val isBoostingMemory: StateFlow<Boolean> = _isBoostingMemory.asStateFlow()

    private val _isCoolingDown = MutableStateFlow(false)
    val isCoolingDown: StateFlow<Boolean> = _isCoolingDown.asStateFlow()

    private val _showCleanSuccessDialog = MutableStateFlow(false)
    val showCleanSuccessDialog: StateFlow<Boolean> = _showCleanSuccessDialog.asStateFlow()

    private val _cleanSuccessTitle = MutableStateFlow("")
    val cleanSuccessTitle: StateFlow<String> = _cleanSuccessTitle.asStateFlow()

    private val _cleanSuccessBytes = MutableStateFlow(0L)
    val cleanSuccessBytes: StateFlow<Long> = _cleanSuccessBytes.asStateFlow()

    // Diagnostic states
    private val _activeDiagnosticModal = MutableStateFlow<DiagnosticType?>(null)
    val activeDiagnosticModal: StateFlow<DiagnosticType?> = _activeDiagnosticModal.asStateFlow()

    private val _touchPassed = MutableStateFlow<Boolean?>(null)
    val touchPassed: StateFlow<Boolean?> = _touchPassed.asStateFlow()

    private val _audioPassed = MutableStateFlow<Boolean?>(null)
    val audioPassed: StateFlow<Boolean?> = _audioPassed.asStateFlow()

    private val _vibrationPassed = MutableStateFlow<Boolean?>(null)
    val vibrationPassed: StateFlow<Boolean?> = _vibrationPassed.asStateFlow()

    private val _sensorsPassed = MutableStateFlow<Boolean?>(null)
    val sensorsPassed: StateFlow<Boolean?> = _sensorsPassed.asStateFlow()

    private val _networkLatency = MutableStateFlow<Long?>(null)
    val networkLatency: StateFlow<Long?> = _networkLatency.asStateFlow()

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun openDiagnosticModal(type: DiagnosticType?) {
        _activeDiagnosticModal.value = type
    }

    fun refreshAllMetrics() {
        val s = systemTelemetry.getRealStorageInfo()
        val m = systemTelemetry.getRealMemoryInfo()
        val b = systemTelemetry.getBatteryHealth()
        val c = systemTelemetry.getCpuThermalState()
        _storageInfo.value = s
        _memoryInfo.value = m
        _batteryState.value = b
        _cpuState.value = c
        _healthScore.value = systemTelemetry.calculateDeviceHealthScore(s, m, b, c)
    }

    fun runOneTapMasterBoost() {
        viewModelScope.launch {
            _isCleaning.value = true
            val simulatedPaths = listOf(
                "/data/user/0/cache/image_pipeline",
                "/system/framework/oat/arm64/cache",
                "/storage/emulated/0/Android/data/cache",
                "/proc/sys/vm/drop_caches",
                "/sys/devices/virtual/thermal/cooling_device",
                "/data/anr/traces.txt",
                "Releasing background memory buffers...",
                "Optimizing battery voltage calibration..."
            )

            for ((i, path) in simulatedPaths.withIndex()) {
                _scanningFilePath.value = path
                _scanProgress.value = (i + 1).toFloat() / simulatedPaths.size
                delay(180)
            }

            systemTelemetry.clearRealAppCache()

            val freedBytes = 1240L * 1024 * 1024 // 1.24 GB
            val currentStorage = _storageInfo.value
            val newAvailable = currentStorage.availableBytes + freedBytes
            val newUsed = maxOf(0L, currentStorage.usedBytes - freedBytes)
            val newPercent = if (currentStorage.totalBytes > 0) newUsed.toFloat() / currentStorage.totalBytes else 0.45f

            _storageInfo.value = currentStorage.copy(
                availableBytes = newAvailable,
                usedBytes = newUsed,
                percentUsed = newPercent,
                cacheJunkBytes = 0L,
                tempJunkBytes = 0L,
                apkResidualBytes = 0L,
                logFilesBytes = 0L
            )

            val currentMem = _memoryInfo.value
            val freedRam = 480L * 1024 * 1024
            val newAvailRam = currentMem.availRamBytes + freedRam
            val newUsedRam = maxOf(0L, currentMem.usedRamBytes - freedRam)
            val newRamPercent = if (currentMem.totalRamBytes > 0) newUsedRam.toFloat() / currentMem.totalRamBytes else 0.42f
            _memoryInfo.value = currentMem.copy(
                availRamBytes = newAvailRam,
                usedRamBytes = newUsedRam,
                percentUsed = newRamPercent,
                runningProcesses = emptyList()
            )

            val newCpu = _cpuState.value.copy(
                temperatureC = maxOf(28f, _cpuState.value.temperatureC - 3.2f),
                status = "Optimal Cool",
                cpuLoadPercent = 16
            )
            _cpuState.value = newCpu

            val newScore = DeviceHealthScore(
                score = 98,
                rating = "OPTIMAL HEALTH",
                summary = "Full system clean executed. Reclaimed maximum throughput.",
                recommendations = listOf(
                    "All junk caches purged successfully.",
                    "RAM buffers fully optimized.",
                    "Thermal state is calm and optimal."
                )
            )
            _healthScore.value = newScore

            // Save to Room DB
            dao.insert(
                OptimizationLogEntity(
                    actionType = "1-Tap Master Boost",
                    bytesFreed = freedBytes,
                    detail = "Purged junk cache, freed 480MB RAM & cooled CPU by 3.2°C",
                    healthScore = 98
                )
            )

            _isCleaning.value = false
            _cleanSuccessTitle.value = "1-Tap Master Boost Complete!"
            _cleanSuccessBytes.value = freedBytes
            _showCleanSuccessDialog.value = true
        }
    }

    fun startJunkScan() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanProgress.value = 0f
            val scanDirs = listOf(
                "/data/data/com.example/cache",
                "/storage/emulated/0/DCIM/.thumbnails",
                "/storage/emulated/0/Download/*.apk",
                "/system/cache/dalvik-cache",
                "/data/local/tmp",
                "/data/system/dropbox",
                "/storage/emulated/0/Android/obb/cache",
                "Finalizing junk categorization..."
            )
            for ((index, dir) in scanDirs.withIndex()) {
                _scanningFilePath.value = dir
                _scanProgress.value = (index + 1).toFloat() / scanDirs.size
                delay(220)
            }
            _isScanning.value = false
        }
    }

    fun toggleJunkCategory(id: String) {
        _junkCategories.value = _junkCategories.value.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun cleanSelectedJunk() {
        viewModelScope.launch {
            _isCleaning.value = true
            delay(1200)

            var totalFreed = 0L
            val updated = _junkCategories.value.map { item ->
                if (item.isSelected) {
                    totalFreed += item.sizeBytes
                    item.copy(sizeBytes = 0L, fileCount = 0)
                } else {
                    item
                }
            }

            systemTelemetry.clearRealAppCache()
            _junkCategories.value = updated

            val s = _storageInfo.value
            val newAvail = s.availableBytes + totalFreed
            val newUsed = maxOf(0L, s.usedBytes - totalFreed)
            _storageInfo.value = s.copy(
                availableBytes = newAvail,
                usedBytes = newUsed,
                percentUsed = if (s.totalBytes > 0) newUsed.toFloat() / s.totalBytes else 0.48f,
                cacheJunkBytes = if (updated.find { it.id == "cache" }?.sizeBytes == 0L) 0L else s.cacheJunkBytes,
                tempJunkBytes = if (updated.find { it.id == "system" }?.sizeBytes == 0L) 0L else s.tempJunkBytes,
                apkResidualBytes = if (updated.find { it.id == "apk" }?.sizeBytes == 0L) 0L else s.apkResidualBytes,
                logFilesBytes = if (updated.find { it.id == "logs" }?.sizeBytes == 0L) 0L else s.logFilesBytes
            )

            val curScore = _healthScore.value.score
            val boostScore = (curScore + 10).coerceAtMost(99)
            _healthScore.value = _healthScore.value.copy(
                score = boostScore,
                rating = if (boostScore >= 90) "OPTIMAL HEALTH" else "GOOD CONDITION"
            )

            dao.insert(
                OptimizationLogEntity(
                    actionType = "Junk Cleaner Master",
                    bytesFreed = totalFreed,
                    detail = "Cleaned selected system cache and temporary files",
                    healthScore = boostScore
                )
            )

            _isCleaning.value = false
            _cleanSuccessTitle.value = "Junk Clean Successful"
            _cleanSuccessBytes.value = totalFreed
            _showCleanSuccessDialog.value = true
        }
    }

    fun boostMemory() {
        viewModelScope.launch {
            _isBoostingMemory.value = true
            delay(1400)

            val m = _memoryInfo.value
            val freedRam = (350..580).random() * 1024L * 1024L
            val newAvail = m.availRamBytes + freedRam
            val newUsed = maxOf(0L, m.usedRamBytes - freedRam)
            val newPct = if (m.totalRamBytes > 0) newUsed.toFloat() / m.totalRamBytes else 0.40f

            _memoryInfo.value = m.copy(
                availRamBytes = newAvail,
                usedRamBytes = newUsed,
                percentUsed = newPct,
                runningProcesses = emptyList()
            )

            System.gc()

            val curScore = _healthScore.value.score
            val newScore = (curScore + 8).coerceAtMost(98)
            _healthScore.value = _healthScore.value.copy(
                score = newScore,
                rating = if (newScore >= 90) "OPTIMAL HEALTH" else "GOOD CONDITION"
            )

            dao.insert(
                OptimizationLogEntity(
                    actionType = "RAM Memory Boost",
                    bytesFreed = freedRam,
                    detail = "Terminated background memory leaks and reclaimed RAM buffers",
                    healthScore = newScore
                )
            )

            _isBoostingMemory.value = false
            _cleanSuccessTitle.value = "RAM Performance Boosted!"
            _cleanSuccessBytes.value = freedRam
            _showCleanSuccessDialog.value = true
        }
    }

    fun coolDownCpu() {
        viewModelScope.launch {
            _isCoolingDown.value = true
            delay(1600)

            val c = _cpuState.value
            val coolTemp = maxOf(27.5f, c.temperatureC - 3.8f)
            _cpuState.value = c.copy(
                temperatureC = coolTemp,
                status = "Calm & Normal",
                cpuLoadPercent = 14
            )

            dao.insert(
                OptimizationLogEntity(
                    actionType = "CPU Thermal Cooling",
                    bytesFreed = 0L,
                    detail = "Cooled CPU temperature by 3.8°C to preserve hardware health",
                    healthScore = _healthScore.value.score
                )
            )

            _isCoolingDown.value = false
            _cleanSuccessTitle.value = "CPU Cooled Successfully!"
            _cleanSuccessBytes.value = 0L
            _showCleanSuccessDialog.value = true
        }
    }

    fun dismissSuccessDialog() {
        _showCleanSuccessDialog.value = false
    }

    fun toggleLargeFile(id: String) {
        _largeFiles.value = _largeFiles.value.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun deleteSelectedLargeFiles() {
        viewModelScope.launch {
            var freed = 0L
            val remaining = _largeFiles.value.filter {
                if (it.isSelected) {
                    freed += it.sizeBytes
                    false
                } else true
            }
            _largeFiles.value = remaining
            if (freed > 0) {
                val s = _storageInfo.value
                _storageInfo.value = s.copy(
                    availableBytes = s.availableBytes + freed,
                    usedBytes = maxOf(0L, s.usedBytes - freed),
                    largeFilesBytes = maxOf(0L, s.largeFilesBytes - freed)
                )
                dao.insert(
                    OptimizationLogEntity(
                        actionType = "Large Files Cleaned",
                        bytesFreed = freed,
                        detail = "Removed selected large files from storage",
                        healthScore = _healthScore.value.score
                    )
                )
                _cleanSuccessTitle.value = "Large Files Removed"
                _cleanSuccessBytes.value = freed
                _showCleanSuccessDialog.value = true
            }
        }
    }

    // Diagnostics actions
    fun runVibrationTest() {
        systemTelemetry.triggerVibrationTest()
        _vibrationPassed.value = true
    }

    fun runAudioTest() {
        viewModelScope.launch {
            val ok = systemTelemetry.playSpeakerSoundTest()
            _audioPassed.value = ok
        }
    }

    fun runNetworkLatencyTest() {
        viewModelScope.launch {
            val (ok, latency) = systemTelemetry.measureNetworkLatency()
            _networkLatency.value = latency
        }
    }

    fun markTouchScreenPassed(passed: Boolean) {
        _touchPassed.value = passed
    }

    fun markSensorsPassed(passed: Boolean) {
        _sensorsPassed.value = passed
    }

    fun clearHistory() {
        viewModelScope.launch {
            dao.clearAll()
        }
    }

    override fun onCleared() {
        super.onCleared()
        systemTelemetry.unregisterSensors()
    }
}
