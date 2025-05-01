package com.lasttimer.app.ui.timer

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.ITimerRepository
import com.lasttimer.app.service.TimerService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

// UI state for the Timer screen
sealed class TimerUiState {
    object Loading : TimerUiState()
    data class Success(val timers: List<Timer>) : TimerUiState()
    data class Error(val message: String) : TimerUiState()
}

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val timerRepository: ITimerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TimerUiState>(TimerUiState.Loading)
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()
    
    private val _newTimerHours = MutableStateFlow(0)
    val newTimerHours: StateFlow<Int> = _newTimerHours.asStateFlow()
    
    private val _newTimerMinutes = MutableStateFlow(0)
    val newTimerMinutes: StateFlow<Int> = _newTimerMinutes.asStateFlow()
    
    private val _newTimerSeconds = MutableStateFlow(0)
    val newTimerSeconds: StateFlow<Int> = _newTimerSeconds.asStateFlow()
    
    private val _newTimerName = MutableStateFlow("")
    val newTimerName: StateFlow<String> = _newTimerName.asStateFlow()
    
    private val _newTimerRepeat = MutableStateFlow(false)
    val newTimerRepeat: StateFlow<Boolean> = _newTimerRepeat.asStateFlow()
    
    private val _newTimerRepeatCount = MutableStateFlow(0)
    val newTimerRepeatCount: StateFlow<Int> = _newTimerRepeatCount.asStateFlow()
    
    private val _isCreateTimerDialogVisible = MutableStateFlow(false)
    val isCreateTimerDialogVisible: StateFlow<Boolean> = _isCreateTimerDialogVisible.asStateFlow()
    
    init {
        loadTimers()
    }
    
    fun loadTimers() {
        viewModelScope.launch {
            try {
                val timers = timerRepository.getTimersByType(TimerType.COUNTDOWN).first()
                _uiState.value = TimerUiState.Success(timers)
            } catch (e: Exception) {
                _uiState.value = TimerUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun showCreateTimerDialog() {
        _isCreateTimerDialogVisible.value = true
    }
    
    fun hideCreateTimerDialog() {
        _isCreateTimerDialogVisible.value = false
        resetNewTimerInputs()
    }
    
    fun updateNewTimerHours(hours: Int) {
        _newTimerHours.value = hours
    }
    
    fun updateNewTimerMinutes(minutes: Int) {
        _newTimerMinutes.value = minutes
    }
    
    fun updateNewTimerSeconds(seconds: Int) {
        _newTimerSeconds.value = seconds
    }
    
    fun updateNewTimerName(name: String) {
        _newTimerName.value = name
    }
    
    fun updateNewTimerRepeat(repeat: Boolean) {
        _newTimerRepeat.value = repeat
    }
    
    fun updateNewTimerRepeatCount(count: Int) {
        _newTimerRepeatCount.value = count
    }
    
    private fun resetNewTimerInputs() {
        _newTimerHours.value = 0
        _newTimerMinutes.value = 0
        _newTimerSeconds.value = 0
        _newTimerName.value = ""
        _newTimerRepeat.value = false
        _newTimerRepeatCount.value = 0
    }
    
    /**
     * Generates a descriptive timer name based on the duration
     */
    private fun generateTimerName(hours: Int, minutes: Int, seconds: Int): String {
        return when {
            hours > 0 && minutes > 0 -> "$hours h $minutes min Timer"
            hours > 0 -> "$hours Hour Timer"
            minutes > 0 && seconds > 0 -> "$minutes min $seconds sec Timer"
            minutes > 0 -> "$minutes Minute Timer" 
            seconds > 0 -> "$seconds Second Timer"
            else -> "Timer"
        }
    }
    
    fun createTimer() {
        val hours = _newTimerHours.value
        val minutes = _newTimerMinutes.value
        val seconds = _newTimerSeconds.value
        // Use the provided name if not blank, otherwise generate a name based on duration
        val name = _newTimerName.value.takeIf { it.isNotBlank() } 
            ?: generateTimerName(hours, minutes, seconds)
        
        val totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L
        
        if (totalMillis <= 0) {
            return // Don't create a timer with zero or negative duration
        }
        
        val timer = Timer(
            id = UUID.randomUUID().toString(),
            name = name,
            type = TimerType.COUNTDOWN,
            durationMillis = totalMillis,
            createdAt = Date(),
            status = TimerStatus.IDLE,
            repeat = _newTimerRepeat.value,
            repeatCount = _newTimerRepeatCount.value
        )
        
        viewModelScope.launch {
            try {
                // Save the timer
                timerRepository.saveTimer(timer)
                // Hide the dialog 
                hideCreateTimerDialog()
                // Reload timers to update UI
                loadTimers()
                // Log for debugging
                println("DEBUG: Timer created and saved successfully with ID: ${timer.id}")
            } catch (e: Exception) {
                println("ERROR: Failed to save timer - ${e.message}")
                _uiState.value = TimerUiState.Error("Failed to save timer: ${e.message}")
            }
        }
    }
    
    fun startTimer(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            try {
                // Get the current timer status first
                timerRepository.getTimerById(timerId).first()?.let { timer ->
                    println("DEBUG: Starting timer ${timer.id} with status ${timer.status}")
                    
                    // If the timer is in COMPLETED status, reset it first
                    if (timer.status == TimerStatus.COMPLETED) {
                        println("DEBUG: Resetting completed timer before starting")
                        timerRepository.resetTimer(timerId)
                        
                        // Force reload the timers to update UI
                        loadTimers()
                    }
                    
                    // Now update to RUNNING status
                    timerRepository.updateTimerStatus(timerId, TimerStatus.RUNNING)
                    timerRepository.updateLastUsedAt(timerId)
                }
            } catch (e: Exception) {
                println("ERROR: Failed to start timer $timerId - ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun pauseTimer(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.updateTimerStatus(timerId, TimerStatus.PAUSED)
        }
    }
    
    fun resumeTimer(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.updateTimerStatus(timerId, TimerStatus.RUNNING)
            timerRepository.updateLastUsedAt(timerId)
        }
    }
    
    fun stopTimer(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            try {
                println("DEBUG: Stopping timer $timerId")
                timerRepository.resetTimer(timerId)
                
                // Force reload the timers list to update UI state
                loadTimers()
            } catch (e: Exception) {
                println("ERROR: Failed to stop timer $timerId - ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun deleteTimer(timerId: String) {
        viewModelScope.launch {
            try {
                println("DEBUG: Deleting timer $timerId")
                
                // Use first() instead of collect to avoid infinite loop
                val timer = timerRepository.getTimerById(timerId).first()
                timer?.let {
                    timerRepository.deleteTimer(it)
                    println("DEBUG: Timer $timerId deleted successfully")
                    
                    // Force refresh the UI
                    loadTimers()
                } ?: println("ERROR: Could not find timer $timerId to delete")
            } catch (e: Exception) {
                println("ERROR: Failed to delete timer $timerId - ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun saveAsTemplate(timerId: String) {
        viewModelScope.launch {
            // Use first() instead of collect() to prevent multiple emissions
            // causing infinite template creation.
            timerRepository.getTimerById(timerId).first()?.let { timer ->
                // Create a copy of the timer as a template
                val template = timer.copy(
                    id = UUID.randomUUID().toString(),
                    name = "${timer.name} (Template)", // Ensure template name is distinct
                    isTemplate = true,
                    status = TimerStatus.IDLE, // Reset status
                    elapsedTimeMillis = 0, // Reset elapsed time
                    createdAt = Date(), // Set new creation date for template
                    lastUsedAt = null // Templates haven't been 'used'
                )
                timerRepository.saveTimer(template)
                // Optionally, add user feedback here (e.g., show a Snackbar)
            }
        }
    }
}
