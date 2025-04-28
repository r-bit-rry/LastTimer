package com.lasttimer.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.lasttimer.app.data.model.TimerGroup
import com.lasttimer.app.data.model.TimerGroupItem
import com.lasttimer.app.data.model.TimerWithGroup
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TimerGroupDao {
    @Query("SELECT * FROM timer_groups ORDER BY lastUsedAt DESC NULLS LAST")
    fun getAllGroups(): Flow<List<TimerGroup>>
    
    @Query("SELECT * FROM timer_groups WHERE id = :id")
    fun getGroupById(id: String): Flow<TimerGroup?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(timerGroup: TimerGroup): Long
    
    @Update
    suspend fun updateGroup(timerGroup: TimerGroup)
    
    @Delete
    suspend fun deleteGroup(timerGroup: TimerGroup)
    
    @Query("UPDATE timer_groups SET lastUsedAt = :lastUsedAt WHERE id = :groupId")
    suspend fun updateLastUsedAt(groupId: String, lastUsedAt: Date)
    
    @Query("SELECT * FROM timer_group_items WHERE groupId = :groupId ORDER BY position ASC")
    fun getGroupItems(groupId: String): Flow<List<TimerGroupItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupItem(item: TimerGroupItem)
    
    @Delete
    suspend fun deleteGroupItem(item: TimerGroupItem)
    
    @Query("DELETE FROM timer_group_items WHERE groupId = :groupId")
    suspend fun deleteAllGroupItems(groupId: String)
    
    @Query("UPDATE timer_group_items SET position = position - 1 WHERE groupId = :groupId AND position > :position")
    suspend fun decrementPositionsAfter(groupId: String, position: Int)
    
    @Transaction
    suspend fun deleteItemAndReorder(groupId: String, position: Int) {
        deleteItemAtPosition(groupId, position)
        decrementPositionsAfter(groupId, position)
    }
    
    @Query("DELETE FROM timer_group_items WHERE groupId = :groupId AND position = :position")
    suspend fun deleteItemAtPosition(groupId: String, position: Int)
    
    @Transaction
    @Query("SELECT * FROM timer_groups")
    fun getGroupsWithTimers(): Flow<List<TimerWithGroup>>
}
