package com.lasttimer.app.ui.timer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.hilt.navigation.compose.hiltViewModel
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.FakeTimerRepository
import com.lasttimer.app.service.TimerService
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date
import java.util.UUID

/**
 * UI tests for TimerScreen
 * Note: This test uses a mocked ViewModel and Repository
 */
@RunWith(MockitoJUnitRunner::class)
class TimerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Mock
    private lateinit var viewModel: TimerViewModel
    
    private lateinit var fakeRepository: FakeTimerRepository

    @Before
    fun setUp() {
        fakeRepository = FakeTimerRepository()
        
        // Configure ViewModel with mock data
        val uiState = MutableStateFlow<TimerViewModel.TimerUiState>(
            TimerViewModel.TimerUiState.Success(fakeRepository.getAllTimers())
        )
        
        `when`(viewModel.uiState).thenReturn(uiState)
    }

    @Test
    fun testEmptyState_showsNoTimersMessage() {
        // Given
        fakeRepository.clearData() // Ensure no timers exist
        
        // When
        composeTestRule.setContent {
            TimerList(
                timersFlow = fakeRepository.getAllTimers(),
                timerService = null,
                onStartTimer = {},
                onPauseTimer = {},
                onResumeTimer = {},
                onStopTimer = {},
                onDeleteTimer = {},
                onSaveAsTemplate = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("No timers found").assertIsDisplayed()
    }

    @Test
    fun testTimerList_showsTimers() {
        // Given
        fakeRepository.clearData()
        
        // Add a timer
        val timer = Timer(
            id = UUID.randomUUID().toString(),
            name = "Test Timer",
            type = TimerType.COUNTDOWN,
            durationMillis = 60000,
            elapsedTimeMillis = 0,
            status = TimerStatus.IDLE,
            createdAt = Date(),
            lastUsedAt = Date()
        )
        
        // When
        composeTestRule.setContent {
            TimerItem(
                timer = timer,
                timerState = null,
                onStartTimer = {},
                onPauseTimer = {},
                onResumeTimer = {},
                onStopTimer = {},
                onDeleteTimer = {},
                onSaveAsTemplate = {}
            )
        }
        
        // Then
        composeTestRule.onNodeWithText("Test Timer").assertIsDisplayed()
    }
}
