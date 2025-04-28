package com.lasttimer.app.ui.group

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.lasttimer.app.R
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerGroup
import com.lasttimer.app.service.TimerService
import kotlinx.coroutines.flow.Flow

@Composable
fun GroupScreen(
    viewModel: GroupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isCreateGroupDialogVisible by viewModel.isCreateGroupDialogVisible.collectAsState()
    val selectedGroup by viewModel.selectedGroup.collectAsState()
    val selectedGroupTimers by viewModel.selectedGroupTimers.collectAsState()
    val isAddTimerToGroupDialogVisible by viewModel.isAddTimerToGroupDialogVisible.collectAsState()
    
    // Service connection
    var timerService by remember { mutableStateOf<TimerService?>(null) }
    var isBound by remember { mutableStateOf(false) }
    
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as TimerService.TimerBinder
                timerService = binder.getService()
                isBound = true
            }
            
            override fun onServiceDisconnected(name: ComponentName?) {
                timerService = null
                isBound = false
            }
        }
    }
    
    // Bind to the service when the screen is created
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                val intent = Intent(context, TimerService::class.java)
                context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            } else if (event == Lifecycle.Event.ON_STOP) {
                if (isBound) {
                    context.unbindService(serviceConnection)
                    isBound = false
                }
            }
        }
        
        // Ensure service is running
        if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            val intent = Intent(context, TimerService::class.java)
            context.startService(intent)
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = selectedGroup?.name ?: stringResource(R.string.cascading_timers)
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = if (selectedGroup != null) {
                    {
                        IconButton(onClick = { viewModel.selectGroup(TimerGroup("", "")) }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                } else null
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedGroup != null) {
                        viewModel.showAddTimerToGroupDialog()
                    } else {
                        viewModel.showCreateGroupDialog()
                    }
                }
            ) {
                Icon(
                    Icons.Default.Add, 
                    contentDescription = if (selectedGroup != null) 
                        stringResource(R.string.add_timer) 
                    else 
                        stringResource(R.string.create_group)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selectedGroup == null) {
                // Show list of timer groups
                when (uiState) {
                    is GroupViewModel.GroupUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    is GroupViewModel.GroupUiState.Error -> {
                        Text(
                            text = (uiState as GroupViewModel.GroupUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    
                    is GroupViewModel.GroupUiState.Success -> {
                        val groupsFlow = (uiState as GroupViewModel.GroupUiState.Success).groups
                        GroupList(
                            groupsFlow = groupsFlow,
                            onGroupSelected = { viewModel.selectGroup(it) },
                            onDeleteGroup = { viewModel.deleteGroup(it.id) },
                            onStartGroup = { groupId ->
                                val intent = Intent(context, TimerService::class.java).apply {
                                    action = TimerService.ACTION_START_TIMER
                                    putExtra(TimerService.EXTRA_TIMER_ID, groupId)
                                    putExtra("IS_GROUP", true)
                                }
                                context.startService(intent)
                                viewModel.startGroup(groupId, intent)
                            }
                        )
                    }
                }
            } else {
                // Show timers in the selected group
                GroupDetailScreen(
                    timers = selectedGroupTimers,
                    onAddTimer = { viewModel.showAddTimerToGroupDialog() },
                    onRemoveTimer = { viewModel.removeTimerFromGroup(it) },
                    onMoveUp = { viewModel.moveTimerUp(it) },
                    onMoveDown = { viewModel.moveTimerDown(it) }
                )
            }
        }
    }
    
    if (isCreateGroupDialogVisible) {
        CreateGroupDialog(
            nameState = viewModel.newGroupName.collectAsState(),
            descriptionState = viewModel.newGroupDescription.collectAsState(),
            autoStartNextState = viewModel.newGroupAutoStartNext.collectAsState(),
            repeatGroupState = viewModel.newGroupRepeat.collectAsState(),
            repeatCountState = viewModel.newGroupRepeatCount.collectAsState(),
            onNameChange = { viewModel.updateNewGroupName(it) },
            onDescriptionChange = { viewModel.updateNewGroupDescription(it) },
            onAutoStartNextChange = { viewModel.updateNewGroupAutoStartNext(it) },
            onRepeatGroupChange = { viewModel.updateNewGroupRepeat(it) },
            onRepeatCountChange = { viewModel.updateNewGroupRepeatCount(it) },
            onDismiss = { viewModel.hideCreateGroupDialog() },
            onCreate = { viewModel.createGroup() }
        )
    }
    
    if (isAddTimerToGroupDialogVisible) {
        AddTimerToGroupDialog(
            availableTimers = viewModel.availableTimers.collectAsState().value,
            onTimerSelected = { viewModel.addTimerToGroup(it) },
            onDismiss = { viewModel.hideAddTimerToGroupDialog() }
        )
    }
}

@Composable
fun GroupList(
    groupsFlow: Flow<List<TimerGroup>>,
    onGroupSelected: (TimerGroup) -> Unit,
    onDeleteGroup: (TimerGroup) -> Unit,
    onStartGroup: (String) -> Unit
) {
    val groups by remember(groupsFlow) {
        mutableStateOf<List<TimerGroup>>(emptyList())
    }
    
    // Collect groups from the flow
    LaunchedEffect(groupsFlow) {
        groupsFlow.collect { newGroups ->
            groups = newGroups
        }
    }
    
    if (groups.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_groups_found),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(groups, key = { it.id }) { group ->
                GroupItem(
                    group = group,
                    onGroupSelected = { onGroupSelected(group) },
                    onDeleteGroup = { onDeleteGroup(group) },
                    onStartGroup = { onStartGroup(group.id) }
                )
            }
        }
    }
}

