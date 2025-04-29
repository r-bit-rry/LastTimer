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
) : ITimerRepository {
    // Timer operations
    override fun getAllTimers(): Flow<List<Timer>> = timerDao.getAllTimers()
    
    override fun getTimersByType(type: TimerType): Flow<List<Timer>> = timerDao.getTimersByType(type)
    
    override fun getTimerById(id: String): Flow<Timer?> = timerDao.getTimerById(id)
    
    override fun getAllTemplates(): Flow<List<Timer>> = timerDao.getAllTemplates()
    
    override suspend fun saveTimer(timer: Timer): Long = timerDao.insertTimer(timer)
    
    override suspend fun updateTimer(timer: Timer) = timerDao.updateTimer(timer)
    
    override suspend fun deleteTimer(timer: Timer) = timerDao.deleteTimer(timer)
    
    override suspend fun updateTimerStatus(timerId: String, status: TimerStatus) = 
        timerDao.updateTimerStatus(timerId, status)
    
    override suspend fun updateElapsedTime(timerId: String, elapsedTimeMillis: Long) = 
        timerDao.updateElapsedTime(timerId, elapsedTimeMillis)
    
    override suspend fun updateLastUsedAt(timerId: String, lastUsedAt: Date) = 
        timerDao.updateLastUsedAt(timerId, lastUsedAt)
    
    override suspend fun resetTimer(timerId: String) = timerDao.resetTimer(timerId)
    
    override fun getTimersByStatus(status: TimerStatus): Flow<List<Timer>> = 
        timerDao.getTimersByStatus(status)
    
    override fun getTimersByCategory(category: String): Flow<List<Timer>> = 
        timerDao.getTimersByCategory(category)
    
    // Timer Group operations
    override fun getAllGroups(): Flow<List<TimerGroup>> = timerGroupDao.getAllGroups()
    
    override fun getGroupById(id: String): Flow<TimerGroup?> = timerGroupDao.getGroupById(id)
    
    override suspend fun saveGroup(timerGroup: TimerGroup): Long = timerGroupDao.insertGroup(timerGroup)
    
    override suspend fun updateGroup(timerGroup: TimerGroup) = timerGroupDao.updateGroup(timerGroup)
    
    override suspend fun deleteGroup(timerGroup: TimerGroup) = timerGroupDao.deleteGroup(timerGroup)
    
    override suspend fun updateGroupLastUsedAt(groupId: String, lastUsedAt: Date) = 
        timerGroupDao.updateLastUsedAt(groupId, lastUsedAt)
    
    override fun getGroupItems(groupId: String): Flow<List<TimerGroupItem>> = 
        timerGroupDao.getGroupItems(groupId)
    
    override suspend fun saveGroupItem(item: TimerGroupItem): Long = timerGroupDao.insertGroupItem(item)
    
    override suspend fun deleteGroupItem(item: TimerGroupItem) = timerGroupDao.deleteGroupItem(item)
    
    override suspend fun deleteAllGroupItems(groupId: String) = timerGroupDao.deleteAllGroupItems(groupId)
    
    override suspend fun deleteItemAndReorder(groupId: String, position: Int) = 
        timerGroupDao.deleteItemAndReorder(groupId, position)
    
    // Timer Lap operations
    override fun getLapsByTimerId(timerId: String): Flow<List<TimerLap>> = 
        timerLapDao.getLapsByTimerId(timerId)
    
    override suspend fun saveLap(lap: TimerLap) = timerLapDao.insertLap(lap)
    
    override suspend fun deleteLap(lap: TimerLap) = timerLapDao.deleteLap(lap)
    
    override suspend fun deleteAllLapsForTimer(timerId: String) = 
        timerLapDao.deleteAllLapsForTimer(timerId)
    
    override suspend fun getLapCount(timerId: String): Int = timerLapDao.getLapCount(timerId)
}
