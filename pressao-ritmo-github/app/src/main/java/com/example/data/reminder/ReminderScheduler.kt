package com.example.data.reminder

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.entity.ReminderEntity
import com.example.data.receiver.ReminderReceiver
import java.util.Calendar

object ReminderScheduler {

    const val CHANNEL_ID = "channel_blood_pressure_reminders_v2"
    const val CHANNEL_NAME = "Lembretes de Medição"
    const val CHANNEL_DESC = "Notificações diárias para lembrar de aferir a pressão arterial"

    private const val TAG = "ReminderScheduler"

    /**
     * Ensures the notification channel exists on Android 8.0 (API 26) and above.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            
            // Define default sound
            val soundUri = Uri.parse("android.resource://${context.packageName}/${R.raw.heartbeat_sound}")
            
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
                
                // Set default sound for the channel
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build()
                setSound(soundUri, audioAttributes)
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    /**
     * Schedules a daily recurring alarm for the given reminder.
     */
    fun scheduleReminder(context: Context, reminder: ReminderEntity) {
        if (!reminder.isEnabled) {
            cancelReminder(context, reminder.id)
            return
        }

        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerTimeMillis = calculateNextTriggerTime(reminder.hour, reminder.minute)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_REMINDER
            putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.id)
            putExtra(ReminderReceiver.EXTRA_REMINDER_LABEL, reminder.label)
            putExtra(ReminderReceiver.EXTRA_REMINDER_HOUR, reminder.hour)
            putExtra(ReminderReceiver.EXTRA_REMINDER_MINUTE, reminder.minute)
            putExtra(ReminderReceiver.EXTRA_REMINDER_SOUND_URI, reminder.soundUri)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled reminder #${reminder.id} (${reminder.label}) for $triggerTimeMillis")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to set exact alarm: permission denied", e)
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } catch (ex: Exception) {
                Log.e(TAG, "Fallback alarm failed", ex)
            }
        }
    }

    /**
     * Cancels an existing scheduled reminder.
     */
    fun cancelReminder(context: Context, reminderId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_TRIGGER_REMINDER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelled reminder #$reminderId")
        }
    }

    /**
     * Calculates the next timestamp in epoch millis for the specified hour and minute.
     * If the time is already past for today, schedules for tomorrow.
     */
    fun calculateNextTriggerTime(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    /**
     * Sends an immediate test notification so the user can verify permissions and audio/visual alerts.
     */
    fun sendTestNotification(context: Context) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val heartbeatSound = Uri.parse("android.resource://" + context.packageName + "/" + R.raw.heartbeat_sound)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Lembrete de Pressão Arterial 🩺")
            .setContentText("Notificações funcionando! É hora de medir sua pressão arterial e registrar.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Notificações funcionando perfeitamente! Você receberá seus lembretes nos horários configurados para manter sua saúde em dia."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(heartbeatSound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        notificationManager?.notify(9999, notification)
    }
}
