package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.reminder.ReminderScheduler
import com.example.data.repository.BloodPressureRepository
import com.example.domain.backup.ImportResult
import com.example.domain.backup.JsonBackupManager
import com.example.domain.backup.ReportExporter
import com.example.domain.model.DateFilter
import com.example.domain.model.Measurement
import com.example.domain.model.MeasurementStats
import com.example.domain.model.Reminder
import com.example.domain.model.SortOrder
import com.example.domain.model.UserProfile
import com.example.domain.rules.HealthCalculator
import com.example.domain.utils.ImageHelper
import android.net.Uri
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.OutputStream

enum class ScreenTab(val title: String) {
    DASHBOARD("Início"),
    NEW_MEASUREMENT("Nova Aferição"),
    HISTORY("Histórico"),
    CHARTS("Gráficos"),
    PROFILE("Perfil")
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BloodPressureRepository
    init {
        val db = AppDatabase.getInstance(application)
        repository = BloodPressureRepository(db.profileDao(), db.measurementDao(), db.reminderDao())
        ReminderScheduler.createNotificationChannel(application)
    }

    // Current Tab
    private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // All profiles list
    val allProfiles: StateFlow<List<UserProfile>> = repository.allProfilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected profile ID
    private val _selectedProfileId = MutableStateFlow<Long?>(null)
    val selectedProfileId: StateFlow<Long?> = _selectedProfileId.asStateFlow()

