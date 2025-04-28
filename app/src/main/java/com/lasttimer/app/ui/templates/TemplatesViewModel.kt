package com.lasttimer.app.ui.templates

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
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TemplatesViewModel @Inject constructor(
    private val timerRepository: TimerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<TemplatesUiState>(TemplatesUiState.Loading)
    val uiState: StateFlow<TemplatesUiState> = _uiState.asStateFlow()
    
    // Selected timer for creating a template
    private val _selectedTimer = MutableStateFlow<Timer?>(null)
    val selectedTimer: StateFlow<Timer?> = _selectedTimer.asStateFlow()
    
    // Create template dialog
    private val _isCreateTemplateDialogVisible = MutableStateFlow(false)
    val isCreateTemplateDialogVisible: StateFlow<Boolean> = _isCreateTemplateDialogVisible.asStateFlow()
    
    private val _newTemplateName = MutableStateFlow("")
    val newTemplateName: StateFlow<String> = _newTemplateName.asStateFlow()
    
    private val _newTemplateCategory = MutableStateFlow("")
    val newTemplateCategory: StateFlow<String> = _newTemplateCategory.asStateFlow()
    
    // Timer list for template creation
    private val _availableTimers = MutableStateFlow<List<Timer>>(emptyList())
    val availableTimers: StateFlow<List<Timer>> = _availableTimers.asStateFlow()
    
    // Create timer from template dialog
    private val _isCreateFromTemplateDialogVisible = MutableStateFlow(false)
    val isCreateFromTemplateDialogVisible: StateFlow<Boolean> = _isCreateFromTemplateDialogVisible.asStateFlow()
    
    private val _selectedTemplate = MutableStateFlow<Timer?>(null)
    val selectedTemplate: StateFlow<Timer?> = _selectedTemplate.asStateFlow()
    
    init {
        loadTemplates()
        loadAvailableTimers()
    }
    
    private fun loadTemplates() {
        viewModelScope.launch {
            try {
                _uiState.value = TemplatesUiState.Success(timerRepository.getAllTemplates())
            } catch (e: Exception) {
                _uiState.value = TemplatesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    private fun loadAvailableTimers() {
        viewModelScope.launch {
            try {
                val allTimers = timerRepository.getAllTimers().first()
                _availableTimers.value = allTimers
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    fun showCreateTemplateDialog(timer: Timer? = null) {
        if (timer != null) {
            _selectedTimer.value = timer
            _newTemplateName.value = timer.name + " (Template)"
            _newTemplateCategory.value = timer.category ?: ""
        } else {
            _selectedTimer.value = null
            _newTemplateName.value = ""
            _newTemplateCategory.value = ""
        }
        _isCreateTemplateDialogVisible.value = true
    }
    
    fun hideCreateTemplateDialog() {
        _isCreateTemplateDialogVisible.value = false
        _selectedTimer.value = null
        _newTemplateName.value = ""
        _newTemplateCategory.value = ""
    }
    
    fun updateNewTemplateName(name: String) {
        _newTemplateName.value = name
    }
    
    fun updateNewTemplateCategory(category: String) {
        _newTemplateCategory.value = category
    }
    
    fun createTemplate() {
        val timer = _selectedTimer.value ?: return
        
        val template = timer.copy(
            id = UUID.randomUUID().toString(),
            name = _newTemplateName.value.takeIf { it.isNotBlank() } ?: "Template",
            category = _newTemplateCategory.value.takeIf { it.isNotBlank() },
            createdAt = Date(),
            lastUsedAt = null,
            isTemplate = true,
            status = TimerStatus.IDLE,
            elapsedTimeMillis = 0
        )
        
        viewModelScope.launch {
            timerRepository.saveTimer(template)
            hideCreateTemplateDialog()
        }
    }
    
    fun createEmptyTemplate() {
        val name = _newTemplateName.value.takeIf { it.isNotBlank() } ?: "Template"
        val category = _newTemplateCategory.value.takeIf { it.isNotBlank() }
        
        // Create a basic template with default values
        val template = Timer(
            id = UUID.randomUUID().toString(),
            name = name,
            category = category,
            type = TimerType.COUNTDOWN,
            durationMillis = 60000, // 1 minute default
            createdAt = Date(),
            isTemplate = true,
            status = TimerStatus.IDLE
        )
        
        viewModelScope.launch {
            timerRepository.saveTimer(template)
            hideCreateTemplateDialog()
        }
    }
    
    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            val template = timerRepository.getTimerById(templateId).first()
            template?.let {
                if (it.isTemplate) {
                    timerRepository.deleteTimer(it)
                }
            }
        }
    }
    
    fun selectTemplate(template: Timer) {
        _selectedTemplate.value = template
        _isCreateFromTemplateDialogVisible.value = true
    }
    
    fun hideCreateFromTemplateDialog() {
        _isCreateFromTemplateDialogVisible.value = false
        _selectedTemplate.value = null
    }
    
    fun createTimerFromTemplate() {
        val template = _selectedTemplate.value ?: return
        
        // Create a new timer based on the template
        val newTimer = template.copy(
            id = UUID.randomUUID().toString(),
            isTemplate = false,
            createdAt = Date(),
            lastUsedAt = Date(),
            status = TimerStatus.IDLE,
            elapsedTimeMillis = 0
        )
        
        viewModelScope.launch {
            timerRepository.saveTimer(newTimer)
            hideCreateFromTemplateDialog()
        }
    }
    
    sealed class TemplatesUiState {
        data object Loading : TemplatesUiState()
        data class Success(val templates: Flow<List<Timer>>) : TemplatesUiState()
        data class Error(val message: String) : TemplatesUiState()
    }
}
