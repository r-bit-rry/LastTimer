package com.lasttimer.app.util

import java.util.concurrent.TimeUnit

/**
 * Formats time in milliseconds to a readable string format
 */
fun formatTime(millis: Long, totalDuration: Long? = null): String {
    val isCountdown = totalDuration != null && totalDuration > 0
    
    // For countdown timers, calculate remaining time
    val timeToFormat = if (isCountdown) {
        val remaining = totalDuration!! - millis
        if (remaining < 0) 0 else remaining
    } else {
        millis
    }
    
    val hours = TimeUnit.MILLISECONDS.toHours(timeToFormat)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeToFormat) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeToFormat) % 60
    val tenthSeconds = (timeToFormat / 100) % 10
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d.%01d", minutes, seconds, tenthSeconds)
    }
}

/**
 * Formats milliseconds to HH:MM:SS format
 */
fun formatTimeHhMmSs(millis: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(millis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

/**
 * Formats milliseconds to MM:SS format
 */
fun formatTimeMinSec(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    
    return String.format("%02d:%02d", minutes, seconds)
}

/**
 * Formats milliseconds to MM:SS.T format (with tenths of a second)
 */
fun formatTimeWithTenths(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    val tenthSeconds = (millis / 100) % 10
    
    return String.format("%02d:%02d.%01d", minutes, seconds, tenthSeconds)
}
