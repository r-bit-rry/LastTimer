package com.lasttimer.app.ui.countdown

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay // Import Replay icon for Reset
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.service.TimerService
import com.lasttimer.app.ui.common.CustomTimePicker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(
    viewModel: CountdownViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isCreateCountdownDialogVisible by viewModel.isCreateCountdownDialogVisible.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val showTimePicker by viewModel.showTimePicker.collectAsState()
    
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
    DisposableEffect(context) {
        val intent = Intent(context, TimerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
                isBound = false
            }
        }
    }
    
    // Restart the service if we're resuming the app
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Check if we need to restart the service
                val intent = Intent(context, TimerService::class.java)
                context.startService(intent)
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateCountdownDialog() }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_timer))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is CountdownViewModel.CountdownUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is CountdownViewModel.CountdownUiState.Error -> {
                    Text(
                        text = (uiState as CountdownViewModel.CountdownUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                
                is CountdownViewModel.CountdownUiState.Success -> {
                    val countdownsFlow = (uiState as CountdownViewModel.CountdownUiState.Success).countdowns
                    CountdownList(
                        countdownsFlow = countdownsFlow,
                        timerService = timerService,
                        onStartCountdown = { countdownId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_START_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, countdownId)
                            }
                            context.startService(intent)
                            viewModel.startCountdown(countdownId, intent)
                        },
                        onPauseCountdown = { countdownId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_PAUSE_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, countdownId)
                            }
                            context.startService(intent)
                            viewModel.pauseCountdown(countdownId, intent)
                        },
                        onResumeCountdown = { countdownId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_RESUME_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, countdownId)
                            }
                            context.startService(intent)
                            viewModel.resumeCountdown(countdownId, intent)
                        },
                        onStopCountdown = { countdownId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_STOP_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, countdownId)
                            }
                            context.startService(intent)
                            viewModel.stopCountdown(countdownId, intent)
                        },
                        onDeleteCountdown = { countdownId ->
                            viewModel.deleteCountdown(countdownId)
                        }
                    )
                }
            }
        }
    }
    
    if (isCreateCountdownDialogVisible) {
        CreateCountdownDialog(
            nameState = viewModel.newCountdownName.collectAsState(),
            dateState = viewModel.newCountdownDate.collectAsState(),
            onNameChange = { viewModel.updateNewCountdownName(it) },
            onDateClick = { viewModel.setShowDatePicker(true) },
            onTimeClick = { viewModel.setShowTimePicker(true) },
            onDismiss = { viewModel.hideCreateCountdownDialog() },
            onCreate = { viewModel.createCountdown() }
        )
    }
    
    if (showDatePicker) {
        DatePickerDialogWrapper(
            initialDate = viewModel.newCountdownDate.collectAsState().value,
            onDateSelected = { 
                viewModel.updateNewCountdownDate(it)
                viewModel.setShowDatePicker(false)
            },
            onDismiss = { viewModel.setShowDatePicker(false) }
        )
    }
    
    if (showTimePicker) {
        // Use our custom time picker dialog
        TimePickerDialogWithCustomPicker(
            initialDate = viewModel.newCountdownDate.collectAsState().value,
            onTimeSelected = { hour, minute, _ -> 
                viewModel.updateNewCountdownTime(hour, minute)
                viewModel.setShowTimePicker(false)
            },
            onDismiss = { viewModel.setShowTimePicker(false) }
        )
    }
}

@Composable
fun CountdownList(
    countdownsFlow: Flow<List<Timer>>,
    timerService: TimerService?,
    onStartCountdown: (String) -> Unit,
    onPauseCountdown: (String) -> Unit,
    onResumeCountdown: (String) -> Unit,
    onStopCountdown: (String) -> Unit,
    onDeleteCountdown: (String) -> Unit
) {
    val countdownsState = remember(countdownsFlow) {
        mutableStateOf<List<Timer>>(emptyList())
    }
    var countdowns by countdownsState
    
    // Collect countdowns from the flow
    LaunchedEffect(countdownsFlow) {
        countdownsFlow.collect { newCountdowns ->
            countdowns = newCountdowns
        }
    }
    
    // Collect timer states from the service
    val timerStates by timerService?.timerStates?.collectAsState(emptyMap()) ?: remember {
        mutableStateOf(emptyMap<String, TimerService.TimerState>())
    }
    
    if (countdowns.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No date countdowns found.\nCreate one to get started!",
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
            items(countdowns, key = { it.id }) { countdown ->
                val timerState = timerStates[countdown.id]
                
                CountdownItem(
                    countdown = countdown,
                    timerState = timerState,
                    onStartCountdown = { onStartCountdown(countdown.id) },
                    onPauseCountdown = { onPauseCountdown(countdown.id) },
                    onResumeCountdown = { onResumeCountdown(countdown.id) },
                    onStopCountdown = { onStopCountdown(countdown.id) },
                    onDeleteCountdown = { onDeleteCountdown(countdown.id) }
                )
            }
        }
    }
}

