package com.lasttimer.app.ui.timer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isCreateTimerDialogVisible by viewModel.isCreateTimerDialogVisible.collectAsState()
    
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
                title = { Text(stringResource(R.string.tab_timer)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateTimerDialog() }
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
                is TimerViewModel.TimerUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                is TimerViewModel.TimerUiState.Error -> {
                    Text(
                        text = (uiState as TimerViewModel.TimerUiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                
                is TimerViewModel.TimerUiState.Success -> {
                    val timersFlow = (uiState as TimerViewModel.TimerUiState.Success).timers
                    TimerList(
                        timersFlow = timersFlow,
                        timerService = timerService,
                        onStartTimer = { timerId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_START_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                            }
                            context.startService(intent)
                            viewModel.startTimer(timerId, intent)
                        },
                        onPauseTimer = { timerId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_PAUSE_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                            }
                            context.startService(intent)
                            viewModel.pauseTimer(timerId, intent)
                        },
                        onResumeTimer = { timerId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_RESUME_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                            }
                            context.startService(intent)
                            viewModel.resumeTimer(timerId, intent)
                        },
                        onStopTimer = { timerId ->
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_STOP_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                            }
                            context.startService(intent)
                            viewModel.stopTimer(timerId, intent)
                        },
                        onDeleteTimer = { timerId ->
                            viewModel.deleteTimer(timerId)
                        },
                        onSaveAsTemplate = { timerId ->
                            viewModel.saveAsTemplate(timerId)
                        }
                    )
                }
            }
        }
    }
    
    if (isCreateTimerDialogVisible) {
        CreateTimerDialog(
            hoursState = viewModel.newTimerHours.collectAsState(),
            minutesState = viewModel.newTimerMinutes.collectAsState(),
            secondsState = viewModel.newTimerSeconds.collectAsState(),
            nameState = viewModel.newTimerName.collectAsState(),
            repeatState = viewModel.newTimerRepeat.collectAsState(),
            repeatCountState = viewModel.newTimerRepeatCount.collectAsState(),
            onHoursChange = { viewModel.updateNewTimerHours(it) },
            onMinutesChange = { viewModel.updateNewTimerMinutes(it) },
            onSecondsChange = { viewModel.updateNewTimerSeconds(it) },
            onNameChange = { viewModel.updateNewTimerName(it) },
            onRepeatChange = { viewModel.updateNewTimerRepeat(it) },
            onRepeatCountChange = { viewModel.updateNewTimerRepeatCount(it) },
            onDismiss = { viewModel.hideCreateTimerDialog() },
            onCreate = { viewModel.createTimer() }
        )
    }
}

