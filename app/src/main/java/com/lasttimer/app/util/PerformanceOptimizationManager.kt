package com.lasttimer.app.util

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.StrictMode
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manager class to coordinate performance optimizations
 */
class PerformanceOptimizationManager(
    private val application: Application,
    private val isDebugMode: Boolean = false
) : DefaultLifecycleObserver {
    
    private val TAG = "PerformanceManager"
    private val scope = CoroutineScope(Dispatchers.Default + Job())
    
    // Configuration
    private var strictModeEnabled = false
    private var memoryMonitoringEnabled = true
    private var performanceTrackingEnabled = true
    private var fileCleanupEnabled = true
    
    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    
    /**
     * Initialize performance optimizations
     */
    fun init() {
        Log.d(TAG, "Initializing performance optimization manager")
        
        if (strictModeEnabled && isDebugMode) {
            enableStrictMode()
        }
        
        if (fileCleanupEnabled) {
            cleanupOldFiles()
        }
        
        if (memoryMonitoringEnabled) {
            // Log initial memory info
            MemoryMonitor.logMemoryUsage(application, TAG)
        }
    }
    
    /**
     * Enable strict mode for development/debugging
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectActivityLeaks()
                .detectFileUriExposure()
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        detectContentUriWithoutPermission()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        detectNonSdkApiUsage()
                    }
                }
                .penaltyLog()
                .build()
        )
    }
    
    /**
     * Clean up old cache and temporary files
     */
    private fun cleanupOldFiles() {
        scope.launch {
            try {
                // Clean up app cache directory
                val cacheDir = application.cacheDir
                cleanDirectory(cacheDir, 7) // Files older than 7 days
                
                // Clean up external cache if available
                application.externalCacheDir?.let { 
                    cleanDirectory(it, 7) 
                }
                
                Log.d(TAG, "Completed file cleanup")
            } catch (e: Exception) {
                Log.e(TAG, "Error during file cleanup: ${e.message}")
            }
        }
    }
    
    /**
     * Clean files in a directory older than specified days
     * @param directory Directory to clean
     * @param maxAgeDays Maximum age of files to keep
     */
    private fun cleanDirectory(directory: File, maxAgeDays: Int) {
        val now = System.currentTimeMillis()
        val maxAgeMs = maxAgeDays * 24 * 60 * 60 * 1000L
        
        directory.listFiles()?.forEach { file ->
            if (file.isFile && now - file.lastModified() > maxAgeMs) {
                if (file.delete()) {
                    Log.d(TAG, "Deleted old file: ${file.name}")
                }
            }
        }
    }
    
    /**
     * Configure optimization settings
     */
    fun configure(
        strictMode: Boolean = false,
        memoryMonitoring: Boolean = true,
        performanceTracking: Boolean = true,
        fileCleanup: Boolean = true
    ) {
        strictModeEnabled = strictMode
        memoryMonitoringEnabled = memoryMonitoring
        performanceTrackingEnabled = performanceTracking
        fileCleanupEnabled = fileCleanup
    }
    
    /**
     * Create a performance report
     * @return String containing the report
     */
    fun generatePerformanceReport(): String {
        val sb = StringBuilder()
        sb.append("Performance Report - ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
        sb.append("=".repeat(50)).append("\n")
        
        // Memory info
        val memInfo = MemoryMonitor.getMemoryInfo(application)
        sb.append("MEMORY\n")
        sb.append("Available: ${memInfo.availMem / 1048576L} MB\n")
        sb.append("Total: ${memInfo.totalMem / 1048576L} MB\n")
        sb.append("Low memory: ${memInfo.lowMemory}\n")
        sb.append("Threshold: ${memInfo.threshold / 1048576L} MB\n\n")
        
        // Composable render times
        if (performanceTrackingEnabled) {
            val renderTimes = PerformanceMonitor.getCompositionalReport()
            if (renderTimes.isNotEmpty()) {
                sb.append("RENDER TIMES\n")
                renderTimes.forEach { (composable, time) ->
                    sb.append("$composable: $time ms\n")
                }
                sb.append("\n")
            }
        }
        
        return sb.toString()
    }
    
    /**
     * Write performance report to a file
     * @return File containing the report, or null if write fails
     */
    fun writePerformanceReportToFile(): File? {
        try {
            val report = generatePerformanceReport()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val file = File(application.cacheDir, "performance_report_$timestamp.txt")
            
            file.writeText(report)
            Log.d(TAG, "Wrote performance report to ${file.absolutePath}")
            
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error writing performance report: ${e.message}")
            return null
        }
    }
    
    override fun onStart(owner: LifecycleOwner) {
        if (memoryMonitoringEnabled) {
            Log.d(TAG, "App moved to foreground")
            MemoryMonitor.logMemoryUsage(application, "$TAG-Foreground")
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        if (memoryMonitoringEnabled) {
            Log.d(TAG, "App moved to background")
            MemoryMonitor.logMemoryUsage(application, "$TAG-Background")
        }
        
        // Clear memory-intensive resources when app goes to background
        ImageLoader.clearCache()
    }
}
