package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log

class AudioSessionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "unknown"

        Log.d("AudioSessionReceiver", "Received action $action for session $sessionId from $packageName")

        val serviceIntent = Intent(context, AudioEffectsService::class.java).apply {
            this.action = action
            putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId)
            putExtra(AudioEffect.EXTRA_PACKAGE_NAME, packageName)
        }

        try {
            context.startService(serviceIntent)
        } catch (e: Exception) {
            Log.w("AudioSessionReceiver", "Could not start AudioEffectsService: ${e.message}")
        }
    }
}
