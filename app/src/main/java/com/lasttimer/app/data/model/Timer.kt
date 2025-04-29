package com.lasttimer.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.lasttimer.app.data.converter.DateConverter
import com.lasttimer.app.data.converter.TimerTypeConverter
import java.util.Date
import java.util.UUID

enum class TimerType {
    COUNTDOWN,
    STOPWATCH,
    DATE_COUNTDOWN
}

enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

@Entity(tableName = "timers")
@TypeConverters(TimerTypeConverter::class, DateConverter::class)
data class Timer(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TimerType,
    val durationMillis: Long? = null, // For COUNTDOWN
    val targetDate: Date? = null,     // For DATE_COUNTDOWN
    val createdAt: Date = Date(),
    val lastUsedAt: Date? = null,
    val category: String? = null,
    val vibrationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val isTemplate: Boolean = false,
    val repeat: Boolean = false,
    val repeatCount: Int = 0,  // 0 means infinite
    val elapsedTimeMillis: Long = 0,
    val status: TimerStatus = TimerStatus.IDLE
)

@Entity(tableName = "timer_group_items",
    primaryKeys = ["groupId", "timerId", "position"])
data class TimerGroupItem(
    val groupId: String,
    val timerId: String,
    val position: Int,
    val durationOverrideMillis: Long? = null
)

@Entity(tableName = "timer_laps")
data class TimerLap(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timerId: String,
    val lapNumber: Int,
    val timestamp: Date = Date(),
    val elapsedTimeMillis: Long
)
