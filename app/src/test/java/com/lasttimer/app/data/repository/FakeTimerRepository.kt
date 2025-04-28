package com.lasttimer.app.data.repository

import com.lasttimer.app.data.dao.TimerDao
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import java.util.UUID

/**
 * Fake repository implementation for testing
 */
class FakeTimerRepository : TimerRepository {
    
    private val timers = mutableListOf<Timer>()
    
    override fun getAllTimers(): Flow<List<Timer>> {
        return flowOf(timers)
    }
    
    override fun getTimersByType(type: TimerType): Flow<List<Timer>> {
        return flowOf(timers.filter { it.type == type })
    }
    
    override fun getAllTemplates(): Flow<List<Timer>> {
        return flowOf(timers.filter { it.isTemplate })
    }
    
    override fun getTimerById(id: String): Flow<Timer?> {
        return flowOf(timers.find { it.id == id })
    }
    
    override suspend fun saveTimer(timer: Timer) {
        val existingIndex = timers.indexOfFirst { it.id == timer.id }
        if (existingIndex >= 0) {
            timers[existingIndex] = timer
        } else {
            timers.add(timer)
        }
    }
    
    override suspend fun updateTimerStatus(id: String, status: TimerStatus) {
        val existingIndex = timers.indexOfFirst { it.id == id }
        if (existingIndex >= 0) {
            val timer = timers[existingIndex]
            timers[existingIndex] = timer.copy(status = status)
        }
    }
    
    override suspend fun updateElapsedTime(id: String, elapsedTimeMillis: Long) {
        val existingIndex = timers.indexOfFirst { it.id == id }
        if (existingIndex >= 0) {
            val timer = timers[existingIndex]
            timers[existingIndex] = timer.copy(elapsedTimeMillis = elapsedTimeMillis)
        }
    }
    
    override suspend fun updateLastUsedAt(id: String) {
        val existingIndex = timers.indexOfFirst { it.id == id }
        if (existingIndex >= 0) {
            val timer = timers[existingIndex]
            timers[existingIndex] = timer.copy(lastUsedAt = Date())
        }
    }
    
    override suspend fun deleteTimer(timer: Timer) {
        timers.removeIf { it.id == timer.id }
    }
    
    // Helper method for tests to add sample data
    fun addSampleData() {
        val now = Date()
        
        // Add countdown timer
        timers.add(Timer(
            id = UUID.randomUUID().toString(),
            name = "Test Countdown",
            type = TimerType.COUNTDOWN,
            durationMillis = 60000, // 1 minute
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = now,
            lastUsedAt = now
        ))
        
        // Add stopwatch
        timers.add(Timer(
            id = UUID.randomUUID().toString(),
            name = "Test Stopwatch",
            type = TimerType.STOPWATCH,
            durationMillis = null,
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = now,
            lastUsedAt = now
        ))
        
        // Add date countdown
        timers.add(Timer(
            id = UUID.randomUUID().toString(),
            name = "Test Date Countdown",
            type = TimerType.DATE_COUNTDOWN,
            targetDate = Date(now.time + 86400000), // Tomorrow
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = now,
            lastUsedAt = now
        ))
        
        // Add template
        timers.add(Timer(
            id = UUID.randomUUID().toString(),
            name = "Test Template",
            type = TimerType.COUNTDOWN,
            durationMillis = 300000, // 5 minutes
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = now,
            lastUsedAt = now,
            isTemplate = true
        ))
    }
    
    // Helper method to clear data
    fun clearData() {
        timers.clear()
    }
}
