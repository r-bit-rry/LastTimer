package com.lasttimer.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date
import java.util.UUID

class TimerTest {
    
    @Test
    fun `countdown timer properties should be set correctly`() {
        // Given
        val id = UUID.randomUUID().toString()
        val now = Date()
        
        // When
        val timer = Timer(
            id = id,
            name = "Test Timer",
            type = TimerType.COUNTDOWN,
            durationMillis = 60000, // 1 minute
            elapsedTimeMillis = 30000, // 30 seconds elapsed
            status = TimerStatus.RUNNING,
            createdAt = now,
            lastUsedAt = now
        )
        
        // Then
        assertEquals(id, timer.id)
        assertEquals("Test Timer", timer.name)
        assertEquals(TimerType.COUNTDOWN, timer.type)
        assertEquals(60000L, timer.durationMillis)
        assertEquals(30000L, timer.elapsedTimeMillis)
        assertEquals(TimerStatus.RUNNING, timer.status)
        assertEquals(now, timer.createdAt)
        assertEquals(now, timer.lastUsedAt)
        assertNull(timer.targetDate)
        assertFalse(timer.isTemplate)
        assertNull(timer.category)
        assertFalse(timer.repeat)
        assertEquals(0, timer.repeatCount)
    }
    
    @Test
    fun `date countdown timer properties should be set correctly`() {
        // Given
        val id = UUID.randomUUID().toString()
        val now = Date()
        val targetDate = Date(now.time + 86400000) // Tomorrow
        
        // When
        val timer = Timer(
            id = id,
            name = "Test Date Timer",
            type = TimerType.DATE_COUNTDOWN,
            targetDate = targetDate,
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = now,
            lastUsedAt = now
        )
        
        // Then
        assertEquals(id, timer.id)
        assertEquals("Test Date Timer", timer.name)
        assertEquals(TimerType.DATE_COUNTDOWN, timer.type)
        assertNull(timer.durationMillis)
        assertEquals(0L, timer.elapsedTimeMillis)
        assertEquals(TimerStatus.IDLE, timer.status)
        assertEquals(now, timer.createdAt)
        assertEquals(now, timer.lastUsedAt)
        assertEquals(targetDate, timer.targetDate)
        assertFalse(timer.isTemplate)
    }
    
    @Test
    fun `template properties should be set correctly`() {
        // Given
        val id = UUID.randomUUID().toString()
        val now = Date()
        
        // When
        val timer = Timer(
            id = id,
            name = "Test Template",
            type = TimerType.COUNTDOWN,
            durationMillis = 300000, // 5 minutes
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = now,
            lastUsedAt = null,
            isTemplate = true,
            category = "Workouts"
        )
        
        // Then
        assertEquals(id, timer.id)
        assertEquals("Test Template", timer.name)
        assertEquals(TimerType.COUNTDOWN, timer.type)
        assertEquals(300000L, timer.durationMillis)
        assertEquals(0L, timer.elapsedTimeMillis)
        assertEquals(TimerStatus.IDLE, timer.status)
        assertEquals(now, timer.createdAt)
        assertNull(timer.lastUsedAt)
        assertTrue(timer.isTemplate)
        assertEquals("Workouts", timer.category)
    }
    
    @Test
    fun `repeating timer properties should be set correctly`() {
        // Given
        val id = UUID.randomUUID().toString()
        val now = Date()
        
        // When
        val timer = Timer(
            id = id,
            name = "Repeating Timer",
            type = TimerType.COUNTDOWN,
            durationMillis = 30000, // 30 seconds
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = now,
            lastUsedAt = now,
            repeat = true,
            repeatCount = 5
        )
        
        // Then
        assertEquals(id, timer.id)
        assertEquals("Repeating Timer", timer.name)
        assertEquals(TimerType.COUNTDOWN, timer.type)
        assertEquals(30000L, timer.durationMillis)
        assertEquals(0L, timer.elapsedTimeMillis)
        assertEquals(TimerStatus.IDLE, timer.status)
        assertEquals(now, timer.createdAt)
        assertEquals(now, timer.lastUsedAt)
        assertTrue(timer.repeat)
        assertEquals(5, timer.repeatCount)
    }
}
