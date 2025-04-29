package com.lasttimer.app.data.repository

import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerGroup
import com.lasttimer.app.data.model.TimerGroupItem
import com.lasttimer.app.data.model.TimerLap
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import java.util.UUID

/**
 * Fake repository implementation for testing
 */
class FakeTimerRepository {
    
    private val timers = mutableListOf<Timer>()
    private val timerLaps = mutableListOf<TimerLap>()
    private val timerGroups = mutableListOf<TimerGroup>()
    private val timerGroupItems = mutableListOf<TimerGroupItem>()
    
    fun getAllTimers(): Flow<List<Timer>> {
        return flowOf(timers)
    }
    
    fun getTimersByType(type: TimerType): Flow<List<Timer>> {
        return flowOf(timers.filter { it.type == type })
    }
    
    fun getAllTemplates(): Flow<List<Timer>> {
        return flowOf(timers.filter { it.isTemplate })
    }
    
    fun getTimerById(id: String): Flow<Timer?> {
        return flowOf(timers.find { it.id == id })
    }
    
    suspend fun saveTimer(timer: Timer): Long {
        val existingIndex = timers.indexOfFirst { it.id == timer.id }
        if (existingIndex >= 0) {
            timers[existingIndex] = timer
        } else {
            timers.add(timer)
        }
        return timer.id.hashCode().toLong()
    }
    
    suspend fun updateTimer(timer: Timer) {
        val existingIndex = timers.indexOfFirst { it.id == timer.id }
        if (existingIndex >= 0) {
            timers[existingIndex] = timer
        }
    }
    
    suspend fun updateTimerStatus(timerId: String, status: TimerStatus) {
        val existingIndex = timers.indexOfFirst { it.id == timerId }
        if (existingIndex >= 0) {
            val timer = timers[existingIndex]
            timers[existingIndex] = timer.copy(status = status)
        }
    }
    
    suspend fun updateElapsedTime(timerId: String, elapsedTimeMillis: Long) {
        val existingIndex = timers.indexOfFirst { it.id == timerId }
        if (existingIndex >= 0) {
            val timer = timers[existingIndex]
            timers[existingIndex] = timer.copy(elapsedTimeMillis = elapsedTimeMillis)
        }
    }
    
    suspend fun updateLastUsedAt(timerId: String, lastUsedAt: Date = Date()) {
        val existingIndex = timers.indexOfFirst { it.id == timerId }
        if (existingIndex >= 0) {
            val timer = timers[existingIndex]
            timers[existingIndex] = timer.copy(lastUsedAt = lastUsedAt)
        }
    }
    
    suspend fun deleteTimer(timer: Timer) {
        timers.removeIf { it.id == timer.id }
    }
    
    suspend fun resetTimer(timerId: String) {
        val existingIndex = timers.indexOfFirst { it.id == timerId }
        if (existingIndex >= 0) {
            val timer = timers[existingIndex]
            timers[existingIndex] = timer.copy(
                elapsedTimeMillis = 0,
                status = TimerStatus.IDLE
            )
        }
    }
    
    fun getTimersByStatus(status: TimerStatus): Flow<List<Timer>> {
        return flowOf(timers.filter { it.status == status })
    }
    
    fun getTimersByCategory(category: String): Flow<List<Timer>> {
        return flowOf(timers.filter { it.category == category })
    }
    
    // Timer Group operations
    fun getAllGroups(): Flow<List<TimerGroup>> {
        return flowOf(timerGroups)
    }
    
    fun getGroupById(id: String): Flow<TimerGroup?> {
        return flowOf(timerGroups.find { it.id == id })
    }
    
    suspend fun saveGroup(timerGroup: TimerGroup): Long {
        val existingIndex = timerGroups.indexOfFirst { it.id == timerGroup.id }
        if (existingIndex >= 0) {
            timerGroups[existingIndex] = timerGroup
        } else {
            timerGroups.add(timerGroup)
        }
        return timerGroup.id.hashCode().toLong()
    }
    
    suspend fun updateGroup(timerGroup: TimerGroup) {
        val existingIndex = timerGroups.indexOfFirst { it.id == timerGroup.id }
        if (existingIndex >= 0) {
            timerGroups[existingIndex] = timerGroup
        }
    }
    
    suspend fun deleteGroup(timerGroup: TimerGroup) {
        timerGroups.removeIf { it.id == timerGroup.id }
    }
    
    suspend fun updateGroupLastUsedAt(groupId: String, lastUsedAt: Date = Date()) {
        val existingIndex = timerGroups.indexOfFirst { it.id == groupId }
        if (existingIndex >= 0) {
            val group = timerGroups[existingIndex]
            timerGroups[existingIndex] = group.copy(lastUsedAt = lastUsedAt)
        }
    }
    
    fun getGroupItems(groupId: String): Flow<List<TimerGroupItem>> {
        return flowOf(timerGroupItems.filter { it.groupId == groupId }.sortedBy { it.position })
    }
    
    suspend fun saveGroupItem(item: TimerGroupItem): Long {
        val existingIndex = timerGroupItems.indexOfFirst { it.groupId == item.groupId && it.timerId == item.timerId && it.position == item.position }
        if (existingIndex >= 0) {
            timerGroupItems[existingIndex] = item
        } else {
            timerGroupItems.add(item)
        }
        return (item.groupId + item.timerId + item.position).hashCode().toLong()
    }
    
    suspend fun deleteGroupItem(item: TimerGroupItem) {
        timerGroupItems.removeIf { it.groupId == item.groupId && it.timerId == item.timerId && it.position == item.position }
    }
    
    suspend fun deleteAllGroupItems(groupId: String) {
        timerGroupItems.removeIf { it.groupId == groupId }
    }
    
    suspend fun deleteItemAndReorder(groupId: String, position: Int) {
        // Remove the item at the specified position
        timerGroupItems.removeIf { it.groupId == groupId && it.position == position }
        
        // Reorder remaining items
        timerGroupItems.forEach { item ->
            if (item.groupId == groupId && item.position > position) {
                item.position = item.position - 1
            }
        }
    }
    
    fun getLapsByTimerId(timerId: String): Flow<List<TimerLap>> {
        return flowOf(timerLaps.filter { it.timerId == timerId }.sortedBy { it.lapNumber })
    }
    
    suspend fun saveLap(lap: TimerLap): Long {
        timerLaps.add(lap)
        return lap.id.hashCode().toLong()
    }
    
    suspend fun deleteLap(lap: TimerLap) {
        timerLaps.removeIf { it.id == lap.id }
    }
    
    suspend fun deleteAllLapsForTimer(timerId: String) {
        timerLaps.removeIf { it.timerId == timerId }
    }
    
    suspend fun getLapCount(timerId: String): Int {
        return timerLaps.count { it.timerId == timerId }
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
        timerLaps.clear()
        timerGroups.clear()
        timerGroupItems.clear()
    }
}
