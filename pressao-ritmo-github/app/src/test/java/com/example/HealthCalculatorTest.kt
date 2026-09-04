package com.example

import com.example.domain.backup.JsonBackupManager
import com.example.domain.model.BmiClassification
import com.example.domain.model.Measurement
import com.example.domain.model.PressureClassification
import com.example.domain.rules.HealthCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthCalculatorTest {

    @Test
    fun testPressureClassification_Normal() {
        assertEquals(PressureClassification.NORMAL, HealthCalculator.classifyPressure(115, 75))
        assertEquals(PressureClassification.NORMAL, HealthCalculator.classifyPressure(119, 79))
    }

    @Test
    fun testPressureClassification_PreHypertension() {
        assertEquals(PressureClassification.PRE_HIPERTENSAO, HealthCalculator.classifyPressure(125, 75))
        assertEquals(PressureClassification.PRE_HIPERTENSAO, HealthCalculator.classifyPressure(115, 84))
        assertEquals(PressureClassification.PRE_HIPERTENSAO, HealthCalculator.classifyPressure(139, 89))
    }

    @Test
    fun testPressureClassification_Stage1() {
        assertEquals(PressureClassification.HIPERTENSAO_ESTAGIO_1, HealthCalculator.classifyPressure(145, 85))
        assertEquals(PressureClassification.HIPERTENSAO_ESTAGIO_1, HealthCalculator.classifyPressure(120, 95))
        assertEquals(PressureClassification.HIPERTENSAO_ESTAGIO_1, HealthCalculator.classifyPressure(159, 99))
    }

    @Test
    fun testPressureClassification_Stage2() {
        assertEquals(PressureClassification.HIPERTENSAO_ESTAGIO_2, HealthCalculator.classifyPressure(165, 85))
        assertEquals(PressureClassification.HIPERTENSAO_ESTAGIO_2, HealthCalculator.classifyPressure(130, 105))
        assertEquals(PressureClassification.HIPERTENSAO_ESTAGIO_2, HealthCalculator.classifyPressure(179, 109))
    }

    @Test
    fun testPressureClassification_Stage3() {
        val stage3Sys = HealthCalculator.classifyPressure(185, 85)
        assertEquals(PressureClassification.HIPERTENSAO_ESTAGIO_3, stage3Sys)
        assertTrue(stage3Sys.isEmergencyAlert)

        val stage3Dia = HealthCalculator.classifyPressure(120, 115)
        assertEquals(PressureClassification.HIPERTENSAO_ESTAGIO_3, stage3Dia)
        assertTrue(stage3Dia.isEmergencyAlert)
    }

    @Test
    fun testBmiCalculationAndClassification() {
        val bmi = HealthCalculator.calculateBmi(70.0, 1.75)
        assertNotNull(bmi)
        assertEquals(22.9, bmi!!, 0.05)
        assertEquals(BmiClassification.PESO_ADEQUADO, HealthCalculator.classifyBmi(bmi))

        val obeseBmi = HealthCalculator.calculateBmi(110.0, 1.70)
        assertNotNull(obeseBmi)
        assertEquals(BmiClassification.OBESIDADE_GRAU_2, HealthCalculator.classifyBmi(obeseBmi!!))
    }

    @Test
    fun testStatisticsCalculation() {
        val measurements = listOf(
            Measurement(id = "1", systolic = 120, diastolic = 80, heartRate = 70, measuredAt = "1970-01-01T00:00:01.000Z", measuredAtEpoch = 1000L, formattedDate = "10/01/2025 08:00", formattedTime = "08:00", observation = "", classification = PressureClassification.NORMAL),
            Measurement(id = "2", systolic = 140, diastolic = 90, heartRate = 80, measuredAt = "1970-01-01T00:00:02.000Z", measuredAtEpoch = 2000L, formattedDate = "11/01/2025 08:00", formattedTime = "08:00", observation = "", classification = PressureClassification.HIPERTENSAO_ESTAGIO_1)
        )

        val stats = HealthCalculator.calculateStatistics(measurements)
        assertEquals(2, stats.totalCount)
        assertEquals(130.0, stats.avgSystolic!!, 0.0)
        assertEquals(85.0, stats.avgDiastolic!!, 0.0)
        assertEquals(75.0, stats.avgHeartRate!!, 0.0)
        assertEquals(120, stats.minSystolic)
        assertEquals(140, stats.maxSystolic)
        assertEquals(80, stats.minDiastolic)
        assertEquals(90, stats.maxDiastolic)
        assertEquals(70, stats.minHeartRate)
        assertEquals(80, stats.maxHeartRate)
    }

    @Test
    fun testPwaJsonBackupCompatibility() {
        val samplePwaJson = """
        {
          "version": "1.0",
          "exportDate": "2025-01-15T12:00:00.000Z",
          "userProfile": {
            "fullName": "Maria Silva",
            "sex": "Feminino",
            "birthDate": "1980-05-20",
            "weight": 68.5,
            "height": 1.65
          },
          "records": [
            {
              "id": "rec_001",
              "systolic": 128,
              "diastolic": 82,
              "heartRate": 72,
              "timestamp": "2025-01-15T08:30:00.000Z",
              "observation": "Após caminhada matinal"
            },
            {
              "id": "rec_002",
              "systolic": "135",
              "diastolic": "88",
              "heartRate": "76",
              "timestamp": "2025-01-14T19:00:00.000Z",
              "observation": ""
            }
          ]
        }
        """.trimIndent()

        val result = JsonBackupManager.parseBackupJson(samplePwaJson)
        assertTrue(result.isSuccess)
        assertEquals(2, result.importedCount)
        assertNotNull(result.userProfile)
        assertEquals("Maria Silva", result.userProfile?.fullName)
        assertEquals(68.5, result.userProfile?.weight ?: 0.0, 0.01)
        assertEquals(1.65, result.userProfile?.height ?: 0.0, 0.01)
        assertEquals(2, result.measurementEntities.size)

        val first = result.measurementEntities.first { it.id == "rec_001" }
        assertEquals(128, first.systolic)
        assertEquals(82, first.diastolic)
        assertEquals(72, first.heartRate)
        assertEquals("Após caminhada matinal", first.observation)
    }

    @Test
    fun testReminderTimeFormatting() {
        val reminder1 = com.example.domain.model.Reminder(
            id = 1L,
            hour = 8,
            minute = 5,
            label = "Manhã",
            isEnabled = true
        )
        assertEquals("08:05", reminder1.formattedTime)

        val reminder2 = com.example.domain.model.Reminder(
            id = 2L,
            hour = 20,
            minute = 0,
            label = "Noite",
            isEnabled = false
        )
        assertEquals("20:00", reminder2.formattedTime)
    }

    @Test
    fun testUserProfileInitials() {
        val profile = com.example.domain.model.UserProfile(
            id = 1L,
            fullName = "João Carlos Santos",
            sex = "Masculino",
            birthDate = "1975-08-12",
            age = 49,
            weight = 80.0,
            height = 1.78,
            bmi = 25.2,
            bmiClassification = BmiClassification.SOBREPESO,
            avatarColorHex = "#6750A4"
        )
        assertEquals("JC", profile.initials)
    }
}
