package com.lasttimer.app.ui.countdown

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CountdownViewModel @Inject constructor(
    private val timerRepository: TimerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CountdownUiState>(CountdownUiState.Loading)
    val uiState: StateFlow<CountdownUiState> = _uiState.asStateFlow()
    
    private val _isCreateCountdownDialogVisible = MutableStateFlow(false)
    val isCreateCountdownDialogVisible: StateFlow<Boolean> = _isCreateCountdownDialogVisible.asStateFlow()
    
    private val _newCountdownName = MutableStateFlow("")
    val newCountdownName: StateFlow<String> = _newCountdownName.asStateFlow()
    
    private val _newCountdownDate = MutableStateFlow<Date>(Calendar.getInstance().time)
    val newCountdownDate: StateFlow<Date> = _newCountdownDate.asStateFlow()
    
    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()
    
    private val _showTimePicker = MutableStateFlow(false)
    val showTimePicker: StateFlow<Boolean> = _showTimePicker.asStateFlow()
    
    init {
        loadCountdowns()
    }
    
    private fun loadCountdowns() {
        viewModelScope.launch {
            try {
                _uiState.value = CountdownUiState.Success(timerRepository.getTimersByType(TimerType.DATE_COUNTDOWN))
            } catch (e: Exception) {
                _uiState.value = CountdownUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun showCreateCountdownDialog() {
        // Initialize with a date 1 day in the future
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        _newCountdownDate.value = calendar.time
        
        _isCreateCountdownDialogVisible.value = true
    }
    
    fun hideCreateCountdownDialog() {
        _isCreateCountdownDialogVisible.value = false
        _newCountdownName.value = ""
        _showDatePicker.value = false
        _showTimePicker.value = false
    }
    
    fun updateNewCountdownName(name: String) {
        _newCountdownName.value = name
    }
    
    fun updateNewCountdownDate(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = _newCountdownDate.value
        
        val newCalendar = Calendar.getInstance()
        newCalendar.time = date
        
        // Preserve the time from the existing date
        newCalendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY))
        newCalendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE))
        newCalendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND))
        
        _newCountdownDate.value = newCalendar.time
    }
    
    fun updateNewCountdownTime(hourOfDay: Int, minute: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = _newCountdownDate.value
        
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        
        _newCountdownDate.value = calendar.time
    }
    
    fun setShowDatePicker(show: Boolean) {
        _showDatePicker.value = show
    }
    
    fun setShowTimePicker(show: Boolean) {
        _showTimePicker.value = show
    }
    
    fun createCountdown() {
        val name = _newCountdownName.value.takeIf { it.isNotBlank() } ?: "Countdown"
        val targetDate = _newCountdownDate.value
        
        // Ensure the target date is in the future
        if (targetDate.before(Date())) {
            return
        }
        
        val countdown = Timer(
            id = UUID.randomUUID().toString(),
            name = name,
            type = TimerType.DATE_COUNTDOWN,
            targetDate = targetDate,
            createdAt = Date(),
            status = TimerStatus.IDLE
        )
        
        viewModelScope.launch {
            timerRepository.saveTimer(countdown)
            hideCreateCountdownDialog()
        }
    }
    
    fun startCountdown(timerId: String, serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.updateTimerStatus(timerId, TimerStatus.RUNNING)
            timerRepository.updateLastUsedAt(timerId)
        }
    }
    
    fun pauseCountdown(timerId: String, serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.updateTimerStatus(timerId, TimerStatus.PAUSED)
        }
    }
    
    fun resumeCountdown(timerId: String, serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.updateTimerStatus(timerId, TimerStatus.RUNNING)
            timerRepository.updateLastUsedAt(timerId)
        }
    }
    
    fun stopCountdown(timerId: String, serviceIntent: Intent) {
        viewModelScope.launch {
            timerRepository.resetTimer(timerId)
        }
    }
    
    fun deleteCountdown(timerId: String) {
        viewModelScope.launch {
            val countdown = timerRepository.getTimerById(timerId).first()
            
            countdown?.let {
                timerRepository.deleteTimer(it)
            }
        }
    }
    
    sealed class CountdownUiState {
        data object Loading : CountdownUiState()
        data class Success(val countdowns: Flow<List<Timer>>) : CountdownUiState()
        data class Error(val message: String) : CountdownUiState()
    }
}
