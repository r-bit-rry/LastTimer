package com.lasttimer.app.data.repository

import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerGroup
import com.lasttimer.app.data.model.TimerGroupItem
import com.lasttimer.app.data.model.TimerLap
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Interface for timer repository operations to enable easier testing
 */
interface ITimerRepository {
    // Timer operations
    fun getAllTimers(): Flow<List<Timer>>
    fun getTimersByType(type: TimerType): Flow<List<Timer>>
    fun getTimerById(id: String): Flow<Timer?>
    fun getAllTemplates(): Flow<List<Timer>>
    suspend fun saveTimer(timer: Timer): Long
    suspend fun updateTimer(timer: Timer)
    suspend fun deleteTimer(timer: Timer)
    suspend fun updateTimerStatus(timerId: String, status: TimerStatus)
    suspend fun updateElapsedTime(timerId: String, elapsedTimeMillis: Long)
    suspend fun updateLastUsedAt(timerId: String, lastUsedAt: Date = Date())
    suspend fun resetTimer(timerId: String)
    fun getTimersByStatus(status: TimerStatus): Flow<List<Timer>>
    fun getTimersByCategory(category: String): Flow<List<Timer>>

    // Timer Group operations
    fun getAllGroups(): Flow<List<TimerGroup>>
    fun getGroupById(id: String): Flow<TimerGroup?>
    suspend fun saveGroup(timerGroup: TimerGroup): Long
    suspend fun updateGroup(timerGroup: TimerGroup)
    suspend fun deleteGroup(timerGroup: TimerGroup)
    suspend fun updateGroupLastUsedAt(groupId: String, lastUsedAt: Date = Date())
    fun getGroupItems(groupId: String): Flow<List<TimerGroupItem>>
    suspend fun saveGroupItem(item: TimerGroupItem): Long
    suspend fun deleteGroupItem(item: TimerGroupItem)
    suspend fun deleteAllGroupItems(groupId: String)
    suspend fun deleteItemAndReorder(groupId: String, position: Int)
    
    // Timer Lap operations
    fun getLapsByTimerId(timerId: String): Flow<List<TimerLap>>
    suspend fun saveLap(lap: TimerLap): Long
    suspend fun deleteLap(lap: TimerLap)
    suspend fun deleteAllLapsForTimer(timerId: String)
    suspend fun getLapCount(timerId: String): Int
}
