package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val hour: Int, // 0..23
    val minute: Int, // 0..59
    val label: String, // e.g. "Manhã", "Tarde", "Noite", "Após Medicação"
    val category: String = "AFERICAO",
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Sun, 2=Mon, etc.
    val isEnabled: Boolean = true,
    val profileId: Long? = null, // Optional association with a profile
    val soundUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
