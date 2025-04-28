package com.lasttimer.app.ui.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.FakeTimerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.UUID

@ExperimentalCoroutinesApi
class TimerViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeTimerRepository
    private lateinit var viewModel: TimerViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeTimerRepository()
        viewModel = TimerViewModel(repository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadTimers should load timers from repository`() = runTest {
        // Given
        repository.addSampleData() // Add sample timers
        
        // When
        viewModel.loadTimers()
        advanceUntilIdle() // Wait for coroutines to complete
        
        // Then
        val uiState = viewModel.uiState.value
        assertTrue(uiState is TimerViewModel.TimerUiState.Success)
        
        // Verify that timers were loaded
        val timers = (uiState as TimerViewModel.TimerUiState.Success).timers.first()
        assertEquals(1, timers.size) // Only one countdown timer should be loaded
    }
    
    @Test
    fun `createTimer should add a new timer to repository`() = runTest {
        // Given
        repository.clearData()
        viewModel.showCreateTimerDialog()
        viewModel.updateNewTimerName("Test Timer")
        viewModel.updateNewTimerHours(1)
        viewModel.updateNewTimerMinutes(30)
        viewModel.updateNewTimerSeconds(0)
        
        // When
        viewModel.createTimer()
        advanceUntilIdle()
        
        // Then
        val timers = repository.getAllTimers().first()
        assertEquals(1, timers.size)
        
        val timer = timers.first()
        assertEquals("Test Timer", timer.name)
        assertEquals(TimerType.COUNTDOWN, timer.type)
        assertEquals(5400000, timer.durationMillis) // 1h30m = 5400000ms
        assertEquals(TimerStatus.IDLE, timer.status)
    }
    
    @Test
    fun `deleteTimer should remove timer from repository`() = runTest {
        // Given
        repository.clearData()
        val timerId = UUID.randomUUID().toString()
        val timer = Timer(
            id = timerId,
            name = "Timer to delete",
            type = TimerType.COUNTDOWN,
            durationMillis = 60000,
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = Date(),
            lastUsedAt = Date()
        )
        repository.saveTimer(timer)
        
        // Verify timer exists
        val beforeDelete = repository.getAllTimers().first()
        assertEquals(1, beforeDelete.size)
        
        // When
        viewModel.deleteTimer(timerId)
        advanceUntilIdle()
        
        // Then
        val afterDelete = repository.getAllTimers().first()
        assertEquals(0, afterDelete.size)
    }
    
    @Test
    fun `saveAsTemplate should create a template from timer`() = runTest {
        // Given
        repository.clearData()
        val timerId = UUID.randomUUID().toString()
        val timer = Timer(
            id = timerId,
            name = "Timer to template",
            type = TimerType.COUNTDOWN,
            durationMillis = 60000,
            elapsedTimeMillis = 30000, // Half completed
            status = TimerStatus.RUNNING,
            createdAt = Date(),
            lastUsedAt = Date()
        )
        repository.saveTimer(timer)
        
        // When
        viewModel.saveAsTemplate(timerId)
        advanceUntilIdle()
        
        // Then
        val templates = repository.getAllTemplates().first()
        assertEquals(1, templates.size)
        
        val template = templates.first()
        assertTrue(template.isTemplate)
        assertEquals("Timer to template (Template)", template.name)
        assertEquals(TimerType.COUNTDOWN, template.type)
        assertEquals(60000, template.durationMillis)
        assertEquals(0, template.elapsedTimeMillis) // Templates should start at 0
        assertEquals(TimerStatus.IDLE, template.status) // Templates should be idle
    }
}
