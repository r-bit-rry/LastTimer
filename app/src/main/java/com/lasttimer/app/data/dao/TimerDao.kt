package com.lasttimer.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TimerDao {
    @Query("SELECT * FROM timers WHERE isTemplate = 0 ORDER BY lastUsedAt DESC NULLS LAST")
    fun getAllTimers(): Flow<List<Timer>>
    
    @Query("SELECT * FROM timers WHERE type = :type AND isTemplate = 0 ORDER BY lastUsedAt DESC NULLS LAST")
    fun getTimersByType(type: TimerType): Flow<List<Timer>>
    
    @Query("SELECT * FROM timers WHERE id = :id")
    fun getTimerById(id: String): Flow<Timer?>
    
    @Query("SELECT * FROM timers WHERE isTemplate = 1 ORDER BY name ASC")
    fun getAllTemplates(): Flow<List<Timer>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimer(timer: Timer): Long
    
    @Update
    suspend fun updateTimer(timer: Timer)
    
    @Delete
    suspend fun deleteTimer(timer: Timer)
    
    @Query("UPDATE timers SET status = :status WHERE id = :timerId")
    suspend fun updateTimerStatus(timerId: String, status: TimerStatus)
    
    @Query("UPDATE timers SET elapsedTimeMillis = :elapsedTimeMillis WHERE id = :timerId")
    suspend fun updateElapsedTime(timerId: String, elapsedTimeMillis: Long)
    
    @Query("UPDATE timers SET lastUsedAt = :lastUsedAt WHERE id = :timerId")
    suspend fun updateLastUsedAt(timerId: String, lastUsedAt: Date)
    
    @Transaction
    suspend fun resetTimer(timerId: String) {
        updateElapsedTime(timerId, 0)
        updateTimerStatus(timerId, TimerStatus.IDLE)
    }
    
    @Query("SELECT * FROM timers WHERE status = :status")
    fun getTimersByStatus(status: TimerStatus): Flow<List<Timer>>
    
    @Query("SELECT * FROM timers WHERE category = :category")
    fun getTimersByCategory(category: String): Flow<List<Timer>>
}