    // Active profile resolved from selectedProfileId or first in list
    val activeProfile: StateFlow<UserProfile?> = combine(allProfiles, _selectedProfileId) { profiles, selectedId ->
        if (profiles.isEmpty()) return@combine null
        val found = if (selectedId != null) profiles.find { it.id == selectedId } else null
        found ?: profiles.find { it.isDefault } ?: profiles.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Legacy profileState alias
    val profileState: StateFlow<UserProfile?> = activeProfile

    // Active Profile's measurements
    val allMeasurements: StateFlow<List<Measurement>> = activeProfile.flatMapLatest { profile ->
        if (profile != null) {
            repository.getMeasurementsForProfileFlow(profile.id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Recent measurements for active profile
    val recentMeasurements: StateFlow<List<Measurement>> = activeProfile.flatMapLatest { profile ->
        if (profile != null) {
            repository.getRecentMeasurementsForProfileFlow(profile.id, 5)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter for History & Charts
    private val _dateFilter = MutableStateFlow(DateFilter())
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    // Filtered measurements for active profile
    val filteredMeasurements: StateFlow<List<Measurement>> = combine(
        allMeasurements,
        _dateFilter
    ) { list, filter ->
        HealthCalculator.filterMeasurements(list, filter)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Overall stats (from active profile's measurements)
    val overallStats: StateFlow<MeasurementStats> = allMeasurements.map { list ->
        HealthCalculator.calculateStatistics(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HealthCalculator.calculateStatistics(emptyList()))

    // Filtered stats (for charts / export)
    val filteredStats: StateFlow<MeasurementStats> = filteredMeasurements.map { list ->
        HealthCalculator.calculateStatistics(list)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HealthCalculator.calculateStatistics(emptyList()))

    // Reminders state
    val allReminders: StateFlow<List<Reminder>> = repository.allRemindersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Measurement being edited
    private val _editingMeasurement = MutableStateFlow<Measurement?>(null)
    val editingMeasurement: StateFlow<Measurement?> = _editingMeasurement.asStateFlow()

    // Transient UI events / feedback
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    fun selectTab(tab: ScreenTab) {
        if (tab != ScreenTab.NEW_MEASUREMENT && _editingMeasurement.value != null) {
            _editingMeasurement.value = null
        }
        _currentTab.value = tab
    }

    fun selectProfile(profileId: Long) {
        _selectedProfileId.value = profileId
        val target = allProfiles.value.find { it.id == profileId }
        if (target != null) {
            viewModelScope.launch {
                _userMessage.emit("Perfil alterado para ${target.fullName}")
            }
        }
    }

    fun startEditMeasurement(measurement: Measurement) {
        _editingMeasurement.value = measurement
        _currentTab.value = ScreenTab.NEW_MEASUREMENT
    }

    fun cancelEditMeasurement() {
        val wasEditing = _editingMeasurement.value != null
        _editingMeasurement.value = null
        if (wasEditing) {
            _currentTab.value = ScreenTab.HISTORY
        } else {
            _currentTab.value = ScreenTab.DASHBOARD
        }
    }

    fun saveMeasurement(
        id: String?,
        systolic: Int,
        diastolic: Int,
        heartRate: Int,
        measuredAtEpoch: Long,
        observation: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val currentProf = activeProfile.value
            if (currentProf == null) {
                _userMessage.emit("Crie ou selecione um perfil antes de registrar uma aferição.")
                _currentTab.value = ScreenTab.PROFILE
                return@launch
            }

            if (diastolic >= systolic) {
                _userMessage.emit("A pressão diastólica deve ser menor que a sistólica.")
                return@launch
            }

            repository.saveMeasurement(
                id = id,
                profileId = currentProf.id,
                systolic = systolic,
                diastolic = diastolic,
                heartRate = heartRate,
                measuredAtEpoch = measuredAtEpoch,
                observation = observation
            )

            val isEdit = id != null
            _editingMeasurement.value = null
            _userMessage.emit(if (isEdit) "Aferição atualizada com sucesso!" else "Aferição salva com sucesso para ${currentProf.fullName}!")
            onSuccess()
            _currentTab.value = if (isEdit) ScreenTab.HISTORY else ScreenTab.DASHBOARD
        }
    }

    fun deleteMeasurement(id: String) {
        viewModelScope.launch {
            repository.deleteMeasurement(id)
            _userMessage.emit("Aferição excluída com sucesso.")
        }
    }

    fun saveProfile(
        id: Long? = null,
        fullName: String,
        sex: String,
        birthDate: String?,
        weight: Double,
        height: Double,
        avatarColorHex: String = "#6750A4",
        photoUri: String? = null,
        isDefault: Boolean = false,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            if (fullName.isBlank()) {
                _userMessage.emit("Informe o nome completo do perfil.")
                return@launch
            }
            if (weight <= 0 || weight > 500) {
                _userMessage.emit("Informe um peso válido entre 1 e 500 kg.")
                return@launch
            }
            if (height < 0.3 || height > 2.7) {
                _userMessage.emit("Informe uma altura válida entre 0.30 e 2.70 m.")
                return@launch
            }

            val isUpdate = id != null && id > 0
            val targetId = if (isUpdate) id else null
            var finalPhotoUri = photoUri

            // If photoUri is a content URI (from gallery), copy it to internal storage for persistence
            if (photoUri != null && photoUri.startsWith("content://")) {
                // Get old photo URI to delete it later if update
                val oldPhotoUri = if (targetId != null) {
                    allProfiles.value.find { it.id == targetId }?.photoUri
                } else null

                val internalUri = ImageHelper.saveImageToInternalStorage(
                    context = getApplication(),
                    sourceUri = Uri.parse(photoUri),
                    profileId = targetId ?: System.currentTimeMillis()
                )
                
                if (internalUri != null) {
                    finalPhotoUri = internalUri
                    // Delete ONLY the old photo of this specific profile
                    if (oldPhotoUri != null && oldPhotoUri != finalPhotoUri) {
                        ImageHelper.deleteSpecificPhoto(oldPhotoUri)
                    }
                }
            }

            if (isUpdate && targetId != null) {
                repository.updateProfile(
                    id = targetId,
                    fullName = fullName,
                    sex = sex,
                    birthDate = birthDate,
                    weight = weight,
                    height = height,
                    avatarColorHex = avatarColorHex,
                    photoUri = finalPhotoUri,
                    isDefault = isDefault
                )
                _selectedProfileId.value = targetId
                _userMessage.emit("Perfil '$fullName' atualizado com sucesso!")
            } else {
                val newId = repository.createProfile(
                    fullName = fullName,
                    sex = sex,
                    birthDate = birthDate,
                    weight = weight,
                    height = height,
                    avatarColorHex = avatarColorHex,
                    photoUri = finalPhotoUri,
                    isDefault = allProfiles.value.isEmpty() || isDefault
                )
                _selectedProfileId.value = newId
                _userMessage.emit("Novo perfil '$fullName' criado com sucesso!")
            }

            onSuccess()
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            val list = allProfiles.value
            if (list.size <= 1) {
                _userMessage.emit("Não é possível excluir o único perfil existente.")
                return@launch
            }
            val target = list.find { it.id == id }
            
            // Delete photo if exists
            target?.photoUri?.let { uriString ->
                try {
                    val file = File(Uri.parse(uriString).path ?: "")
                    if (file.exists()) file.delete()
                } catch (_: Exception) {}
            }

            repository.deleteProfile(id)
            val remaining = list.filter { it.id != id }
            _selectedProfileId.value = remaining.firstOrNull()?.id
            _userMessage.emit("Perfil '${target?.fullName ?: ""}' e seus registros foram excluídos.")
        }
    }

    // Reminders Management
    fun saveReminder(
        id: Long = 0L,
        hour: Int,
        minute: Int,
        label: String,
        category: com.example.domain.model.ReminderCategory = com.example.domain.model.ReminderCategory.AFERICAO,
        isEnabled: Boolean = true,
        soundUri: String? = null
    ) {
        viewModelScope.launch {
            val savedEntity = repository.saveReminder(
                id = id,
                hour = hour,
                minute = minute,
                label = label.ifBlank { if (category == com.example.domain.model.ReminderCategory.MEDICAMENTO) "Remédio" else "Aferição de Rotina" },
                category = category,
                isEnabled = isEnabled,
                profileId = activeProfile.value?.id,
                soundUri = soundUri
            )
            val app = getApplication<Application>()
            ReminderScheduler.scheduleReminder(app, savedEntity)
            val formattedTime = String.format("%02d:%02d", hour, minute)
            _userMessage.emit("Lembrete para as $formattedTime salvo com sucesso!")
        }
    }

    fun toggleReminder(id: Long, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, isEnabled)
            val reminder = allReminders.value.find { it.id == id }
            val app = getApplication<Application>()
            if (reminder != null) {
                val entity = com.example.data.entity.ReminderEntity(
                    id = id,
                    hour = reminder.hour,
                    minute = reminder.minute,
                    label = reminder.label,
                    category = reminder.category.name,
                    daysOfWeek = reminder.daysOfWeek,
                    isEnabled = isEnabled,
                    profileId = reminder.profileId,
                    soundUri = reminder.soundUri
                )
                ReminderScheduler.scheduleReminder(app, entity)
                _userMessage.emit(if (isEnabled) "Lembrete das ${reminder.formattedTime} ativado." else "Lembrete das ${reminder.formattedTime} desativado.")
            }
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            val reminder = allReminders.value.find { it.id == id }
            val app = getApplication<Application>()
            ReminderScheduler.cancelReminder(app, id)
            repository.deleteReminder(id)
            _userMessage.emit("Lembrete ${reminder?.formattedTime ?: ""} excluído.")
        }
    }

    fun sendTestReminder() {
        val app = getApplication<Application>()
        ReminderScheduler.sendTestNotification(app)
        viewModelScope.launch {
            _userMessage.emit("Notificação de teste enviada! Verifique sua barra de status.")
        }
    }

    fun updateDateFilter(
        startDate: Long?,
        endDate: Long?,
        sortOrder: SortOrder
    ) {
        _dateFilter.value = DateFilter(
            startDate = startDate,
            endDate = endDate,
            sortOrder = sortOrder
        )
    }

    fun clearDateFilter() {
        _dateFilter.value = DateFilter()
    }

    suspend fun importJsonData(jsonString: String): ImportResult {
        val result = JsonBackupManager.parseBackupJson(jsonString, getApplication())
        if (result.isSuccess) {
            if (result.userProfiles.size > 1) {
                // Multi-profile import
                repository.replaceAllDataMulti(
                    profiles = result.userProfiles,
                    measurements = result.measurementEntities
                )
                _userMessage.emit("Backup importado: ${result.userProfiles.size} perfis e ${result.importedCount} aferições carregadas.")
            } else {
                // Single profile legacy logic
                val currentProf = activeProfile.value
                if (currentProf != null) {
                    repository.replaceProfileData(
                        profile = currentProf,
                        measurements = result.measurementEntities.map { it.copy(profileId = currentProf.id) }
                    )
                    _userMessage.emit("Backup importado: ${result.importedCount} aferições carregadas para ${currentProf.fullName}.")
                } else {
                    repository.replaceAllData(
                        newProfile = result.userProfile,
                        newMeasurements = result.measurementEntities
                    )
                    _userMessage.emit("Backup importado com sucesso: ${result.importedCount} aferições carregadas.")
                }
            }
        }
        return result
    }

    fun generateBackupJson(): String {
        return JsonBackupManager.createBackupJson(
            activeProfile = activeProfile.value,
            allProfiles = allProfiles.value,
            measurements = allMeasurements.value, // This actually needs all measurements from all profiles for a full backup
            context = getApplication()
        )
    }

    fun exportPdfToStream(outputStream: OutputStream) {
        val measurementsToExport = if (filteredMeasurements.value.isNotEmpty()) filteredMeasurements.value else allMeasurements.value
        val statsToExport = if (filteredMeasurements.value.isNotEmpty()) filteredStats.value else overallStats.value
        ReportExporter.writePdfReport(
            outputStream = outputStream,
            profile = activeProfile.value,
            measurements = measurementsToExport,
            stats = statsToExport,
            context = getApplication(),
            startDate = _dateFilter.value.startDate,
            endDate = _dateFilter.value.endDate
        )
    }

    fun generateCsvReport(): String {
        val measurementsToExport = if (filteredMeasurements.value.isNotEmpty()) filteredMeasurements.value else allMeasurements.value
        val statsToExport = if (filteredMeasurements.value.isNotEmpty()) filteredStats.value else overallStats.value
        return ReportExporter.generateCsv(
            profile = activeProfile.value,
            measurements = measurementsToExport,
            stats = statsToExport
        )
    }
}

