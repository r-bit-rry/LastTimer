package com.lasttimer.app.ui.group

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerGroup
import com.lasttimer.app.data.model.TimerGroupItem
import com.lasttimer.app.data.model.TimerWithGroup
import com.lasttimer.app.data.repository.TimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val timerRepository: TimerRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<GroupUiState>(GroupUiState.Loading)
    val uiState: StateFlow<GroupUiState> = _uiState.asStateFlow()
    
    private val _isCreateGroupDialogVisible = MutableStateFlow(false)
    val isCreateGroupDialogVisible: StateFlow<Boolean> = _isCreateGroupDialogVisible.asStateFlow()
    
    private val _newGroupName = MutableStateFlow("")
    val newGroupName: StateFlow<String> = _newGroupName.asStateFlow()
    
    private val _newGroupDescription = MutableStateFlow("")
    val newGroupDescription: StateFlow<String> = _newGroupDescription.asStateFlow()
    
    private val _newGroupAutoStartNext = MutableStateFlow(true)
    val newGroupAutoStartNext: StateFlow<Boolean> = _newGroupAutoStartNext.asStateFlow()
    
    private val _newGroupRepeat = MutableStateFlow(false)
    val newGroupRepeat: StateFlow<Boolean> = _newGroupRepeat.asStateFlow()
    
    private val _newGroupRepeatCount = MutableStateFlow(0)
    val newGroupRepeatCount: StateFlow<Int> = _newGroupRepeatCount.asStateFlow()
    
    private val _selectedGroup = MutableStateFlow<TimerGroup?>(null)
    val selectedGroup: StateFlow<TimerGroup?> = _selectedGroup.asStateFlow()
    
    private val _selectedGroupTimers = MutableStateFlow<List<Timer>>(emptyList())
    val selectedGroupTimers: StateFlow<List<Timer>> = _selectedGroupTimers.asStateFlow()
    
    private val _isAddTimerToGroupDialogVisible = MutableStateFlow(false)
    val isAddTimerToGroupDialogVisible: StateFlow<Boolean> = _isAddTimerToGroupDialogVisible.asStateFlow()
    
    private val _availableTimers = MutableStateFlow<List<Timer>>(emptyList())
    val availableTimers: StateFlow<List<Timer>> = _availableTimers.asStateFlow()
    
    init {
        loadGroups()
    }
    
    private fun loadGroups() {
        viewModelScope.launch {
            try {
                _uiState.value = GroupUiState.Success(timerRepository.getAllGroups())
            } catch (e: Exception) {
                _uiState.value = GroupUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun showCreateGroupDialog() {
        _isCreateGroupDialogVisible.value = true
    }
    
    fun hideCreateGroupDialog() {
        _isCreateGroupDialogVisible.value = false
        resetNewGroupInputs()
    }
    
    fun updateNewGroupName(name: String) {
        _newGroupName.value = name
    }
    
    fun updateNewGroupDescription(description: String) {
        _newGroupDescription.value = description
    }
    
    fun updateNewGroupAutoStartNext(autoStartNext: Boolean) {
        _newGroupAutoStartNext.value = autoStartNext
    }
    
    fun updateNewGroupRepeat(repeat: Boolean) {
        _newGroupRepeat.value = repeat
    }
    
    fun updateNewGroupRepeatCount(repeatCount: Int) {
        _newGroupRepeatCount.value = repeatCount
    }
    
    private fun resetNewGroupInputs() {
        _newGroupName.value = ""
        _newGroupDescription.value = ""
        _newGroupAutoStartNext.value = true
        _newGroupRepeat.value = false
        _newGroupRepeatCount.value = 0
    }
    
    fun createGroup() {
        val name = _newGroupName.value.takeIf { it.isNotBlank() } ?: "Timer Group"
        val description = _newGroupDescription.value.takeIf { it.isNotBlank() }
        val autoStartNext = _newGroupAutoStartNext.value
        val repeatGroup = _newGroupRepeat.value
        val repeatCount = if (repeatGroup) _newGroupRepeatCount.value else null
        
        val timerGroup = TimerGroup(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            createdAt = Date(),
            autoStartNext = autoStartNext,
            repeatGroup = repeatGroup,
            repeatCount = repeatCount
        )
        
        viewModelScope.launch {
            timerRepository.saveGroup(timerGroup)
            hideCreateGroupDialog()
        }
    }
    
    fun selectGroup(group: TimerGroup) {
        viewModelScope.launch {
            _selectedGroup.value = group
            loadGroupTimers(group.id)
        }
    }
    
    private fun loadGroupTimers(groupId: String) {
        viewModelScope.launch {
            val groupItems = timerRepository.getGroupItems(groupId).first()
            val timers = mutableListOf<Timer>()
            
            groupItems.sortedBy { it.position }.forEach { groupItem ->
                timerRepository.getTimerById(groupItem.timerId).first()?.let { timer ->
                    timers.add(timer)
                }
            }
            
            _selectedGroupTimers.value = timers
        }
    }
    
    fun showAddTimerToGroupDialog() {
        viewModelScope.launch {
            val allTimers = timerRepository.getAllTimers().first()
            val groupTimerIds = _selectedGroupTimers.value.map { it.id }
            _availableTimers.value = allTimers.filter { it.id !in groupTimerIds }
            _isAddTimerToGroupDialogVisible.value = true
        }
    }
    
    fun hideAddTimerToGroupDialog() {
        _isAddTimerToGroupDialogVisible.value = false
    }
    
    fun addTimerToGroup(timer: Timer) {
        val group = _selectedGroup.value ?: return
        val position = _selectedGroupTimers.value.size
        
        val groupItem = TimerGroupItem(
            groupId = group.id,
            timerId = timer.id,
            position = position
        )
        
        viewModelScope.launch {
            timerRepository.saveGroupItem(groupItem)
            loadGroupTimers(group.id)
            hideAddTimerToGroupDialog()
        }
    }
    
    fun removeTimerFromGroup(timerId: String) {
        val group = _selectedGroup.value ?: return
        val position = _selectedGroupTimers.value.indexOfFirst { it.id == timerId }
        
        if (position >= 0) {
            viewModelScope.launch {
                timerRepository.deleteItemAndReorder(group.id, position)
                loadGroupTimers(group.id)
            }
        }
    }
    
    fun moveTimerUp(position: Int) {
        if (position <= 0) return
        
        val group = _selectedGroup.value ?: return
        viewModelScope.launch {
            // Swap positions in database
            val items = timerRepository.getGroupItems(group.id).first()
                .sortedBy { it.position }
                .toMutableList()
            
            if (position < items.size) {
                val item = items[position]
                val itemAbove = items[position - 1]
                
                // Update positions in database
                val updatedItem = item.copy(position = item.position - 1)
                val updatedItemAbove = itemAbove.copy(position = itemAbove.position + 1)
                
                timerRepository.saveGroupItem(updatedItem)
                timerRepository.saveGroupItem(updatedItemAbove)
                
                // Reload group timers
                loadGroupTimers(group.id)
            }
        }
    }
    
    fun moveTimerDown(position: Int) {
        val group = _selectedGroup.value ?: return
        viewModelScope.launch {
            val items = timerRepository.getGroupItems(group.id).first()
                .sortedBy { it.position }
                .toMutableList()
            
            if (position < items.size - 1) {
                val item = items[position]
                val itemBelow = items[position + 1]
                
                // Update positions in database
                val updatedItem = item.copy(position = item.position + 1)
                val updatedItemBelow = itemBelow.copy(position = itemBelow.position - 1)
                
                timerRepository.saveGroupItem(updatedItem)
                timerRepository.saveGroupItem(updatedItemBelow)
                
                // Reload group timers
                loadGroupTimers(group.id)
            }
        }
    }
    
    fun deleteGroup(groupId: String) {
        viewModelScope.launch {
            val group = timerRepository.getGroupById(groupId).first()
            
            group?.let {
                // First delete all group items
                timerRepository.deleteAllGroupItems(groupId)
                
                // Then delete the group
                timerRepository.deleteGroup(it)
                
                // Clear selected group if it was the deleted one
                if (_selectedGroup.value?.id == groupId) {
                    _selectedGroup.value = null
                    _selectedGroupTimers.value = emptyList()
                }
            }
        }
    }
    
    fun startGroup(groupId: String, serviceIntent: Intent) {
        viewModelScope.launch {
            val group = timerRepository.getGroupById(groupId).first() ?: return@launch
            timerRepository.updateGroupLastUsedAt(groupId)
            
            // Get all timers in the group in order
            val groupItems = timerRepository.getGroupItems(groupId).first()
                .sortedBy { it.position }
            
            if (groupItems.isNotEmpty()) {
                // Start the first timer
                val firstTimerId = groupItems.first().timerId
                val intent = Intent(serviceIntent)
                intent.action = "com.lasttimer.app.action.START_GROUP_TIMER"
                intent.putExtra("com.lasttimer.app.extra.TIMER_ID", firstTimerId)
                intent.putExtra("com.lasttimer.app.extra.GROUP_ID", groupId)
            }
        }
    }
    
    sealed class GroupUiState {
        data object Loading : GroupUiState()
        data class Success(val groups: Flow<List<TimerGroup>>) : GroupUiState()
        data class Error(val message: String) : GroupUiState()
    }
}
