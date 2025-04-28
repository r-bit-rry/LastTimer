package com.lasttimer.app.ui.countdown

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tab_countdown)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
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
        TimePickerDialogWrapper(
            initialDate = viewModel.newCountdownDate.collectAsState().value,
            onTimeSelected = { hour, minute ->
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
    val countdowns by remember(countdownsFlow) {
        mutableStateOf<List<Timer>>(emptyList())
    }
    
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
    onStopCountdown: () -> Unit,
    onDeleteCountdown: () -> Unit
) {
    val targetDate = countdown.targetDate ?: Date()
    val now = Date()
    val isTargetInFuture = targetDate.after(now)
    val isExpired = !isTargetInFuture && countdown.status != TimerStatus.RUNNING
    val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            if (!isExpired) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    when (countdown.status) {
                        TimerStatus.IDLE -> {
                            ControlButton(
                                icon = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.start),
                                onClick = onStartCountdown
                            )
                        }
                        TimerStatus.RUNNING -> {
                            ControlButton(
                                icon = Icons.Default.Pause,
                                contentDescription = stringResource(R.string.pause),
                                onClick = onPauseCountdown
                            )
                            ControlButton(
                                icon = Icons.Default.Stop,
                                contentDescription = stringResource(R.string.stop),
                                onClick = onStopCountdown
                            )
                        }
                        TimerStatus.PAUSED -> {
                            ControlButton(
                                icon = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.resume),
                                onClick = onResumeCountdown
                            )
                            ControlButton(
                                icon = Icons.Default.Stop,
                                contentDescription = stringResource(R.string.stop),
                                onClick = onStopCountdown
                            )
                        }
                        TimerStatus.COMPLETED -> {
                            ControlButton(
                                icon = Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.start),
                                onClick = onStartCountdown
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(28.dp)
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
    
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE),
        is24Hour = false
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(timePickerState.hour, timePickerState.minute)
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
