package com.lasttimer.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.lasttimer.app.data.preferences.SettingsPreferences
import com.lasttimer.app.data.repository.TimerRepository
import com.lasttimer.app.service.TimerService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TimerActionReceiver : BroadcastReceiver() {
    
    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface TimerActionReceiverEntryPoint {
        fun timerRepository(): TimerRepository
        fun settingsPreferences(): SettingsPreferences
    }
    
    private fun getEntryPoint(context: Context): TimerActionReceiverEntryPoint {
        return EntryPointAccessors.fromApplication(
            context.applicationContext,
            TimerActionReceiverEntryPoint::class.java
        )
    }
    
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    
    override fun onReceive(context: Context, intent: Intent) {
        val timerId = intent.getStringExtra(TimerService.EXTRA_TIMER_ID) ?: return
        
        when (intent.action) {
            TimerService.ACTION_TIMER_COMPLETED -> {
                scope.launch {
                    handleTimerCompleted(context, timerId)
                }
            }
            TimerService.ACTION_PAUSE_TIMER,
            TimerService.ACTION_RESUME_TIMER,
            TimerService.ACTION_STOP_TIMER -> {
                // Forward these actions to the timer service
                val serviceIntent = Intent(context, TimerService::class.java).apply {
                    action = intent.action
                    putExtra(TimerService.EXTRA_TIMER_ID, timerId)
                }
                context.startService(serviceIntent)
            }
        }
    }
    
    private suspend fun handleTimerCompleted(context: Context, @Suppress("UNUSED_PARAMETER") timerId: String) {
        // Get EntryPoint for dependency access
        val entryPoint = getEntryPoint(context)
        
        // Get sound and vibration preferences
        val soundEnabled = entryPoint.settingsPreferences().soundEnabled.first()
        val vibrationEnabled = entryPoint.settingsPreferences().vibrationEnabled.first()
        
        // Play sound if enabled
        if (soundEnabled) {
            try {
                val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val ringtone = RingtoneManager.getRingtone(context, notification)
                ringtone.play()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        // Vibrate if enabled
        if (vibrationEnabled) {
            val vibrationPattern = longArrayOf(0, 500, 200, 500)
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(vibrationPattern, -1)
                }
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(vibrationPattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(vibrationPattern, -1)
                }
            }
        }
    }
}
