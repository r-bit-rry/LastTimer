package com.lasttimer.app.util

import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "MemoryMonitor"

/**
 * Utility class for memory monitoring and optimization
 */
object MemoryMonitor {
    
    private var hprofDumpsEnabled = false
    
    /**
     * Get current memory info
     * @param context Application context
     * @return MemoryInfo object containing memory details
     */
    fun getMemoryInfo(context: Context): ActivityManager.MemoryInfo {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo
    }
    
    /**
     * Log current memory usage
     * @param context Application context
     * @param tag Optional tag for the log message
     */
    fun logMemoryUsage(context: Context, tag: String = TAG) {
        val memoryInfo = getMemoryInfo(context)
        val availableMegs = memoryInfo.availMem / 1048576L
        val totalMegs = memoryInfo.totalMem / 1048576L
        val percentAvailable = 100 * memoryInfo.availMem / memoryInfo.totalMem
        
        val nativeHeapSize = Debug.getNativeHeapSize() / 1048576L
        val nativeHeapAllocated = Debug.getNativeHeapAllocatedSize() / 1048576L
        
        Log.d(tag, "Memory: $availableMegs MB free, $totalMegs MB total ($percentAvailable% available)")
        Log.d(tag, "Native Heap: $nativeHeapAllocated MB allocated, $nativeHeapSize MB size")
    }
    
    /**
     * Enable/disable heap dumps on low memory
     * @param enabled Whether to enable heap dumps
     */
    fun enableHeapDumps(enabled: Boolean) {
        hprofDumpsEnabled = enabled
    }
    
    /**
     * Create a heap dump file
     * @param context Application context
     * @return File object for the heap dump
     */
    fun createHeapDump(context: Context): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(context.cacheDir, "heap_dump_$timestamp.hprof")
        
        Debug.dumpHprofData(file.absolutePath)
        Log.d(TAG, "Created heap dump at ${file.absolutePath}")
        
        return file
    }
    
    /**
     * Clean up old heap dumps to free space
     * @param context Application context
     * @param maxAgeDays Maximum age of heap dumps to keep (in days)
     */
    fun cleanupOldHeapDumps(context: Context, maxAgeDays: Int = 7) {
        val now = System.currentTimeMillis()
        val maxAgeMs = maxAgeDays * 24 * 60 * 60 * 1000L
        
        val hprofFiles = context.cacheDir.listFiles { file ->
            file.name.startsWith("heap_dump_") && file.name.endsWith(".hprof")
        }
        
        hprofFiles?.forEach { file ->
            if (now - file.lastModified() > maxAgeMs) {
                if (file.delete()) {
                    Log.d(TAG, "Deleted old heap dump: ${file.name}")
                }
            }
        }
    }
    
    /**
     * Composable to monitor memory usage periodically
     * @param label Label for the monitor
     * @param intervalMs Interval between memory checks in milliseconds
     */
    @Composable
    fun MonitorMemoryUsage(label: String, intervalMs: Long = 10000) {
        val context = LocalContext.current
        
        DisposableEffect(Unit) {
            val runnable = object : Runnable {
                override fun run() {
                    logMemoryUsage(context, "$TAG-$label")
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(this, intervalMs)
                }
            }
            
            // Start memory monitoring
            runnable.run()
            
            onDispose {
                // Stop memory monitoring by not re-posting the runnable
            }
        }
    }
}
