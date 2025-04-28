package com.lasttimer.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import androi            }
        }
        
        // Start foreground service with a basic notification
        // The specific timer notification will be updated once the timer starts
        startForeground(NOTIFICATION_ID, createNotification("LastTimer is running").build())p.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.lasttimer.app.R
import com.lasttimer.app.data.model.Timer
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.model.TimerType
import com.lasttimer.app.data.repository.TimerRepository
import com.lasttimer.app.receiver.TimerActionReceiver
import com.lasttimer.app.ui.MainActivity
import com.lasttimer.app.util.formatTime
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class TimerService : Service() {
    
    @Inject
    lateinit var timerRepository: TimerRepository
    
    private val job = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + job)
    
    private val binder = TimerBinder()
    
    private var wakeLock: PowerManager.WakeLock? = null
    
    // Active timers
    private val activeTimers = mutableMapOf<String, CountDownTimer?>()
    private val _timerStates = MutableStateFlow<Map<String, TimerState>>(emptyMap())
    val timerStates: StateFlow<Map<String, TimerState>> = _timerStates.asStateFlow()
    
    // Track stopwatch base time
    private val stopwatchBaseTime = mutableMapOf<String, Long>()
    
    // Track timer groups
    private val activeGroups = mutableMapOf<String, String>() // Maps timerId to groupId
    
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "TimerServiceChannel"
        private const val WAKELOCK_TAG = "LastTimer:TimerServiceWakeLock"
        
        // Intent actions
        const val ACTION_START_TIMER = "com.lasttimer.app.action.START_TIMER"
        const val ACTION_PAUSE_TIMER = "com.lasttimer.app.action.PAUSE_TIMER"
        const val ACTION_RESUME_TIMER = "com.lasttimer.app.action.RESUME_TIMER"
        const val ACTION_STOP_TIMER = "com.lasttimer.app.action.STOP_TIMER"
        const val ACTION_TIMER_COMPLETED = "com.lasttimer.app.action.TIMER_COMPLETED"
        const val ACTION_START_GROUP_TIMER = "com.lasttimer.app.action.START_GROUP_TIMER"
        
        // Intent extras
        const val EXTRA_TIMER_ID = "com.lasttimer.app.extra.TIMER_ID"
        const val EXTRA_GROUP_ID = "com.lasttimer.app.extra.GROUP_ID"
        const val EXTRA_IS_GROUP = "com.lasttimer.app.extra.IS_GROUP"
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_TIMER -> {
                intent.getStringExtra(EXTRA_TIMER_ID)?.let { timerId ->
                    val groupId = intent.getStringExtra(EXTRA_GROUP_ID)
                    
                    serviceScope.launch {
                        if (groupId != null) {
                            // This is a timer within a group
                            activeGroups[timerId] = groupId
                        }
                        startTimer(timerId)
                    }
                }
            }
            ACTION_START_GROUP_TIMER -> {
                intent.getStringExtra(EXTRA_GROUP_ID)?.let { groupId ->
                    serviceScope.launch {
                        startTimerGroup(groupId)
                    }
                }
            }
            ACTION_PAUSE_TIMER -> {
                intent.getStringExtra(EXTRA_TIMER_ID)?.let { timerId ->
                    serviceScope.launch {
                        pauseTimer(timerId)
                        
                        // Update notification with timer name
                        val timer = timerRepository.getTimerById(timerId).first()
                        if (timer != null) {
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.notify(NOTIFICATION_ID, createNotification("Timer paused: ${timer.name}", timer.id).build())
                        }
                    }
                }
            }
            ACTION_RESUME_TIMER -> {
                intent.getStringExtra(EXTRA_TIMER_ID)?.let { timerId ->
                    serviceScope.launch {
                        resumeTimer(timerId)
                        
                        // Update notification with timer name
                        val timer = timerRepository.getTimerById(timerId).first()
                        if (timer != null) {
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.notify(NOTIFICATION_ID, createNotification("Timer running: ${timer.name}", timer.id).build())
                        }
                    }
                }
            }
            ACTION_STOP_TIMER -> {
                intent.getStringExtra(EXTRA_TIMER_ID)?.let { timerId ->
                    serviceScope.launch {
                        stopTimer(timerId)
                        
                        // Update notification with timer name
                        val timer = timerRepository.getTimerById(timerId).first()
                        if (timer != null) {
                            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                            notificationManager.notify(NOTIFICATION_ID, createNotification("Timer stopped: ${timer.name}", timer.id).build())
                        }
                    }
                }
            }
        }
        
        startForeground(NOTIFICATION_ID, createNotification("LastTimer is running"))
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
        // Cancel all active timers
        activeTimers.forEach { (_, timer) -> timer?.cancel() }
        activeTimers.clear()
        
        job.cancel()
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    inner class TimerBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = "Timer notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                // Make the notification appear on the lock screen
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                // Enable notification lights
                enableLights(true)
                // Make notification vibrate (if enabled in settings)
                enableVibration(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(contentText: String, timerId: String? = null): NotificationCompat.Builder {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            notificationIntent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create lock screen/notification action intents
        val pauseIntent = Intent(this, TimerActionReceiver::class.java).apply {
            action = ACTION_PAUSE_TIMER
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        val resumeIntent = Intent(this, TimerActionReceiver::class.java).apply {
            action = ACTION_RESUME_TIMER
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        val stopIntent = Intent(this, TimerActionReceiver::class.java).apply {
            action = ACTION_STOP_TIMER
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        
        val pausePendingIntent = PendingIntent.getBroadcast(
            this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val resumePendingIntent = PendingIntent.getBroadcast(
            this, 2, resumeIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val stopPendingIntent = PendingIntent.getBroadcast(
            this, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get current timer state
        val currentTimerState = _timerStates.value.values.firstOrNull()?.status ?: TimerStatus.IDLE
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // Show on lock screen
            // Add action buttons based on current timer state
            .apply {
                when (currentTimerState) {
                    TimerStatus.RUNNING -> {
                        addAction(R.drawable.ic_pause, getString(R.string.pause), pausePendingIntent)
                        addAction(R.drawable.ic_stop, getString(R.string.stop), stopPendingIntent)
                    }
                    TimerStatus.PAUSED -> {
                        addAction(R.drawable.ic_play, getString(R.string.resume), resumePendingIntent)
                        addAction(R.drawable.ic_stop, getString(R.string.stop), stopPendingIntent)
                    }
                    else -> {
                        // No actions for IDLE or COMPLETED
                    }
                }
            }
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            WAKELOCK_TAG
        ).apply {
            acquire(10*60*1000L /*10 minutes*/)
        }
    }
    
    private fun releaseWakeLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        wakeLock = null
    }
    
    suspend fun startTimer(timerId: String) {
        val timer = timerRepository.getTimerById(timerId).first() ?: return
        
        // Update timer status in database
        timerRepository.updateTimerStatus(timerId, TimerStatus.RUNNING)
        timerRepository.updateLastUsedAt(timerId)
        
        // Cancel any existing timer for this ID
        activeTimers[timerId]?.cancel()
        
        when (timer.type) {
            TimerType.COUNTDOWN -> {
                startCountdownTimer(timer)
            }
            TimerType.STOPWATCH -> {
                startStopwatch(timer)
            }
            TimerType.DATE_COUNTDOWN -> {
                startDateCountdown(timer)
            }
        }
        
        // Update notification
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification("Timer running: ${timer.name}", timer.id).build())
    }
    
    private fun startCountdownTimer(timer: Timer) {
        val duration = timer.durationMillis ?: return
        val remaining = duration - timer.elapsedTimeMillis
        
        if (remaining <= 0) {
            handleTimerCompleted(timer.id)
            return
        }
        
        val countDownTimer = object : CountDownTimer(remaining, 100) {
            override fun onTick(millisUntilFinished: Long) {
                serviceScope.launch {
                    val elapsed = duration - millisUntilFinished
                    timerRepository.updateElapsedTime(timer.id, elapsed)
                    updateTimerState(timer.id, elapsed, duration, TimerStatus.RUNNING)
                }
            }
            
            override fun onFinish() {
                serviceScope.launch {
                    handleTimerCompleted(timer.id)
                }
            }
        }
        
        activeTimers[timer.id] = countDownTimer
        countDownTimer.start()
        
        updateTimerState(timer.id, timer.elapsedTimeMillis, duration, TimerStatus.RUNNING)
    }
    
    private fun startStopwatch(timer: Timer) {
        // For stopwatch, we store the base time (system time minus elapsed)
        val baseTime = SystemClock.elapsedRealtime() - timer.elapsedTimeMillis
        stopwatchBaseTime[timer.id] = baseTime
        
        // Create a timer to update every 100ms
        val countDownTimer = object : CountDownTimer(Long.MAX_VALUE, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsed = SystemClock.elapsedRealtime() - baseTime
                serviceScope.launch {
                    timerRepository.updateElapsedTime(timer.id, elapsed)
                    updateTimerState(timer.id, elapsed, null, TimerStatus.RUNNING)
                }
            }
            
            override fun onFinish() {
                // This should never be called as we're using Long.MAX_VALUE
            }
        }
        
        activeTimers[timer.id] = countDownTimer
        countDownTimer.start()
        
        updateTimerState(timer.id, timer.elapsedTimeMillis, null, TimerStatus.RUNNING)
    }
    
    private fun startDateCountdown(timer: Timer) {
        val targetDate = timer.targetDate ?: return
        val now = Date()
        
        // If target date is in the past, complete the timer
        if (targetDate.before(now)) {
            handleTimerCompleted(timer.id)
            return
        }
        
        val duration = targetDate.time - now.time
        
        val countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val elapsed = duration - millisUntilFinished
                serviceScope.launch {
                    timerRepository.updateElapsedTime(timer.id, elapsed)
                    updateTimerState(timer.id, elapsed, duration, TimerStatus.RUNNING)
                }
            }
            
            override fun onFinish() {
                serviceScope.launch {
                    handleTimerCompleted(timer.id)
                }
            }
        }
        
        activeTimers[timer.id] = countDownTimer
        countDownTimer.start()
        
        updateTimerState(timer.id, 0, duration, TimerStatus.RUNNING)
    }
    
    suspend fun pauseTimer(timerId: String) {
        // Cancel the active timer
        activeTimers[timerId]?.cancel()
        activeTimers[timerId] = null
        
        // Update timer status in database
        timerRepository.updateTimerStatus(timerId, TimerStatus.PAUSED)
        
        // Update timer state
        val timer = timerRepository.getTimerById(timerId).first() ?: return
        val duration = when (timer.type) {
            TimerType.COUNTDOWN -> timer.durationMillis
            TimerType.DATE_COUNTDOWN -> timer.targetDate?.time?.minus(Date().time)
            TimerType.STOPWATCH -> null
        }
        
        updateTimerState(timerId, timer.elapsedTimeMillis, duration, TimerStatus.PAUSED)
    }
    
    suspend fun resumeTimer(timerId: String) {
        val timer = timerRepository.getTimerById(timerId).first() ?: return
        
        // Only resume if the timer is paused
        if (timer.status != TimerStatus.PAUSED) return
        
        startTimer(timerId)
    }
    
    suspend fun stopTimer(timerId: String) {
        // Cancel the active timer
        activeTimers[timerId]?.cancel()
        activeTimers[timerId] = null
        
        // Reset the timer
        timerRepository.resetTimer(timerId)
        
        // Remove from state tracking
        val currentStates = _timerStates.value.toMutableMap()
        currentStates.remove(timerId)
        _timerStates.value = currentStates
    }
    
    private suspend fun handleTimerCompleted(timerId: String) {
        // Update timer status in database
        timerRepository.updateTimerStatus(timerId, TimerStatus.COMPLETED)
        
        // Cancel the active timer
        activeTimers[timerId]?.cancel()
        activeTimers[timerId] = null
        
        // Get the timer to check if it should repeat
        val timer = timerRepository.getTimerById(timerId).first() ?: return
        
        // Update UI state
        updateTimerState(
            timerId, 
            timer.durationMillis ?: 0, 
            timer.durationMillis, 
            TimerStatus.COMPLETED
        )
        
        // Send broadcast for timer completed
        val intent = Intent(this, TimerActionReceiver::class.java).apply {
            action = ACTION_TIMER_COMPLETED
            putExtra(EXTRA_TIMER_ID, timerId)
        }
        sendBroadcast(intent)
        
        // Check if this timer is part of a group
        val groupId = activeGroups[timerId]
        if (groupId != null) {
            // Remove this timer from active groups
            activeGroups.remove(timerId)
            
            // Check if there's a next timer in the group
            handleNextTimerInGroup(groupId, timerId)
        }
        // Handle repeating timers
        else if (timer.repeat) {
            // Reset and restart
            timerRepository.resetTimer(timerId)
            startTimer(timerId)
        }
    }
    }
    
    private fun updateTimerState(
        timerId: String,
        elapsedTime: Long,
        totalDuration: Long?,
        status: TimerStatus
    ) {
        val currentStates = _timerStates.value.toMutableMap()
        currentStates[timerId] = TimerState(
            elapsedTime = elapsedTime,
            totalDuration = totalDuration,
            status = status,
            formattedTime = formatTime(elapsedTime, totalDuration)
        )
        _timerStates.value = currentStates
    }
    
    // New methods for handling timer groups
    
    suspend fun startTimerGroup(groupId: String) {
        val group = timerRepository.getGroupById(groupId).first() ?: return
        
        // Update last used timestamp
        timerRepository.updateGroupLastUsedAt(groupId)
        
        // Get all timers in the group, sorted by position
        val groupItems = timerRepository.getGroupItems(groupId).first()
            .sortedBy { it.position }
        
        if (groupItems.isNotEmpty()) {
            // Start the first timer in the group
            val firstTimerId = groupItems.first().timerId
            activeGroups[firstTimerId] = groupId
            startTimer(firstTimerId)
        }
    }
    
    private suspend fun handleNextTimerInGroup(groupId: String, currentTimerId: String) {
        val group = timerRepository.getGroupById(groupId).first() ?: return
        
        // Get all timers in the group, sorted by position
        val groupItems = timerRepository.getGroupItems(groupId).first()
            .sortedBy { it.position }
            .toMutableList()
        
        // Find the current timer's position
        val currentPosition = groupItems.indexOfFirst { it.timerId == currentTimerId }
        
        if (currentPosition >= 0 && currentPosition < groupItems.size - 1) {
            // There is a next timer in the sequence
            val nextItem = groupItems[currentPosition + 1]
            
            // Only auto-start if the group setting allows it
            if (group.autoStartNext) {
                activeGroups[nextItem.timerId] = groupId
                startTimer(nextItem.timerId)
            }
        } else if (currentPosition == groupItems.size - 1) {
            // This was the last timer in the group
            if (group.repeatGroup) {
                // If the group should repeat, start from the beginning
                val firstItem = groupItems.first()
                
                // Check repeat count if it's not infinite
                val currentRepeatCycle = group.currentRepeatCycle
                val repeatCount = group.repeatCount
                
                if (repeatCount == null || repeatCount <= 0 || currentRepeatCycle < repeatCount - 1) {
                    // Update the group's repeat cycle
                    val updatedGroup = group.copy(
                        currentRepeatCycle = currentRepeatCycle + 1
                    )
                    timerRepository.updateGroup(updatedGroup)
                    
                    // Start the first timer again
                    activeGroups[firstItem.timerId] = groupId
                    startTimer(firstItem.timerId)
                } else {
                    // Reset the repeat cycle
                    val updatedGroup = group.copy(
                        currentRepeatCycle = 0
                    )
                    timerRepository.updateGroup(updatedGroup)
                    
                    // The group has completed all repetitions
                    // Send some kind of notification or update UI
                    val notification = createNotification("Timer group ${group.name} completed.")
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID + 1, notification.build())
                }
            } else {
                // The group has completed without repeating
                // Send some kind of notification or update UI
                val notification = createNotification("Timer group ${group.name} completed.")
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(NOTIFICATION_ID + 1, notification.build())
            }
        }
    }
    
    // Data class for timer state
    data class TimerState(
        val elapsedTime: Long,
        val totalDuration: Long?,
        val status: TimerStatus,
        val formattedTime: String
    )
}
