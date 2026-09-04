package com.example.domain.rules

import com.example.domain.model.BmiClassification
import com.example.domain.model.DateFilter
import com.example.domain.model.Measurement
import com.example.domain.model.MeasurementStats
import com.example.domain.model.PressureClassification
import com.example.domain.model.SortOrder
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max
import kotlin.math.roundToInt

object HealthCalculator {

    /**
     * Classifies blood pressure according to SBC (Brazilian Society of Cardiology) 2025 Guidelines.
     * Uses the highest stage reached between systolic and diastolic values.
     */
    fun classifyPressure(systolic: Int, diastolic: Int): PressureClassification {
        val systolicLevel = when {
            systolic < 120 -> 0
            systolic in 120..139 -> 1
            systolic in 140..159 -> 2
            systolic in 160..179 -> 3
            else -> 4
        }

        val diastolicLevel = when {
            diastolic < 80 -> 0
            diastolic in 80..89 -> 1
            diastolic in 90..99 -> 2
            diastolic in 100..109 -> 3
            else -> 4
        }

        val highestLevel = max(systolicLevel, diastolicLevel)
        return when (highestLevel) {
            0 -> PressureClassification.NORMAL
            1 -> PressureClassification.PRE_HIPERTENSAO
            2 -> PressureClassification.HIPERTENSAO_ESTAGIO_1
            3 -> PressureClassification.HIPERTENSAO_ESTAGIO_2
            else -> PressureClassification.HIPERTENSAO_ESTAGIO_3
        }
    }

    /**
     * Calculates BMI = weight (kg) / (height (m) * height (m))
     * Returns value rounded to 1 decimal place.
     */
    fun calculateBmi(weight: Double, height: Double): Double? {
        if (weight <= 0.0 || height <= 0.0) return null
        val bmi = weight / (height * height)
        return (bmi * 10.0).roundToInt() / 10.0
    }

    /**
     * Classifies BMI value into World Health Organization standard categories.
     */
    fun classifyBmi(bmi: Double): BmiClassification {
        return when {
            bmi < 18.5 -> BmiClassification.ABAIXO_DO_PESO
            bmi < 25.0 -> BmiClassification.PESO_ADEQUADO
            bmi < 30.0 -> BmiClassification.SOBREPESO
            bmi < 35.0 -> BmiClassification.OBESIDADE_GRAU_1
            bmi < 40.0 -> BmiClassification.OBESIDADE_GRAU_2
            else -> BmiClassification.OBESIDADE_GRAU_3
        }
    }

    /**
     * Calculates age from birth date string (yyyy-MM-dd) relative to current local date.
     */
    fun calculateAge(birthDateStr: String?, referenceDate: LocalDate = LocalDate.now()): Int? {
        if (birthDateStr.isNullOrBlank()) return null
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birthDate = LocalDate.parse(birthDateStr.trim(), formatter)
            if (birthDate.isAfter(referenceDate)) return null
            val period = Period.between(birthDate, referenceDate)
            period.years
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Calculates summary statistics over a list of measurements.
     */
    fun calculateStatistics(measurements: List<Measurement>): MeasurementStats {
        if (measurements.isEmpty()) {
            return MeasurementStats(
                totalCount = 0,
                avgSystolic = null,
                avgDiastolic = null,
                avgHeartRate = null,
                minSystolic = null,
                maxSystolic = null,
                minDiastolic = null,
                maxDiastolic = null,
                minHeartRate = null,
                maxHeartRate = null
            )
        }

        val systolicSum = measurements.sumOf { it.systolic }
        val diastolicSum = measurements.sumOf { it.diastolic }
        val heartRateSum = measurements.sumOf { it.heartRate }
        val count = measurements.size.toDouble()

        val avgSys = (systolicSum / count * 10.0).roundToInt() / 10.0
        val avgDia = (diastolicSum / count * 10.0).roundToInt() / 10.0
        val avgHr = (heartRateSum / count * 10.0).roundToInt() / 10.0

        return MeasurementStats(
            totalCount = measurements.size,
            avgSystolic = avgSys,
            avgDiastolic = avgDia,
            avgHeartRate = avgHr,
            minSystolic = measurements.minOfOrNull { it.systolic },
            maxSystolic = measurements.maxOfOrNull { it.systolic },
            minDiastolic = measurements.minOfOrNull { it.diastolic },
            maxDiastolic = measurements.maxOfOrNull { it.diastolic },
            minHeartRate = measurements.minOfOrNull { it.heartRate },
            maxHeartRate = measurements.maxOfOrNull { it.heartRate }
        )
    }

    /**
     * Filters and sorts measurements by optional start date, end date, and order.
     */
    fun filterMeasurements(
        measurements: List<Measurement>,
        filter: DateFilter
    ): List<Measurement> {
        var result = measurements

        if (filter.startDate != null) {
            val startOfDay = filter.startDate
            result = result.filter { it.measuredAtEpoch >= startOfDay }
        }

        if (filter.endDate != null) {
            // End date includes the entire day up to 23:59:59.999
            val endOfDay = filter.endDate
            result = result.filter { it.measuredAtEpoch <= endOfDay }
        }

        return when (filter.sortOrder) {
            SortOrder.NEWEST_FIRST -> result.sortedByDescending { it.measuredAtEpoch }
            SortOrder.OLDEST_FIRST -> result.sortedBy { it.measuredAtEpoch }
        }
    }

    /**
     * Formats epoch millis into readable Brazilian format "dd/MM/yyyy HH:mm"
     */
    fun formatEpochToDisplay(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    /**
     * Formats epoch millis into date only "dd/MM/yyyy"
     */
    fun formatEpochToDateOnly(epochMillis: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    /**
     * Formats epoch millis into time only "HH:mm"
     */
    fun formatEpochToTimeOnly(epochMillis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(epochMillis))
    }

    /**
     * Formats epoch millis to ISO 8601 string "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
     */
    fun formatEpochToIso(epochMillis: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date(epochMillis))
    }

    /**
     * Parses ISO 8601 string or date string to epoch millis.
     */
    fun parseIsoToEpoch(isoStr: String): Long {
        if (isoStr.isBlank()) return System.currentTimeMillis()
        return try {
            Instant.parse(isoStr).toEpochMilli()
        } catch (_: Exception) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                sdf.parse(isoStr)?.time ?: System.currentTimeMillis()
            } catch (_: Exception) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    sdf.parse(isoStr)?.time ?: System.currentTimeMillis()
                } catch (_: Exception) {
                    System.currentTimeMillis()
                }
            }
        }
    }
}
