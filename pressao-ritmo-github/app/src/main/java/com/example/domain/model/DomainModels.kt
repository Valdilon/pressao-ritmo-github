package com.example.domain.model

data class UserProfile(
    val id: Long = 1L,
    val fullName: String,
    val sex: String,
    val birthDate: String?, // yyyy-MM-dd
    val age: Int?,
    val weight: Double,
    val height: Double,
    val bmi: Double?,
    val bmiClassification: BmiClassification?,
    val isDefault: Boolean = false,
    val avatarColorHex: String = "#6750A4",
    val photoUri: String? = null
) {
    val initials: String
        get() {
            val parts = fullName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
            return when {
                parts.isEmpty() -> "U"
                parts.size == 1 -> parts[0].take(2).uppercase()
                else -> "${parts[0].first()}${parts[1].first()}".uppercase()
            }
        }
}

data class Measurement(
    val id: String,
    val profileId: Long = 1L,
    val systolic: Int,
    val diastolic: Int,
    val heartRate: Int,
    val measuredAt: String,      // ISO string e.g. 2026-08-12T12:42:00.000Z
    val measuredAtEpoch: Long,  // Milliseconds
    val formattedDate: String,  // e.g. "12/08/2026 09:42"
    val formattedTime: String,  // e.g. "09:42"
    val observation: String,
    val classification: PressureClassification,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class ReminderCategory(val label: String) {
    AFERICAO("Aferição"),
    MEDICAMENTO("Medicamento");

    companion object {
        fun fromName(name: String?): ReminderCategory = entries.firstOrNull { it.name == name } ?: AFERICAO
    }
}

data class Reminder(
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val label: String,
    val category: ReminderCategory = ReminderCategory.AFERICAO,
    val daysOfWeek: String = "1,2,3,4,5,6,7", // 1=Sun, 2=Mon...
    val isEnabled: Boolean = true,
    val profileId: Long? = null,
    val soundUri: String? = null,
    val formattedTime: String = String.format("%02d:%02d", hour, minute)
)

data class MeasurementStats(
    val totalCount: Int,
    val avgSystolic: Double?,
    val avgDiastolic: Double?,
    val avgHeartRate: Double?,
    val minSystolic: Int?,
    val maxSystolic: Int?,
    val minDiastolic: Int?,
    val maxDiastolic: Int?,
    val minHeartRate: Int?,
    val maxHeartRate: Int?
)

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

data class DateFilter(
    val startDate: Long? = null, // Start of day in millis
    val endDate: Long? = null,   // End of day in millis
    val sortOrder: SortOrder = SortOrder.NEWEST_FIRST
)

