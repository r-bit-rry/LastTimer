package com.lasttimer.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lasttimer.app.data.model.TimerLap
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerLapDao {
    @Query("SELECT * FROM timer_laps WHERE timerId = :timerId ORDER BY lapNumber ASC")
    fun getLapsByTimerId(timerId: String): Flow<List<TimerLap>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLap(lap: TimerLap): Long
    
    @Delete
    suspend fun deleteLap(lap: TimerLap)
    
    @Query("DELETE FROM timer_laps WHERE timerId = :timerId")
    suspend fun deleteAllLapsForTimer(timerId: String)
    
    @Query("SELECT COUNT(*) FROM timer_laps WHERE timerId = :timerId")
    suspend fun getLapCount(timerId: String): Int
}
