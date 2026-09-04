package com.example.data.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.entity.ReminderEntity
import com.example.data.reminder.ReminderScheduler

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TRIGGER_REMINDER) return

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, 0L)
        val label = intent.getStringExtra(EXTRA_REMINDER_LABEL) ?: "Medição de Rotina"
        val hour = intent.getIntExtra(EXTRA_REMINDER_HOUR, 8)
        val minute = intent.getIntExtra(EXTRA_REMINDER_MINUTE, 0)
        val soundUriString = intent.getStringExtra(EXTRA_REMINDER_SOUND_URI)
        val soundUri = if (soundUriString.isNullOrBlank()) {
            Uri.parse("android.resource://${context.packageName}/${R.raw.heartbeat_sound}")
        } else {
            Uri.parse(soundUriString)
        }

        Log.d("ReminderReceiver", "Alarm triggered for reminder #$reminderId: $label ($hour:$minute) with sound $soundUri")

        ReminderScheduler.createNotificationChannel(context)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_TAB", "NEW_MEASUREMENT")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Hora de Aferir sua Pressão! 🩺")
            .setContentText("Lembrete: $label. Registre seus valores para manter seu controle em dia.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Lembrete: $label.\nReserve alguns minutos para relaxar, aferir sua pressão e registrar no aplicativo Pressão & Ritmo.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(reminderId.toInt(), notification)

        // Reschedule for next day
        val reminder = ReminderEntity(
            id = reminderId,
            hour = hour,
            minute = minute,
            label = label,
            soundUri = soundUriString,
            isEnabled = true
        )
        ReminderScheduler.scheduleReminder(context, reminder)
    }

    companion object {
        const val ACTION_TRIGGER_REMINDER = "com.example.ACTION_TRIGGER_REMINDER"
        const val EXTRA_REMINDER_ID = "extra_reminder_id"
        const val EXTRA_REMINDER_LABEL = "extra_reminder_label"
        const val EXTRA_REMINDER_HOUR = "extra_reminder_hour"
        const val EXTRA_REMINDER_MINUTE = "extra_reminder_minute"
        const val EXTRA_REMINDER_SOUND_URI = "extra_reminder_sound_uri"
    }
}
