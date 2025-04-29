package com.lasttimer.app.ui.timer

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.ITimerRepository
import com.lasttimer.app.data.repository.TimerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import java.util.Date
import java.util.UUID

@ExperimentalCoroutinesApi
class TimerViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var timerRepository: ITimerRepository
    
    private lateinit var viewModel: TimerViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = TimerViewModel(timerRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadTimers should update uiState with timers from repository`() = runTest {
        // Given
        val timer1 = createTimer("Timer 1", TimerType.COUNTDOWN)
        val timer2 = createTimer("Timer 2", TimerType.COUNTDOWN)
        val timers = listOf(timer1, timer2)
        
        Mockito.`when`(timerRepository.getTimersByType(TimerType.COUNTDOWN))
            .thenReturn(flowOf(timers))
        
        // When
        viewModel.loadTimers()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        assert(viewModel.uiState.value is TimerViewModel.TimerUiState.Success)
        val successState = viewModel.uiState.value as TimerViewModel.TimerUiState.Success
        assert(successState.timers == timers)
    }
    
    private fun createTimer(name: String, type: TimerType): Timer {
        return Timer(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            durationMillis = 60000L,
            status = TimerStatus.IDLE,
            createdAt = Date()
        )
    }
}
