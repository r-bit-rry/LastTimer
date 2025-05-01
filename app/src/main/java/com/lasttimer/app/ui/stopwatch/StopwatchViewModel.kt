package com.lasttimer.app.ui.stopwatch

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerLap
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.TimerRepository
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

@HiltViewModel
class StopwatchViewModel @Inject constructor(
    private val timerRepository: TimerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<StopwatchUiState>(StopwatchUiState.Loading)
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()
    
    private val _activeStopwatch = MutableStateFlow<Timer?>(null)
    val activeStopwatch: StateFlow<Timer?> = _activeStopwatch.asStateFlow()
    
    private val _stopwatchLaps = MutableStateFlow<List<TimerLap>>(emptyList())
    val stopwatchLaps: StateFlow<List<TimerLap>> = _stopwatchLaps.asStateFlow()
    
    private val _isCreateStopwatchDialogVisible = MutableStateFlow(false)
    val isCreateStopwatchDialogVisible: StateFlow<Boolean> = _isCreateStopwatchDialogVisible.asStateFlow()
    
    private val _newStopwatchName = MutableStateFlow("")
    val newStopwatchName: StateFlow<String> = _newStopwatchName.asStateFlow()
    
    init {
        loadStopwatches()
    }
    
    private fun loadStopwatches() {
        viewModelScope.launch {
            try {
                _uiState.value = StopwatchUiState.Success(timerRepository.getTimersByType(TimerType.STOPWATCH))
                
                // Check if there's an active stopwatch
                val runningStopwatches = timerRepository.getTimersByType(TimerType.STOPWATCH).first()
                    .filter { it.status == TimerStatus.RUNNING || it.status == TimerStatus.PAUSED }
                
                if (runningStopwatches.isNotEmpty()) {
                    _activeStopwatch.value = runningStopwatches.first()
                    loadLaps(runningStopwatches.first().id)
                }
            } catch (e: Exception) {
                _uiState.value = StopwatchUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun loadLaps(timerId: String) {
        viewModelScope.launch {
            timerRepository.getLapsByTimerId(timerId).collect { laps ->
                _stopwatchLaps.value = laps
            }
        }
    }
    
    fun showCreateStopwatchDialog() {
        _isCreateStopwatchDialogVisible.value = true
    }
    
    fun hideCreateStopwatchDialog() {
        _isCreateStopwatchDialogVisible.value = false
        _newStopwatchName.value = ""
    }
    
    fun updateNewStopwatchName(name: String) {
        _newStopwatchName.value = name
    }
    
    fun createStopwatch() {
        val name = _newStopwatchName.value.takeIf { it.isNotBlank() } ?: "Stopwatch"
        
        val stopwatch = Timer(
            id = UUID.randomUUID().toString(),
            name = name,
            type = TimerType.STOPWATCH,
            durationMillis = null,
            createdAt = Date(),
            status = TimerStatus.IDLE
        )
        
        viewModelScope.launch {
            timerRepository.saveTimer(stopwatch)
            hideCreateStopwatchDialog()
        }
    }
    
    fun startStopwatch(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            val stopwatch = timerRepository.getTimerById(timerId).first()
            
            if (stopwatch != null) {
                _activeStopwatch.value = stopwatch
                loadLaps(stopwatch.id)
                
                timerRepository.updateTimerStatus(timerId, TimerStatus.RUNNING)
                timerRepository.updateLastUsedAt(timerId)
            }
        }
    }
    
    fun pauseStopwatch(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.updateTimerStatus(timerId, TimerStatus.PAUSED)
        }
    }
    
    fun resumeStopwatch(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.updateTimerStatus(timerId, TimerStatus.RUNNING)
            timerRepository.updateLastUsedAt(timerId)
        }
    }
    
    fun resetStopwatch(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            try {
                println("DEBUG: Resetting stopwatch $timerId")
                timerRepository.resetTimer(timerId)
                timerRepository.deleteAllLapsForTimer(timerId)
                _stopwatchLaps.value = emptyList()
                
                // If this was the active stopwatch, clear it
                if (_activeStopwatch.value?.id == timerId) {
                    _activeStopwatch.value = null
                }
                
                // Force refresh of stopwatches list to update UI
                _uiState.value = StopwatchUiState.Success(timerRepository.getTimersByType(TimerType.STOPWATCH))
            } catch (e: Exception) {
                println("ERROR: Failed to reset stopwatch $timerId - ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun addLap(timerId: String) {
        viewModelScope.launch {
            val stopwatch = timerRepository.getTimerById(timerId).first() ?: return@launch
            
            // Get the next lap number
            val lapCount = timerRepository.getLapCount(timerId)
            val lapNumber = lapCount + 1
            
            val lap = TimerLap(
                timerId = timerId,
                lapNumber = lapNumber,
                elapsedTimeMillis = stopwatch.elapsedTimeMillis
            )
            
            timerRepository.saveLap(lap)
        }
    }
    
    fun deleteStopwatch(timerId: String) {
        viewModelScope.launch {
            try {
                println("DEBUG: Deleting stopwatch $timerId")
                
                val stopwatch = timerRepository.getTimerById(timerId).first()
                
                stopwatch?.let {
                    timerRepository.deleteTimer(it)
                    println("DEBUG: Stopwatch $timerId deleted successfully")
                    
                    // If this was the active stopwatch, clear it
                    if (_activeStopwatch.value?.id == timerId) {
                        _activeStopwatch.value = null
                        _stopwatchLaps.value = emptyList()
                    }
                    
                    // Refresh the UI after deletion
                    _uiState.value = StopwatchUiState.Success(timerRepository.getTimersByType(TimerType.STOPWATCH))
                } ?: println("ERROR: Could not find stopwatch $timerId to delete")
            } catch (e: Exception) {
                println("ERROR: Failed to delete stopwatch $timerId - ${e.message}")
                e.printStackTrace()
                _uiState.value = StopwatchUiState.Error("Failed to delete stopwatch: ${e.message}")
            }
        }
    }
    
    sealed class StopwatchUiState {
        data object Loading : StopwatchUiState()
        data class Success(val stopwatches: Flow<List<Timer>>) : StopwatchUiState()
        data class Error(val message: String) : StopwatchUiState()
    }
}
