package com.lasttimer.app.data.converter

import androidx.room.TypeConverter
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import java.util.Date

class TimerTypeConverter {
    @TypeConverter
    fun fromTimerType(timerType: TimerType): String {
        return timerType.name
    }

    @TypeConverter
    fun toTimerType(value: String): TimerType {
        return enumValueOf(value)
    }
    
    @TypeConverter
    fun fromTimerStatus(timerStatus: TimerStatus): String {
        return timerStatus.name
    }

    @TypeConverter
    fun toTimerStatus(value: String): TimerStatus {
        return enumValueOf(value)
    }
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
