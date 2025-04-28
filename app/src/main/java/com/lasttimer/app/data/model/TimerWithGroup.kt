package com.lasttimer.app.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TimerWithGroup(
    @Embedded val group: TimerGroup,
    @Relation(
        parentColumn = "id",
        entityColumn = "id", 
        associateBy = Junction(
            value = TimerGroupItem::class,
            parentColumn = "groupId",
            entityColumn = "timerId"
        )
    )
    val timers: List<Timer>
)
