package com.lasttimer.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lasttimer.app.data.model.TimerStatus
import com.lasttimer.app.data.repository.TimerRepository
import com.lasttimer.app.service.TimerService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var timerRepository: TimerRepository
    
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scope.launch {
                restoreActiveTimers(context)
            }
        }
    }
    
    private suspend fun restoreActiveTimers(context: Context) {
        // Get all running timers
        val runningTimers = timerRepository.getTimersByStatus(TimerStatus.RUNNING).first()
        
        // Start the service for each running timer
        runningTimers.forEach { timer ->
            val serviceIntent = Intent(context, TimerService::class.java).apply {
                action = TimerService.ACTION_START_TIMER
                putExtra(TimerService.EXTRA_TIMER_ID, timer.id)
            }
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
