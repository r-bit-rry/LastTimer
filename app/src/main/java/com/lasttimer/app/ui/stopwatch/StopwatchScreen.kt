package com.lasttimer.app.ui.stopwatch

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import com.lasttimer.app.data.model.TimerLap
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.service.TimerService
import com.lasttimer.app.util.formatTimeHhMmSs
import com.lasttimer.app.util.formatTimeWithTenths
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val activeStopwatch by viewModel.activeStopwatch.collectAsState()
    val stopwatchLaps by viewModel.stopwatchLaps.collectAsState()
    val isCreateStopwatchDialogVisible by viewModel.isCreateStopwatchDialogVisible.collectAsState()
    
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
                title = { Text(stringResource(R.string.tab_stopwatch)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (activeStopwatch == null) {
                FloatingActionButton(
                    onClick = { viewModel.showCreateStopwatchDialog() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_timer))
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeStopwatch != null) {
                // Show active stopwatch with laps
                val timerState = timerService?.timerStates?.collectAsState()?.value?.get(activeStopwatch?.id)
                
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    ActiveStopwatchDisplay(
                        stopwatch = activeStopwatch!!,
                        timerState = timerState,
                        onPauseStopwatch = {
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_PAUSE_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, activeStopwatch!!.id)
                            }
                            context.startService(intent)
                            viewModel.pauseStopwatch(activeStopwatch!!.id, intent)
                        },
                        onResumeStopwatch = {
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_RESUME_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, activeStopwatch!!.id)
                            }
                            context.startService(intent)
                            viewModel.resumeStopwatch(activeStopwatch!!.id, intent)
                        },
                        onResetStopwatch = {
                            val intent = Intent(context, TimerService::class.java).apply {
                                action = TimerService.ACTION_STOP_TIMER
                                putExtra(TimerService.EXTRA_TIMER_ID, activeStopwatch!!.id)
                            }
                            context.startService(intent)
                            viewModel.resetStopwatch(activeStopwatch!!.id, intent)
                        },
                        onAddLap = {
                            viewModel.addLap(activeStopwatch!!.id)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Laps list
                    if (stopwatchLaps.isNotEmpty()) {
                        Text(
                            text = "Laps",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LapsList(laps = stopwatchLaps)
                    }
                }
            } else {
                when (uiState) {
                    is StopwatchViewModel.StopwatchUiState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    
                    is StopwatchViewModel.StopwatchUiState.Error -> {
                        Text(
                            text = (uiState as StopwatchViewModel.StopwatchUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    
                    is StopwatchViewModel.StopwatchUiState.Success -> {
                        val stopwatchesFlow = (uiState as StopwatchViewModel.StopwatchUiState.Success).stopwatches
                        StopwatchList(
                            stopwatchesFlow = stopwatchesFlow,
                            timerService = timerService,
                            onStartStopwatch = { stopwatchId ->
                                val intent = Intent(context, TimerService::class.java).apply {
                                    action = TimerService.ACTION_START_TIMER
                                    putExtra(TimerService.EXTRA_TIMER_ID, stopwatchId)
                                }
                                context.startService(intent)
                                viewModel.startStopwatch(stopwatchId, intent)
                            },
                            onDeleteStopwatch = { stopwatchId ->
                                viewModel.deleteStopwatch(stopwatchId)
                            }
                        )
                    }
                }
            }
        }
    }
    
    if (isCreateStopwatchDialogVisible) {
        CreateStopwatchDialog(
            nameState = viewModel.newStopwatchName.collectAsState(),
            onNameChange = { viewModel.updateNewStopwatchName(it) },
            onDismiss = { viewModel.hideCreateStopwatchDialog() },
            onCreate = { viewModel.createStopwatch() }
        )
    }
}

