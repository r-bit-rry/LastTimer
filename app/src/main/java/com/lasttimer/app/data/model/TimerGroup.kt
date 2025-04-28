package com.lasttimer.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a group of timers that can be chained together.
 */
@Entity(tableName = "timer_groups")
data class TimerGroup(
    @PrimaryKey
    val id: String,
    
    /**
     * Name of the timer group
     */
    val name: String,
    
    /**
     * Description of the group (optional)
     */
    val description: String? = null,
    
    /**
     * Date when the group was created
     */
    val createdAt: Date,
    
    /**
     * Date when the group was last used/modified
     */
    val lastUsedAt: Date? = null,
    
    /**
     * Whether to automatically start the next timer in the sequence
     */
    val autoStartNext: Boolean = true,
    
    /**
     * Whether the entire group should repeat once completed
     */
    val repeatGroup: Boolean = false,
    
    /**
     * Number of times to repeat the group (null means infinite)
     */
    val repeatCount: Int? = null,
    
    /**
     * Current index of the timer being played in the group
     */
    val currentTimerIndex: Int = 0,
    
    /**
     * Current cycle/iteration count when repeating
     */
    val currentRepeatCycle: Int = 0
)
