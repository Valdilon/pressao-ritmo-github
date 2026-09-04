package com.example.domain.backup

import android.content.Context
import com.example.data.entity.MeasurementEntity
import com.example.domain.model.Measurement
import com.example.domain.model.UserProfile
import com.example.domain.rules.HealthCalculator
import com.example.domain.utils.ImageHelper
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class ImportResult(
    val isSuccess: Boolean,
    val importedCount: Int,
    val profileFound: Boolean,
    val userProfile: UserProfile?,
    val userProfiles: List<UserProfile> = emptyList(),
    val measurementEntities: List<MeasurementEntity>,
    val errorMessage: String? = null,
    val skippedInvalidCount: Int = 0
)

object JsonBackupManager {

    /**
     * Serializes profile and measurements into the standard PWA-compatible JSON backup format.
     */
    fun createBackupJson(
        activeProfile: UserProfile?,
        allProfiles: List<UserProfile>,
        measurements: List<Measurement>,
        context: Context? = null
    ): String {
        val root = JSONObject()
        root.put("version", "1.2")
        root.put("exportDate", HealthCalculator.formatEpochToIso(System.currentTimeMillis()))

        val profsArray = JSONArray()
        for (profile in allProfiles) {
            val profObj = JSONObject()
            profObj.put("id", profile.id)
            profObj.put("fullName", profile.fullName)
            profObj.put("sex", profile.sex)
            if (profile.birthDate != null) profObj.put("birthDate", profile.birthDate)
            if (profile.age != null) profObj.put("age", profile.age.toString())
            profObj.put("weight", profile.weight.toString())
            profObj.put("height", profile.height.toString())
            if (profile.bmi != null) profObj.put("bmi", profile.bmi)
            profObj.put("avatarColorHex", profile.avatarColorHex)
            profObj.put("isDefault", profile.id == activeProfile?.id)
            
            if (context != null && profile.photoUri != null) {
                val base64 = ImageHelper.getBase64FromInternalUri(context, profile.photoUri)
                if (base64 != null) profObj.put("photoBase64", base64)
            }
            profsArray.put(profObj)
        }
        root.put("profiles", profsArray)

        val measArray = JSONArray()
        for (m in measurements) {
            val mObj = JSONObject()
            mObj.put("id", m.id)
            mObj.put("systolic", m.systolic)
            mObj.put("diastolic", m.diastolic)
            mObj.put("heartRate", m.heartRate)
            mObj.put("timestamp", m.measuredAt)
            mObj.put("observation", m.observation)
            if (m.createdAt != null) mObj.put("createdAt", m.createdAt)
            if (m.updatedAt != null) mObj.put("updatedAt", m.updatedAt)
            measArray.put(mObj)
        }
        root.put("records", measArray)

        return root.toString(2)
    }

