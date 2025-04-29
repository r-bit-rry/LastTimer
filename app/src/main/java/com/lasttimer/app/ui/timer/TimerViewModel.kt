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
                _uiState.value = TimerUiState.Success(timerRepository.getTimersByType(TimerType.COUNTDOWN))
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
    
    fun createTimer() {
        val hours = _newTimerHours.value
        val minutes = _newTimerMinutes.value
        val seconds = _newTimerSeconds.value
        val name = _newTimerName.value.takeIf { it.isNotBlank() } ?: "Timer"
        
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
            timerRepository.saveTimer(timer)
            hideCreateTimerDialog()
        }
    }
    
    fun startTimer(timerId: String, @Suppress("UNUSED_PARAMETER") serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.updateTimerStatus(timerId, TimerStatus.RUNNING)
            timerRepository.updateLastUsedAt(timerId)
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
            timerRepository.resetTimer(timerId)
        }
    }
    
    fun deleteTimer(timerId: String) {
        viewModelScope.launch {
            timerRepository.getTimerById(timerId).collect { timer ->
                timer?.let {
                    timerRepository.deleteTimer(it)
                }
            }
        }
    }
    
    fun saveAsTemplate(timerId: String) {
        viewModelScope.launch {
            timerRepository.getTimerById(timerId).collect { timer ->
                timer?.let {
                    // Create a copy of the timer as a template
                    val template = it.copy(
                        id = UUID.randomUUID().toString(),
                        name = "${it.name} (Template)",
                        isTemplate = true,
                        status = TimerStatus.IDLE,
                        elapsedTimeMillis = 0,
                        createdAt = Date(),
                        lastUsedAt = null
                    )
                    timerRepository.saveTimer(template)
                }
            }
        }
    }
    
    sealed class TimerUiState {
        data object Loading : TimerUiState()
        data class Success(val timers: Flow<List<Timer>>) : TimerUiState()
        data class Error(val message: String) : TimerUiState()
    }
}
