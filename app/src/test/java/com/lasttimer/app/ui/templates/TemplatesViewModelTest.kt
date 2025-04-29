package com.lasttimer.app.ui.templates

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.ITimerRepository
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
class TemplatesViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    
    @Mock
    private lateinit var timerRepository: ITimerRepository
    
    private lateinit var viewModel: TemplatesViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        
        viewModel = TemplatesViewModel(timerRepository)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `loadTemplates should update uiState with templates from repository`() = runTest {
        // Given
        val template1 = createTemplate("Template 1", TimerType.COUNTDOWN)
        val template2 = createTemplate("Template 2", TimerType.STOPWATCH)
        val templates = listOf(template1, template2)
        
        Mockito.`when`(timerRepository.getAllTemplates())
            .thenReturn(flowOf(templates))
        
        // When
        viewModel.refreshTemplates()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val currentState = viewModel.uiState.value
        assert(currentState is TemplatesViewModel.TemplatesUiState.Success) {
            "Expected Success state but got ${currentState::class.simpleName}"
        }
    }
    
    private fun createTemplate(name: String, type: TimerType): Timer {
        return Timer(
            id = UUID.randomUUID().toString(),
            name = name,
            type = type,
            durationMillis = 60000L,
            status = TimerStatus.IDLE,
            createdAt = Date(),
            isTemplate = true
        )
    }
}
