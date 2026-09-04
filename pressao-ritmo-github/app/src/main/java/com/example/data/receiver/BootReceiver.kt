package com.example.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.reminder.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            Log.d("BootReceiver", "Device restarted or app replaced, rescheduling enabled blood pressure reminders...")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val enabledReminders = db.reminderDao().getEnabledRemindersDirect()
                    for (reminder in enabledReminders) {
                        ReminderScheduler.scheduleReminder(context, reminder)
                    }
                    Log.d("BootReceiver", "Successfully rescheduled ${enabledReminders.size} reminders.")
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error while rescheduling reminders on boot", e)
                }
            }
        }
    }
}