@Composable
fun GroupItem(
    group: TimerGroup,
    onGroupSelected: () -> Unit,
    onDeleteGroup: () -> Unit,
    onStartGroup: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onGroupSelected),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    IconButton(
                        onClick = onStartGroup
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.start)
                        )
                    }
                    
                    IconButton(
                        onClick = onDeleteGroup
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
            
            group.description?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row {
                Text(
                    text = "Auto-start next: ${if (group.autoStartNext) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Text(
                    text = "Repeat: ${if (group.repeatGroup) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodySmall
                )
                
                if (group.repeatGroup && group.repeatCount != null) {
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Text(
                        text = "Count: ${if (group.repeatCount <= 0) "∞" else group.repeatCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun GroupDetailScreen(
    timers: List<Timer>,
    onAddTimer: () -> Unit,
    onRemoveTimer: (String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit
) {
    if (timers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_timers_in_group),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(timers, key = { _, timer -> timer.id }) { index, timer ->
                GroupTimerItem(
                    timer = timer,
                    position = index,
                    isFirst = index == 0,
                    isLast = index == timers.size - 1,
                    onRemove = { onRemoveTimer(timer.id) },
                    onMoveUp = { onMoveUp(index) },
                    onMoveDown = { onMoveDown(index) }
                )
            }
        }
    }
}

@Composable
fun GroupTimerItem(
    timer: Timer,
    position: Int,
    isFirst: Boolean,
    isLast: Boolean,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${position + 1}. ${timer.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    if (!isFirst) {
                        IconButton(
                            onClick = onMoveUp
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Move Up"
                            )
                        }
                    }
                    
                    if (!isLast) {
                        IconButton(
                            onClick = onMoveDown
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Move Down"
                            )
                        }
                    }
                    
                    IconButton(
                        onClick = onRemove
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.remove_timer)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = when (timer.type) {
                    com.lasttimer.app.data.model.TimerType.COUNTDOWN -> {
                        val hours = timer.durationMillis?.div(3600000) ?: 0
                        val minutes = (timer.durationMillis?.rem(3600000) ?: 0) / 60000
                        val seconds = (timer.durationMillis?.rem(60000) ?: 0) / 1000
                        "Countdown: ${if (hours > 0) "${hours}h " else ""}${minutes}m ${seconds}s"
                    }
                    com.lasttimer.app.data.model.TimerType.STOPWATCH -> "Stopwatch"
                    com.lasttimer.app.data.model.TimerType.DATE_COUNTDOWN -> "Date Countdown: ${timer.targetDate?.toLocaleString() ?: ""}"
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CreateGroupDialog(
    nameState: androidx.compose.runtime.State<String>,
    descriptionState: androidx.compose.runtime.State<String>,
    autoStartNextState: androidx.compose.runtime.State<Boolean>,
    repeatGroupState: androidx.compose.runtime.State<Boolean>,
    repeatCountState: androidx.compose.runtime.State<Int>,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAutoStartNextChange: (Boolean) -> Unit,
    onRepeatGroupChange: (Boolean) -> Unit,
    onRepeatCountChange: (Int) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.create_group)) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.group_name)) },
                    placeholder = { Text(stringResource(R.string.enter_group_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = descriptionState.value,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Enter description") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoStartNextState.value,
                        onCheckedChange = onAutoStartNextChange
                    )
                    Text(
                        text = stringResource(R.string.auto_start_next),
                        modifier = Modifier.clickable { onAutoStartNextChange(!autoStartNextState.value) }
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = repeatGroupState.value,
                        onCheckedChange = onRepeatGroupChange
                    )
                    Text(
                        text = stringResource(R.string.repeat_group),
                        modifier = Modifier.clickable { onRepeatGroupChange(!repeatGroupState.value) }
                    )
                }
                
                if (repeatGroupState.value) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = repeatCountState.value.toString(),
                        onValueChange = { 
                            val count = it.toIntOrNull() ?: 0
                            onRepeatCountChange(count.coerceAtLeast(0)) 
                        },
                        label = { Text(stringResource(R.string.repeat_count)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate
            ) {
                Text(stringResource(R.string.create_group))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun AddTimerToGroupDialog(
    availableTimers: List<Timer>,
    onTimerSelected: (Timer) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_to_group)) },
        text = {
            if (availableTimers.isEmpty()) {
                Text("No available timers to add")
            } else {
                LazyColumn {
                    items(availableTimers) { timer ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTimerSelected(timer) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = timer.name,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.size(8.dp))
                            
                            Text(
                                text = when (timer.type) {
                                    com.lasttimer.app.data.model.TimerType.COUNTDOWN -> "Countdown"
                                    com.lasttimer.app.data.model.TimerType.STOPWATCH -> "Stopwatch"
                                    com.lasttimer.app.data.model.TimerType.DATE_COUNTDOWN -> "Date Countdown"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
