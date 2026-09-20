package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.model.AudioEffectsConfig

class AudioEffectsService : Service() {

    private val binder = LocalBinder()
    private var engine: AudioEffectsEngine? = null

    companion object {
        const val CHANNEL_ID = "audio_effects_dsp_channel"
        const val NOTIFICATION_ID = 101

        const val ACTION_TOGGLE_MASTER = "com.example.action.TOGGLE_MASTER"
        const val ACTION_START_FOREGROUND = "com.example.action.START_FOREGROUND"
        const val ACTION_STOP_FOREGROUND = "com.example.action.STOP_FOREGROUND"

        @Volatile
        var instance: AudioEffectsService? = null
            private set
    }

    inner class LocalBinder : Binder() {
        fun getService(): AudioEffectsService = this@AudioEffectsService
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        engine = AudioEffectsEngine(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        when (action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
                if (sessionId != 0) {
                    engine?.attachSession(sessionId)
                }
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
                if (sessionId != 0) {
                    engine?.detachSession(sessionId)
                }
            }
            ACTION_TOGGLE_MASTER -> {
                val current = engine?.currentConfig ?: AudioEffectsConfig()
                val updated = current.copy(masterEnabled = !current.masterEnabled)
                engine?.applyConfig(updated)
                updateNotification(updated)
            }
            ACTION_START_FOREGROUND -> {
                startForeground(NOTIFICATION_ID, buildNotification(engine?.currentConfig ?: AudioEffectsConfig()))
            }
            ACTION_STOP_FOREGROUND -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            else -> {
                try {
                    startForeground(NOTIFICATION_ID, buildNotification(engine?.currentConfig ?: AudioEffectsConfig()))
                } catch (_: Exception) {}
            }
        }

        return START_STICKY
    }

    fun applyConfig(config: AudioEffectsConfig) {
        engine?.applyConfig(config)
        updateNotification(config)
    }

    fun getEngine(): AudioEffectsEngine? = engine

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "System Audio Effects",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active audio effects status and quick controls"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun updateNotification(config: AudioEffectsConfig) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        try {
            manager.notify(NOTIFICATION_ID, buildNotification(config))
        } catch (_: Exception) {}
    }

    private fun buildNotification(config: AudioEffectsConfig): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(this, AudioEffectsService::class.java).apply {
            action = ACTION_TOGGLE_MASTER
        }
        val pendingToggle = PendingIntent.getService(
            this,
            1,
            toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val status = if (config.masterEnabled && !config.bypassAll) "DSP Active" else "DSP Bypassed"
        val statusText = "$status • ${config.activePresetName} (${if (config.masterGainDb >= 0) "+" else ""}${String.format("%.1f", config.masterGainDb)} dB)"

        val toggleLabel = if (config.masterEnabled) "Disable Effects" else "Enable Effects"

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Audio Effects Studio")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingOpenApp)
            .addAction(0, toggleLabel, pendingToggle)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        engine?.release()
        engine = null
        if (instance == this) {
            instance = null
        }
    }
}
