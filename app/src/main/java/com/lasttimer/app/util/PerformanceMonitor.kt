package com.lasttimer.app.util

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

private const val TAG = "PerformanceMonitor"

/**
 * Utility class for performance monitoring and optimization
 */
object PerformanceMonitor {
    
    private val operations = mutableMapOf<String, Long>()
    private val compositeTimes = mutableMapOf<String, Long>()
    
    /**
     * Start timing an operation
     * @param operationName Name of the operation to time
     */
    fun startOperation(operationName: String) {
        operations[operationName] = SystemClock.elapsedRealtime()
    }
    
    /**
     * End timing an operation and log the result
     * @param operationName Name of the operation to end
     * @param logResults Whether to log results (defaults to true)
     * @return Duration of the operation in milliseconds, or -1 if operation wasn't started
     */
    fun endOperation(operationName: String, logResults: Boolean = true): Long {
        val startTime = operations[operationName] ?: return -1
        val duration = SystemClock.elapsedRealtime() - startTime
        
        if (logResults) {
            Log.d(TAG, "Operation '$operationName' completed in $duration ms")
        }
        
        operations.remove(operationName)
        return duration
    }
    
    /**
     * Track render time of a composable
     * @param composableName Name of the composable to track
     */
    @Composable
    fun TrackComposableRenderTime(composableName: String) {
        val startTime = remember { SystemClock.elapsedRealtime() }
        val lifecycleOwner = LocalLifecycleOwner.current
        
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_CREATE) {
                    val renderTime = SystemClock.elapsedRealtime() - startTime
                    compositeTimes[composableName] = renderTime
                    Log.d(TAG, "Composable '$composableName' initial render: $renderTime ms")
                }
            }
            
            lifecycleOwner.lifecycle.addObserver(observer)
            
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }
    
    /**
     * Get report of all compositional times
     * @return Map of composable names to render times
     */
    fun getCompositionalReport(): Map<String, Long> {
        return compositeTimes.toMap()
    }
    
    /**
     * Clear all timing data
     */
    fun reset() {
        operations.clear()
        compositeTimes.clear()
    }
}