@Composable
fun CountdownItem(
    countdown: Timer,
    timerState: TimerService.TimerState?,
    onStartCountdown: () -> Unit,
    onPauseCountdown: () -> Unit,
    onResumeCountdown: () -> Unit,
    onStopCountdown: () -> Unit, // This will now act as Reset for completed timers
    onDeleteCountdown: () -> Unit
) {
    val targetDate = countdown.targetDate ?: Date()
    val now = Date()
    val isTargetInFuture = targetDate.after(now)
    val isExpired = !isTargetInFuture && countdown.status != TimerStatus.RUNNING
    val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    
    // State for edit dialog
    var showEditDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Remove the empty clickable to fix gesture detection issues
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { 
                        println("DEBUG: Long press detected on countdown")
                        showEditDialog = true 
                    }
                )
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = if (isExpired) 
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        else
            CardDefaults.cardColors()
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
                    text = countdown.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = onDeleteCountdown
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Until: ${dateFormat.format(targetDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timer display
            Text(
                text = if (isExpired) "EXPIRED" else 
                      (timerState?.formattedTime ?: formatRemainingTime(targetDate)),
                style = MaterialTheme.typography.displayMedium,
                color = if (isExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly, // Improved arrangement
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isExpired) { // Only show controls if not expired
                    when (countdown.status) {
                        TimerStatus.IDLE -> {
                            // Use standard Button for better visibility
                            Button(onClick = onStartCountdown) {
                                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.start))
                                Spacer(Modifier.size(4.dp)) // Add space between icon and text
                                Text(stringResource(R.string.start))
                            }
                        }
                        TimerStatus.RUNNING -> {
                            Button(onClick = onPauseCountdown) {
                                Icon(Icons.Default.Pause, contentDescription = stringResource(R.string.pause))
                                Spacer(Modifier.size(4.dp))
                                Text(stringResource(R.string.pause))
                            }
                            // Use TextButton for secondary actions like Stop
                            TextButton(onClick = onStopCountdown) {
                                Icon(Icons.Default.Stop, contentDescription = stringResource(R.string.stop))
                                Spacer(Modifier.size(4.dp))
                                Text(stringResource(R.string.stop))
                            }
                        }
                        TimerStatus.PAUSED -> {
                            Button(onClick = onResumeCountdown) {
                                Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.resume))
                                Spacer(Modifier.size(4.dp))
                                Text(stringResource(R.string.resume))
                            }
                            TextButton(onClick = onStopCountdown) {
                                Icon(Icons.Default.Stop, contentDescription = stringResource(R.string.stop))
                                Spacer(Modifier.size(4.dp))
                                Text(stringResource(R.string.stop))
                            }
                        }
                        TimerStatus.COMPLETED -> {
                            // Show Reset button when completed
                            Button(onClick = onStopCountdown) { // Reuse onStop for reset logic
                                Icon(Icons.Default.Replay, contentDescription = stringResource(R.string.reset))
                                Spacer(Modifier.size(4.dp))
                                Text(stringResource(R.string.reset))
                            }
                        }
                    }
                } else {
                    // Optionally show a message or different control for expired timers
                    Text(
                        text = stringResource(R.string.countdown_expired_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    if (showEditDialog) {
        // Implementation note: This dialog needs to be enhanced with full
        // edit capabilities connected to the ViewModel in a future update
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Date Countdown") },
            text = {
                Column {
                    // Name field
                    OutlinedTextField(
                        value = countdown.name,
                        onValueChange = { /* Will be implemented later */ },
                        label = { Text(stringResource(R.string.timer_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Date and time fields 
                    Button(
                        onClick = { /* Will be implemented later */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(dateFormat.format(targetDate))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun CreateCountdownDialog(
    nameState: androidx.compose.runtime.State<String>,
    dateState: androidx.compose.runtime.State<Date>,
    onNameChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.countdown_to)) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.timer_name)) },
                    placeholder = { Text("Enter countdown name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date picker field
                OutlinedTextField(
                    value = dateFormat.format(dateState.value),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.select_date)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = onDateClick) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time picker field
                OutlinedTextField(
                    value = timeFormat.format(dateState.value),
                    onValueChange = { },
                    label = { Text(stringResource(R.string.select_time)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = onTimeClick) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Select time"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = dateState.value.after(Date())
            ) {
                Text(stringResource(R.string.create_countdown))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogWrapper(
    initialDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = initialDate
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.time,
        initialDisplayMode = DisplayMode.Picker
    )
    
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Date(millis)
                        onDateSelected(date)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogWrapper(
    initialDate: Date,
    onTimeSelected: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = initialDate
    
    // Use our custom time picker instead of the standard one
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var selectedSecond by remember { mutableStateOf(0) } // Default seconds to 0
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Use our custom time picker
                CustomTimePicker(
                    initialHours = selectedHour,
                    initialMinutes = selectedMinute,
                    initialSeconds = selectedSecond,
                    onTimeChange = { hours, minutes, seconds ->
                        selectedHour = hours
                        selectedMinute = minutes
                        selectedSecond = seconds
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(selectedHour, selectedMinute)
                    // Note: We're ignoring seconds since the current implementation only uses hour and minute
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Also update the EditDialog to use CustomTimePicker
@Composable
fun EditCountdownDialog(
    countdown: Timer,
    onSave: (String, Date) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(countdown.name) }
    var selectedDate by remember { mutableStateOf(countdown.targetDate ?: Date()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    // We'll use calendar instances to manipulate dates

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Date Countdown") },
        text = {
            Column {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.timer_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date picker button
                OutlinedTextField(
                    value = dateFormat.format(selectedDate),
                    onValueChange = { /* Read-only */ },
                    label = { Text(stringResource(R.string.select_date)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time picker - display current time
                OutlinedTextField(
                    value = timeFormat.format(selectedDate),
                    onValueChange = { /* Read-only */ },
                    label = { Text(stringResource(R.string.select_time)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTimePickerDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Select time"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(name, selectedDate)
                    onDismiss()
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
    
    // Date picker dialog
    if (showDatePickerDialog) {
        DatePickerDialogWrapper(
            initialDate = selectedDate,
            onDateSelected = { 
                // Preserve time when updating date
                val oldCalendar = Calendar.getInstance().apply { time = selectedDate }
                val newCalendar = Calendar.getInstance().apply { time = it }
                
                newCalendar.set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                newCalendar.set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                newCalendar.set(Calendar.SECOND, oldCalendar.get(Calendar.SECOND))
                
                selectedDate = newCalendar.time
                showDatePickerDialog = false
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }
    
    // Time picker dialog
    if (showTimePickerDialog) {
        // Use our CustomTimePicker
        TimePickerDialogWithCustomPicker(
            initialDate = selectedDate,
            onTimeSelected = { hour, minute, second ->
                // Update time while preserving date
                val newCalendar = Calendar.getInstance()
                newCalendar.time = selectedDate
                
                newCalendar.set(Calendar.HOUR_OF_DAY, hour)
                newCalendar.set(Calendar.MINUTE, minute)
                newCalendar.set(Calendar.SECOND, second)
                
                selectedDate = newCalendar.time
                showTimePickerDialog = false
            },
            onDismiss = { showTimePickerDialog = false }
        )
    }
}

// A dialog wrapper that uses our CustomTimePicker
@Composable
fun TimePickerDialogWithCustomPicker(
    initialDate: Date,
    onTimeSelected: (Int, Int, Int) -> Unit, // Hour, minute, second
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance()
    calendar.time = initialDate
    
    // Initialize with current time values
    var selectedHour by remember { mutableStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(calendar.get(Calendar.MINUTE)) }
    var selectedSecond by remember { mutableStateOf(calendar.get(Calendar.SECOND)) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Use our CustomTimePicker
                CustomTimePicker(
                    initialHours = selectedHour,
                    initialMinutes = selectedMinute,
                    initialSeconds = selectedSecond,
                    onTimeChange = { hours, minutes, seconds ->
                        selectedHour = hours
                        selectedMinute = minutes
                        selectedSecond = seconds
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(selectedHour, selectedMinute, selectedSecond)
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Helper function to format remaining time
fun formatRemainingTime(targetDate: Date): String {
    val now = Date()
    
    if (targetDate.before(now)) {
        return "00:00:00"
    }
    
    val diff = targetDate.time - now.time
    val seconds = (diff / 1000) % 60
    val minutes = (diff / (1000 * 60)) % 60
    val hours = (diff / (1000 * 60 * 60)) % 24
    val days = diff / (1000 * 60 * 60 * 24)
    
    return if (days > 0) {
        "$days days, %02d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d:%02d".format(hours, minutes, seconds)
    }
}
