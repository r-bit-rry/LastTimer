package com.lasttimer.app.data.repository

import com.lasttimer.app.data.dao.TimerDao
import com.lasttimer.app.data.dao.TimerGroupDao
import com.lasttimer.app.data.dao.TimerLapDao
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerGroup
import com.lasttimer.app.data.model.TimerGroupItem
import com.lasttimer.app.data.model.TimerLap
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerRepository @Inject constructor(
    private val timerDao: TimerDao,
    private val timerGroupDao: TimerGroupDao,
    private val timerLapDao: TimerLapDao
) {
    // Timer operations
    fun getAllTimers(): Flow<List<Timer>> = timerDao.getAllTimers()
    
    fun getTimersByType(type: TimerType): Flow<List<Timer>> = timerDao.getTimersByType(type)
    
    fun getTimerById(id: String): Flow<Timer?> = timerDao.getTimerById(id)
    
    fun getAllTemplates(): Flow<List<Timer>> = timerDao.getAllTemplates()
    
    suspend fun saveTimer(timer: Timer) = timerDao.insertTimer(timer)
    
    suspend fun updateTimer(timer: Timer) = timerDao.updateTimer(timer)
    
    suspend fun deleteTimer(timer: Timer) = timerDao.deleteTimer(timer)
    
    suspend fun updateTimerStatus(timerId: String, status: TimerStatus) = 
        timerDao.updateTimerStatus(timerId, status)
    
    suspend fun updateElapsedTime(timerId: String, elapsedTimeMillis: Long) = 
        timerDao.updateElapsedTime(timerId, elapsedTimeMillis)
    
    suspend fun updateLastUsedAt(timerId: String, lastUsedAt: Date = Date()) = 
        timerDao.updateLastUsedAt(timerId, lastUsedAt)
    
    suspend fun resetTimer(timerId: String) = timerDao.resetTimer(timerId)
    
    fun getTimersByStatus(status: TimerStatus): Flow<List<Timer>> = 
        timerDao.getTimersByStatus(status)
    
    fun getTimersByCategory(category: String): Flow<List<Timer>> = 
        timerDao.getTimersByCategory(category)
    
    // Timer Group operations
    fun getAllGroups(): Flow<List<TimerGroup>> = timerGroupDao.getAllGroups()
    
    fun getGroupById(id: String): Flow<TimerGroup?> = timerGroupDao.getGroupById(id)
    
    suspend fun saveGroup(timerGroup: TimerGroup) = timerGroupDao.insertGroup(timerGroup)
    
    suspend fun updateGroup(timerGroup: TimerGroup) = timerGroupDao.updateGroup(timerGroup)
    
    suspend fun deleteGroup(timerGroup: TimerGroup) = timerGroupDao.deleteGroup(timerGroup)
    
    suspend fun updateGroupLastUsedAt(groupId: String, lastUsedAt: Date = Date()) = 
        timerGroupDao.updateLastUsedAt(groupId, lastUsedAt)
    
    fun getGroupItems(groupId: String): Flow<List<TimerGroupItem>> = 
        timerGroupDao.getGroupItems(groupId)
    
    suspend fun saveGroupItem(item: TimerGroupItem) = timerGroupDao.insertGroupItem(item)
    
    suspend fun deleteGroupItem(item: TimerGroupItem) = timerGroupDao.deleteGroupItem(item)
    
    suspend fun deleteAllGroupItems(groupId: String) = timerGroupDao.deleteAllGroupItems(groupId)
    
    suspend fun deleteItemAndReorder(groupId: String, position: Int) = 
        timerGroupDao.deleteItemAndReorder(groupId, position)
    
    // Timer Lap operations
    fun getLapsByTimerId(timerId: String): Flow<List<TimerLap>> = 
        timerLapDao.getLapsByTimerId(timerId)
    
    suspend fun saveLap(lap: TimerLap) = timerLapDao.insertLap(lap)
    
    suspend fun deleteLap(lap: TimerLap) = timerLapDao.deleteLap(lap)
    
    suspend fun deleteAllLapsForTimer(timerId: String) = 
        timerLapDao.deleteAllLapsForTimer(timerId)
    
    suspend fun getLapCount(timerId: String): Int = timerLapDao.getLapCount(timerId)
}