    /**
     * Parses and validates JSON backup string, converting string/number formats flexibly.
     */
    fun parseBackupJson(jsonString: String, context: Context? = null): ImportResult {
        return try {
            val root = JSONObject(jsonString)

            val measArray = root.optJSONArray("measurements") ?: root.optJSONArray("records")
            if (measArray == null) {
                return ImportResult(
                    isSuccess = false,
                    importedCount = 0,
                    profileFound = false,
                    userProfile = null,
                    measurementEntities = emptyList(),
                    errorMessage = "Arquivo inválido: o campo 'measurements' ou 'records' não foi encontrado."
                )
            }

            // Parse Profiles
            val parsedProfiles = mutableListOf<UserProfile>()
            val profsArray = root.optJSONArray("profiles")
            if (profsArray != null) {
                for (i in 0 until profsArray.length()) {
                    val pObj = profsArray.optJSONObject(i) ?: continue
                    parsedProfiles.add(parseProfileObject(pObj, context))
                }
            } else {
                // Fallback to legacy single userProfile
                val legacyProf = root.optJSONObject("profile") ?: root.optJSONObject("userProfile")
                if (legacyProf != null) {
                    parsedProfiles.add(parseProfileObject(legacyProf, context))
                }
            }

            val validEntities = mutableListOf<MeasurementEntity>()
            var skippedCount = 0

            for (i in 0 until measArray.length()) {
                val item = measArray.optJSONObject(i) ?: continue

                val id = if (item.has("id") && !item.isNull("id")) {
                    item.optString("id").trim().ifBlank { UUID.randomUUID().toString() }
                } else {
                    UUID.randomUUID().toString()
                }

                val profileId = item.optLong("profileId", 1L) // Support for profile linking

                val sysVal = item.optInt("systolic", -1)
                val systolic = if (sysVal == -1) item.optString("systolic").toIntOrNull() ?: -1 else sysVal

                val diaVal = item.optInt("diastolic", -1)
                val diastolic = if (diaVal == -1) item.optString("diastolic").toIntOrNull() ?: -1 else diaVal

                val hrVal = item.optInt("heartRate", -1)
                val heartRate = if (hrVal == -1) item.optString("heartRate").toIntOrNull() ?: -1 else hrVal

                val measuredAt = item.optString(
                    "measuredAt",
                    item.optString("timestamp", "")
                ).trim()
                val observation = item.optString("observation", "").take(500)

                // Validate boundaries
                if (systolic < 30 || systolic > 350 || diastolic < 20 || diastolic > 250 || heartRate < 10 || heartRate > 300) {
                    skippedCount++
                    continue
                }

                val epoch = HealthCalculator.parseIsoToEpoch(measuredAt)
                val finalIso = if (measuredAt.isNotBlank()) measuredAt else HealthCalculator.formatEpochToIso(epoch)

                validEntities.add(
                    MeasurementEntity(
                        id = id,
                        profileId = profileId,
                        systolic = systolic,
                        diastolic = diastolic,
                        heartRate = heartRate,
                        measuredAt = finalIso,
                        measuredAtEpoch = epoch,
                        observation = observation,
                        createdAt = item.optString("createdAt").ifBlank { null },
                        updatedAt = item.optString("updatedAt").ifBlank { null }
                    )
                )
            }

            ImportResult(
                isSuccess = true,
                importedCount = validEntities.size,
                profileFound = parsedProfiles.isNotEmpty(),
                userProfile = parsedProfiles.find { it.isDefault } ?: parsedProfiles.firstOrNull(),
                userProfiles = parsedProfiles,
                measurementEntities = validEntities,
                skippedInvalidCount = skippedCount
            )
        } catch (e: Exception) {
            ImportResult(
                isSuccess = false,
                importedCount = 0,
                profileFound = false,
                userProfile = null,
                measurementEntities = emptyList(),
                errorMessage = "Erro ao processar o arquivo JSON: ${e.localizedMessage}"
            )
        }
    }

    private fun parseProfileObject(profObj: JSONObject, context: Context?): UserProfile {
        val id = profObj.optLong("id", 1L)
        val fullName = profObj.optString("fullName", "Usuário").trim()
        val sex = profObj.optString("sex", "Prefiro não informar").trim()
        val birthDate = if (profObj.has("birthDate") && !profObj.isNull("birthDate")) {
            profObj.optString("birthDate").trim().ifBlank { null }
        } else null

        val weightStr = profObj.optString("weight", "70.0").replace(",", ".")
        val weight = weightStr.toDoubleOrNull() ?: 70.0

        val heightStr = profObj.optString("height", "1.70").replace(",", ".")
        val height = heightStr.toDoubleOrNull() ?: 1.70

        val calculatedAge = HealthCalculator.calculateAge(birthDate)
            ?: profObj.optString("age", "").toIntOrNull()
        val calculatedBmi = HealthCalculator.calculateBmi(weight, height)
            ?: profObj.optDouble("bmi", 0.0).takeIf { it > 0 }

        var importedPhotoUri: String? = null
        if (context != null && profObj.has("photoBase64")) {
            val base64 = profObj.optString("photoBase64")
            importedPhotoUri = ImageHelper.saveBase64ToInternalStorage(context, base64, id)
        }

        return UserProfile(
            id = id,
            fullName = fullName.ifBlank { "Usuário" },
            sex = sex.ifBlank { "Prefiro não informar" },
            birthDate = birthDate,
            age = calculatedAge,
            weight = weight,
            height = height,
            bmi = calculatedBmi,
            bmiClassification = calculatedBmi?.let { HealthCalculator.classifyBmi(it) },
            photoUri = importedPhotoUri,
            avatarColorHex = profObj.optString("avatarColorHex", "#6750A4"),
            isDefault = profObj.optBoolean("isDefault", false)
        )
    }
}