@Composable
fun TimerList(
    timersFlow: Flow<List<Timer>>,
    timerService: TimerService?,
    onStartTimer: (String) -> Unit,
    onPauseTimer: (String) -> Unit,
    onResumeTimer: (String) -> Unit,
    onStopTimer: (String) -> Unit,
    onDeleteTimer: (String) -> Unit,
    onSaveAsTemplate: (String) -> Unit
) {
    val timersState = remember(timersFlow) {
        mutableStateOf<List<Timer>>(emptyList())
    }
    var timers by timersState
    
    // Collect timers from the flow
    LaunchedEffect(timersFlow) {
        timersFlow.collect { newTimers ->
            timers = newTimers
        }
    }
    
    // Collect timer states from the service
    val timerStates by timerService?.timerStates?.collectAsState(emptyMap()) ?: remember {
        mutableStateOf(emptyMap<String, TimerService.TimerState>())
    }
    
    if (timers.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_timers_found),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(timers, key = { it.id }) { timer ->
                val timerState = timerStates[timer.id]
                
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)) + 
                            slideInVertically(animationSpec = tween(durationMillis = 300)) { it },
                    exit = fadeOut(animationSpec = tween(durationMillis = 300)) + 
                           slideOutVertically(animationSpec = tween(durationMillis = 300)) { it }
                ) {
                    TimerItem(
                        timer = timer,
                        timerState = timerState,
                        onStartTimer = { onStartTimer(timer.id) },
                        onPauseTimer = { onPauseTimer(timer.id) },
                        onResumeTimer = { onResumeTimer(timer.id) },
                        onStopTimer = { onStopTimer(timer.id) },
                        onDeleteTimer = { onDeleteTimer(timer.id) },
                        onSaveAsTemplate = { onSaveAsTemplate(timer.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TimerItem(
    timer: Timer,
    timerState: TimerService.TimerState?,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onDeleteTimer: () -> Unit,
    onSaveAsTemplate: () -> Unit
) {
    val duration = timer.durationMillis ?: 0L
    val elapsed = timerState?.elapsedTime ?: timer.elapsedTimeMillis
    val progress = if (duration > 0) (elapsed.toFloat() / duration).coerceIn(0f, 1f) else 0f
    val animatedProgress = animateFloatAsState(
        targetValue = progress, 
        animationSpec = tween(durationMillis = 300),
        label = "progress"
    )
    val status = timerState?.status ?: timer.status
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
                    text = timer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row {
                    // Save as template button
                    IconButton(
                        onClick = onSaveAsTemplate
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = stringResource(R.string.save_as_template),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    // Delete button
                    IconButton(
                        onClick = onDeleteTimer
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timer display with pulsing animation when running
            val textScale = remember { androidx.compose.animation.core.Animatable(1f) }
            
            // Pulse animation for the timer text when it's running
            LaunchedEffect(status) {
                if (status == TimerStatus.RUNNING) {
                    while (true) {
                        textScale.animateTo(
                            targetValue = 1.05f,
                            animationSpec = tween(durationMillis = 500)
                        )
                        textScale.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(durationMillis = 500)
                        )
                        kotlinx.coroutines.delay(1000)
                    }
                } else {
                    // Reset scale when not running
                    textScale.snapTo(1f)
                }
            }
            
            Text(
                text = timerState?.formattedTime ?: formatTime(timer.elapsedTimeMillis, timer.durationMillis),
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier
                    .scale(textScale.value)
                    .align(Alignment.CenterHorizontally)
            )
            
            if (timer.repeat) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Repeats: ${if (timer.repeatCount <= 0) "∞" else timer.repeatCount} times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress indicator
            LinearProgressIndicator(
                progress = animatedProgress.value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                when (status) {
                    TimerStatus.IDLE -> {
                        ControlButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.start),
                            onClick = onStartTimer
                        )
                    }
                    TimerStatus.RUNNING -> {
                        ControlButton(
                            icon = Icons.Default.Pause,
                            contentDescription = stringResource(R.string.pause),
                            onClick = onPauseTimer
                        )
                        ControlButton(
                            icon = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.stop),
                            onClick = onStopTimer
                        )
                    }
                    TimerStatus.PAUSED -> {
                        ControlButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.resume),
                            onClick = onResumeTimer
                        )
                        ControlButton(
                            icon = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.stop),
                            onClick = onStopTimer
                        )
                    }
                    TimerStatus.COMPLETED -> {
                        ControlButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.start),
                            onClick = onStartTimer
                        )
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
    val scale = remember { androidx.compose.animation.core.Animatable(1f) }
    
    LaunchedEffect(Unit) {
        // Pulse animation
        kotlinx.coroutines.delay(500)
        scale.animateTo(
            targetValue = 1.1f,
            animationSpec = tween(durationMillis = 200)
        )
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 200)
        )
    }
    
    Box(
        modifier = Modifier
            .size(56.dp)
            .scale(scale.value)
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
fun CreateTimerDialog(
    hoursState: androidx.compose.runtime.State<Int>,
    minutesState: androidx.compose.runtime.State<Int>,
    secondsState: androidx.compose.runtime.State<Int>,
    nameState: androidx.compose.runtime.State<String>,
    repeatState: androidx.compose.runtime.State<Boolean> = androidx.compose.runtime.remember { mutableStateOf(false) },
    repeatCountState: androidx.compose.runtime.State<Int> = androidx.compose.runtime.remember { mutableStateOf(0) },
    onHoursChange: (Int) -> Unit,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    onNameChange: (String) -> Unit,
    onRepeatChange: (Boolean) -> Unit = {},
    onRepeatCountChange: (Int) -> Unit = {},
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_timer)) },
        text = {
            Column {
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.timer_name)) },
                    placeholder = { Text(stringResource(R.string.enter_timer_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Hours
                    OutlinedTextField(
                        value = hoursState.value.toString(),
                        onValueChange = { 
                            val hours = it.toIntOrNull() ?: 0
                            onHoursChange(hours.coerceIn(0, 99)) 
                        },
                        label = { Text(stringResource(R.string.hours)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.size(8.dp))
                    
                    // Minutes
                    OutlinedTextField(
                        value = minutesState.value.toString(),
                        onValueChange = { 
                            val minutes = it.toIntOrNull() ?: 0
                            onMinutesChange(minutes.coerceIn(0, 59)) 
                        },
                        label = { Text(stringResource(R.string.minutes)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.size(8.dp))
                    
                    // Seconds
                    OutlinedTextField(
                        value = secondsState.value.toString(),
                        onValueChange = { 
                            val seconds = it.toIntOrNull() ?: 0
                            onSecondsChange(seconds.coerceIn(0, 59)) 
                        },
                        label = { Text(stringResource(R.string.seconds)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Repeat timer option
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = repeatState.value,
                        onCheckedChange = onRepeatChange
                    )
                    Text(
                        text = stringResource(R.string.repeat_group),
                        modifier = Modifier.clickable { onRepeatChange(!repeatState.value) }
                    )
                }
                
                // Repeat count field (only shown if repeat is enabled)
                if (repeatState.value) {
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = repeatCountState.value.toString(),
                        onValueChange = { 
                            val count = it.toIntOrNull() ?: 0
                            onRepeatCountChange(count.coerceAtLeast(0)) 
                        },
                        label = { Text(stringResource(R.string.repeat_count)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate()
                },
                enabled = hoursState.value > 0 || minutesState.value > 0 || secondsState.value > 0
            ) {
                Text(stringResource(R.string.create_timer))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// Helper function to format time
fun formatTime(elapsed: Long, total: Long?): String {
    val isCountdown = total != null && total > 0
    
    val timeToFormat = if (isCountdown) {
        val remaining = total!! - elapsed
        if (remaining < 0) 0 else remaining
    } else {
        elapsed
    }
    
    val hours = timeToFormat / (1000 * 60 * 60)
    val minutes = (timeToFormat % (1000 * 60 * 60)) / (1000 * 60)
    val seconds = (timeToFormat % (1000 * 60)) / 1000
    
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
