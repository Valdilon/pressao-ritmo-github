package com.example.data.repository

import com.example.data.dao.MeasurementDao
import com.example.data.dao.ProfileDao
import com.example.data.dao.ReminderDao
import com.example.data.entity.MeasurementEntity
import com.example.data.entity.ProfileEntity
import com.example.data.entity.ReminderEntity
import com.example.domain.model.Measurement
import com.example.domain.model.Reminder
import com.example.domain.model.ReminderCategory
import com.example.domain.model.UserProfile
import com.example.domain.rules.HealthCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class BloodPressureRepository(
    private val profileDao: ProfileDao,
    private val measurementDao: MeasurementDao,
    private val reminderDao: ReminderDao
) {
    // Multi-profile flows
    val allProfilesFlow: Flow<List<UserProfile>> = profileDao.getAllProfiles().map { list ->
        list.map { it.toDomain() }
    }

    val defaultOrFirstProfileFlow: Flow<UserProfile?> = profileDao.getDefaultOrFirstProfile().map {
        it?.toDomain()
    }

    fun getProfileByIdFlow(id: Long): Flow<UserProfile?> = profileDao.getProfileById(id).map {
        it?.toDomain()
    }

    suspend fun getProfileByIdDirect(id: Long): UserProfile? = profileDao.getProfileByIdDirect(id)?.toDomain()

    suspend fun getAllProfilesDirect(): List<UserProfile> = profileDao.getAllProfilesDirect().map { it.toDomain() }

    suspend fun createProfile(
        fullName: String,
        sex: String,
        birthDate: String?,
        weight: Double,
        height: Double,
        avatarColorHex: String = "#6750A4",
        photoUri: String? = null,
        isDefault: Boolean = false
    ): Long {
        val calculatedBmi = HealthCalculator.calculateBmi(weight, height)
        val calculatedAge = HealthCalculator.calculateAge(birthDate)
        val entity = ProfileEntity(
            id = 0L, // auto-generated
            fullName = fullName.trim(),
            sex = sex.trim(),
            birthDate = birthDate?.trim()?.ifBlank { null },
            age = calculatedAge,
            weight = weight,
            height = height,
            bmi = calculatedBmi,
            avatarColorHex = avatarColorHex,
            photoUri = photoUri,
            isDefault = isDefault,
            createdAt = System.currentTimeMillis()
        )
        return profileDao.insert(entity)
    }

    suspend fun updateProfile(
        id: Long,
        fullName: String,
        sex: String,
        birthDate: String?,
        weight: Double,
        height: Double,
        avatarColorHex: String = "#6750A4",
        photoUri: String? = null,
        isDefault: Boolean = false
    ) {
        val calculatedBmi = HealthCalculator.calculateBmi(weight, height)
        val calculatedAge = HealthCalculator.calculateAge(birthDate)
        val existing = profileDao.getProfileByIdDirect(id)
        val entity = ProfileEntity(
            id = id,
            fullName = fullName.trim(),
            sex = sex.trim(),
            birthDate = birthDate?.trim()?.ifBlank { null },
            age = calculatedAge,
            weight = weight,
            height = height,
            bmi = calculatedBmi,
            avatarColorHex = avatarColorHex,
            photoUri = photoUri,
            isDefault = isDefault,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        profileDao.update(entity)
    }

    suspend fun deleteProfile(id: Long) {
        measurementDao.deleteByProfileId(id)
        profileDao.deleteById(id)
    }

    suspend fun setDefaultProfile(id: Long) {
        profileDao.setAsDefault(id)
    }

    // Profile-scoped measurements
    fun getMeasurementsForProfileFlow(profileId: Long): Flow<List<Measurement>> =
        measurementDao.getMeasurementsForProfile(profileId).map { list ->
            list.map { it.toDomain() }
        }

    fun getRecentMeasurementsForProfileFlow(profileId: Long, limit: Int = 5): Flow<List<Measurement>> =
        measurementDao.getRecentMeasurementsForProfile(profileId, limit).map { list ->
            list.map { it.toDomain() }
        }

    fun getMeasurementsBetweenForProfileFlow(profileId: Long, startEpoch: Long, endEpoch: Long): Flow<List<Measurement>> =
        measurementDao.getMeasurementsBetweenForProfile(profileId, startEpoch, endEpoch).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun getAllMeasurementsForProfileDirect(profileId: Long): List<Measurement> =
        measurementDao.getMeasurementsForProfileDirect(profileId).map { it.toDomain() }

    suspend fun getMeasurementById(id: String): Measurement? {
        return measurementDao.getMeasurementById(id)?.toDomain()
    }

    suspend fun saveMeasurement(
        id: String? = null,
        profileId: Long,
        systolic: Int,
        diastolic: Int,
        heartRate: Int,
        measuredAtEpoch: Long,
        observation: String
    ): String {
        val finalId = id?.ifBlank { null } ?: UUID.randomUUID().toString()
        val isoDate = HealthCalculator.formatEpochToIso(measuredAtEpoch)
        val nowIso = HealthCalculator.formatEpochToIso(System.currentTimeMillis())

        val entity = MeasurementEntity(
            id = finalId,
            profileId = profileId,
            systolic = systolic,
            diastolic = diastolic,
            heartRate = heartRate,
            measuredAt = isoDate,
            measuredAtEpoch = measuredAtEpoch,
            observation = observation.trim(),
            createdAt = if (id == null) nowIso else null,
            updatedAt = nowIso
        )
        measurementDao.insert(entity)
        return finalId
    }

    suspend fun deleteMeasurement(id: String) {
        measurementDao.deleteById(id)
    }

    // Reminders
    val allRemindersFlow: Flow<List<Reminder>> = reminderDao.getAllReminders().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun getAllRemindersDirect(): List<Reminder> = reminderDao.getAllRemindersDirect().map { it.toDomain() }

    suspend fun getEnabledRemindersDirect(): List<ReminderEntity> = reminderDao.getEnabledRemindersDirect()

    suspend fun saveReminder(
        id: Long = 0L,
        hour: Int,
        minute: Int,
        label: String,
        category: ReminderCategory = ReminderCategory.AFERICAO,
        daysOfWeek: String = "1,2,3,4,5,6,7",
        isEnabled: Boolean = true,
        profileId: Long? = null,
        soundUri: String? = null
    ): ReminderEntity {
        val entity = ReminderEntity(
            id = id,
            hour = hour,
            minute = minute,
            label = label.trim(),
            category = category.name,
            daysOfWeek = daysOfWeek,
            isEnabled = isEnabled,
            profileId = profileId,
            soundUri = soundUri,
            createdAt = System.currentTimeMillis()
        )
        val savedId = reminderDao.insert(entity)
        return entity.copy(id = if (id == 0L) savedId else id)
    }

    suspend fun toggleReminder(id: Long, isEnabled: Boolean) {
        reminderDao.setEnabled(id, isEnabled)
    }

    suspend fun deleteReminder(id: Long) {
        reminderDao.deleteById(id)
    }

    // Backup & Restore
    suspend fun replaceAllData(
        newProfile: UserProfile?,
        newMeasurements: List<MeasurementEntity>
    ) {
        if (newProfile != null) {
            val entity = ProfileEntity(
                id = newProfile.id.takeIf { it > 0 } ?: 1L,
                fullName = newProfile.fullName,
                sex = newProfile.sex,
                birthDate = newProfile.birthDate,
                age = newProfile.age,
                weight = newProfile.weight,
                height = newProfile.height,
                bmi = newProfile.bmi,
                avatarColorHex = newProfile.avatarColorHex,
                photoUri = newProfile.photoUri,
                isDefault = true
            )
            profileDao.insertOrUpdate(entity)
        }
        measurementDao.replaceAll(newMeasurements)
    }

    suspend fun replaceAllDataMulti(
        profiles: List<UserProfile>,
        measurements: List<MeasurementEntity>
    ) {
        // Clear all first
        profileDao.deleteAll()
        measurementDao.deleteAll()
        
        for (profile in profiles) {
            val entity = ProfileEntity(
                id = profile.id,
                fullName = profile.fullName,
                sex = profile.sex,
                birthDate = profile.birthDate,
                age = profile.age,
                weight = profile.weight,
                height = profile.height,
                bmi = profile.bmi,
                avatarColorHex = profile.avatarColorHex,
                photoUri = profile.photoUri,
                isDefault = profile.isDefault
            )
            profileDao.insertOrUpdate(entity)
        }
        measurementDao.replaceAll(measurements)
    }

    suspend fun replaceProfileData(
        profile: UserProfile,
        measurements: List<MeasurementEntity>
    ) {
        val entity = ProfileEntity(
            id = profile.id,
            fullName = profile.fullName,
            sex = profile.sex,
            birthDate = profile.birthDate,
            age = profile.age,
            weight = profile.weight,
            height = profile.height,
            bmi = profile.bmi,
            avatarColorHex = profile.avatarColorHex,
            photoUri = profile.photoUri,
            isDefault = profile.isDefault
        )
        profileDao.insertOrUpdate(entity)
        measurementDao.replaceProfileMeasurements(profile.id, measurements)
    }

    // Extension Mappers
    private fun ProfileEntity.toDomain(): UserProfile {
        val currentAge = HealthCalculator.calculateAge(birthDate) ?: age
        val currentBmi = HealthCalculator.calculateBmi(weight, height) ?: bmi
        val bmiClass = currentBmi?.let { HealthCalculator.classifyBmi(it) }
        return UserProfile(
            id = id,
            fullName = fullName,
            sex = sex,
            birthDate = birthDate,
            age = currentAge,
            weight = weight,
            height = height,
            bmi = currentBmi,
            bmiClassification = bmiClass,
            isDefault = isDefault,
            avatarColorHex = avatarColorHex.ifBlank { "#6750A4" },
            photoUri = photoUri
        )
    }

    private fun MeasurementEntity.toDomain(): Measurement {
        val epoch = if (measuredAtEpoch > 0) measuredAtEpoch else HealthCalculator.parseIsoToEpoch(measuredAt)
        return Measurement(
            id = id,
            profileId = profileId,
            systolic = systolic,
            diastolic = diastolic,
            heartRate = heartRate,
            measuredAt = measuredAt,
            measuredAtEpoch = epoch,
            formattedDate = HealthCalculator.formatEpochToDisplay(epoch),
            formattedTime = HealthCalculator.formatEpochToTimeOnly(epoch),
            observation = observation,
            classification = HealthCalculator.classifyPressure(systolic, diastolic),
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun ReminderEntity.toDomain(): Reminder {
        return Reminder(
            id = id,
            hour = hour,
            minute = minute,
            label = label,
            category = ReminderCategory.fromName(category),
            daysOfWeek = daysOfWeek,
            isEnabled = isEnabled,
            profileId = profileId,
            soundUri = soundUri
        )
    }
}