@Composable
fun StopwatchList(
    stopwatchesFlow: Flow<List<Timer>>,
    timerService: TimerService?,
    onStartStopwatch: (String) -> Unit,
    onDeleteStopwatch: (String) -> Unit
) {
    val stopwatches by remember(stopwatchesFlow) {
        mutableStateOf<List<Timer>>(emptyList())
    }
    
    // Collect stopwatches from the flow
    LaunchedEffect(stopwatchesFlow) {
        stopwatchesFlow.collect { newStopwatches ->
            stopwatches = newStopwatches
        }
    }
    
    if (stopwatches.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No stopwatches found.\nCreate one to get started!",
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
            items(stopwatches, key = { it.id }) { stopwatch ->
                StopwatchItem(
                    stopwatch = stopwatch,
                    onStartStopwatch = { onStartStopwatch(stopwatch.id) },
                    onDeleteStopwatch = { onDeleteStopwatch(stopwatch.id) }
                )
            }
        }
    }
}

@Composable
fun StopwatchItem(
    stopwatch: Timer,
    onStartStopwatch: () -> Unit,
    onDeleteStopwatch: () -> Unit
) {
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
                    text = stopwatch.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = onDeleteStopwatch
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "00:00.0",
                    style = MaterialTheme.typography.titleLarge
                )
                
                IconButton(
                    onClick = onStartStopwatch,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = stringResource(R.string.start),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveStopwatchDisplay(
    stopwatch: Timer,
    timerState: TimerService.TimerState?,
    onPauseStopwatch: () -> Unit,
    onResumeStopwatch: () -> Unit,
    onResetStopwatch: () -> Unit,
    onAddLap: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stopwatch.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = timerState?.formattedTime ?: formatTimeWithTenths(stopwatch.elapsedTimeMillis),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Control buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                when (stopwatch.status) {
                    TimerStatus.RUNNING -> {
                        StopwatchControlButton(
                            icon = Icons.Default.Flag,
                            contentDescription = "Lap",
                            onClick = onAddLap
                        )
                        
                        StopwatchControlButton(
                            icon = Icons.Default.Pause,
                            contentDescription = stringResource(R.string.pause),
                            onClick = onPauseStopwatch
                        )
                        
                        StopwatchControlButton(
                            icon = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.stop),
                            onClick = onResetStopwatch
                        )
                    }
                    TimerStatus.PAUSED -> {
                        StopwatchControlButton(
                            icon = Icons.Default.Flag,
                            contentDescription = "Lap",
                            onClick = onAddLap,
                            enabled = false
                        )
                        
                        StopwatchControlButton(
                            icon = Icons.Default.PlayArrow,
                            contentDescription = stringResource(R.string.resume),
                            onClick = onResumeStopwatch
                        )
                        
                        StopwatchControlButton(
                            icon = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.stop),
                            onClick = onResetStopwatch
                        )
                    }
                    else -> {
                        // Should not reach here since the active stopwatch should always be running or paused
                    }
                }
            }
        }
    }
}

@Composable
fun StopwatchControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(
                color = if (enabled) MaterialTheme.colorScheme.primaryContainer 
                       else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) MaterialTheme.colorScheme.onPrimaryContainer 
                  else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun LapsList(laps: List<TimerLap>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth()
    ) {
        itemsIndexed(
            items = laps.sortedByDescending { it.lapNumber },
            key = { _, lap -> lap.id }
        ) { index, lap ->
            LapItem(
                lapNumber = lap.lapNumber,
                elapsedTime = lap.elapsedTimeMillis
            )
            
            if (index < laps.size - 1) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun LapItem(
    lapNumber: Int,
    elapsedTime: Long
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Lap $lapNumber",
            style = MaterialTheme.typography.bodyLarge
        )
        
        Text(
            text = formatTimeHhMmSs(elapsedTime),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun CreateStopwatchDialog(
    nameState: androidx.compose.runtime.State<String>,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Stopwatch") },
        text = {
            OutlinedTextField(
                value = nameState.value,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.timer_name)) },
                placeholder = { Text("Enter stopwatch name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = onCreate
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
