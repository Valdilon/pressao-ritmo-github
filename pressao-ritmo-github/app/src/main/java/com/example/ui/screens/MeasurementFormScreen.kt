package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Measurement
import com.example.domain.model.PressureClassification
import com.example.domain.model.UserProfile
import com.example.domain.rules.HealthCalculator
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.WarningBanner
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekTertiary
import java.util.Calendar

@Composable
fun MeasurementFormScreen(
    editingMeasurement: Measurement?,
    profile: UserProfile?,
    onSave: (id: String?, systolic: Int, diastolic: Int, heartRate: Int, measuredAtEpoch: Long, observation: String) -> Unit,
    onCancel: () -> Unit,
    onNavigateToProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var systolicText by remember(editingMeasurement) {
        mutableStateOf(editingMeasurement?.systolic?.toString() ?: "")
    }
    var diastolicText by remember(editingMeasurement) {
        mutableStateOf(editingMeasurement?.diastolic?.toString() ?: "")
    }
    var heartRateText by remember(editingMeasurement) {
        mutableStateOf(editingMeasurement?.heartRate?.toString() ?: "")
    }
    var observationText by remember(editingMeasurement) {
        mutableStateOf(editingMeasurement?.observation ?: "")
    }
    var measuredAtEpoch by remember(editingMeasurement) {
        mutableLongStateOf(editingMeasurement?.measuredAtEpoch ?: System.currentTimeMillis())
    }

    val systolicInt = systolicText.toIntOrNull()
    val diastolicInt = diastolicText.toIntOrNull()
    val heartRateInt = heartRateText.toIntOrNull()

    // Calculated classification in real-time
    val liveClassification = if (systolicInt != null && diastolicInt != null) {
        HealthCalculator.classifyPressure(systolicInt, diastolicInt)
    } else null

    // Validation checks
    val isDiastolicInvalid = systolicInt != null && diastolicInt != null && diastolicInt >= systolicInt
    val isSystolicOutOfRange = systolicInt != null && (systolicInt < 50 || systolicInt > 300)
    val isDiastolicOutOfRange = diastolicInt != null && (diastolicInt < 30 || diastolicInt > 200)
    val isHeartRateOutOfRange = heartRateInt != null && (heartRateInt < 20 || heartRateInt > 250)

    val isFormValid = profile != null &&
            systolicInt != null && !isSystolicOutOfRange &&
            diastolicInt != null && !isDiastolicOutOfRange &&
            !isDiastolicInvalid &&
            heartRateInt != null && !isHeartRateOutOfRange

    val calendar = remember(measuredAtEpoch) {
        Calendar.getInstance().apply { timeInMillis = measuredAtEpoch }
    }

    fun showDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = measuredAtEpoch
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                measuredAtEpoch = newCal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker() {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newCal = Calendar.getInstance().apply {
                    timeInMillis = measuredAtEpoch
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                measuredAtEpoch = newCal.timeInMillis
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (editingMeasurement != null) "Editar Aferição" else "Nova Aferição",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.3).sp
                )
                Text(
                    text = "Preencha os dados da sua medição",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (editingMeasurement != null) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors()
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancelar", fontSize = 12.sp)
                }
            }
        }

        // Warning if no profile
        if (profile == null) {
            WarningBanner(
                title = "Perfil obrigatório",
                message = "É necessário cadastrar seu perfil antes de registrar medições para acompanhamento personalizado.",
                buttonText = "Cadastrar Perfil",
                onButtonClick = onNavigateToProfile,
                icon = Icons.Default.Person
            )
        }

        // Live Classification Card Preview
        if (liveClassification != null && !isDiastolicInvalid) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = liveClassification.color.copy(alpha = 0.08f)
                ),
                border = BorderStroke(1.dp, liveClassification.color.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Classificação da Pressão",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ClassificationBadge(classification = liveClassification)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = liveClassification.description,
                        fontSize = 12.sp,
                        color = liveClassification.color,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (liveClassification.isEmergencyAlert) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFFFD8E4),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, Color(0xFFB71C1C).copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(8.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFB71C1C),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = PressureClassification.STAGE_3_ALERT,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF31111D)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Diastolic >= Systolic Error Alert
        AnimatedVisibility(visible = isDiastolicInvalid) {
            Surface(
                color = Color(0xFFFFD8E4),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = PressureClassification.DIASTOLIC_ERROR_MSG,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Form Fields Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Systolic Input
                OutlinedTextField(
                    value = systolicText,
                    onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) systolicText = it },
                    label = { Text("Pressão Sistólica (Máxima)") },
                    placeholder = { Text("Ex: 120") },
                    suffix = { Text("mmHg", fontWeight = FontWeight.Bold, color = SleekPrimary) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = SleekPrimary)
                    },
                    isError = isSystolicOutOfRange,
                    supportingText = {
                        if (isSystolicOutOfRange) {
                            Text("A pressão sistólica deve estar entre 50 e 300 mmHg")
                        } else {
                            Text("Faixa normal de medição: 50 a 300 mmHg")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_systolic")
                )

                // Diastolic Input
                OutlinedTextField(
                    value = diastolicText,
                    onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) diastolicText = it },
                    label = { Text("Pressão Diastólica (Mínima)") },
                    placeholder = { Text("Ex: 80") },
                    suffix = { Text("mmHg", fontWeight = FontWeight.Bold, color = SleekSecondary) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = SleekSecondary)
                    },
                    isError = isDiastolicOutOfRange || isDiastolicInvalid,
                    supportingText = {
                        if (isDiastolicOutOfRange) {
                            Text("A pressão diastólica deve estar entre 30 e 200 mmHg")
                        } else if (isDiastolicInvalid) {
                            Text(PressureClassification.DIASTOLIC_ERROR_MSG)
                        } else {
                            Text("Faixa normal de medição: 30 a 200 mmHg")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_diastolic")
                )

                // Heart Rate Input
                OutlinedTextField(
                    value = heartRateText,
                    onValueChange = { if (it.length <= 3 && it.all { char -> char.isDigit() }) heartRateText = it },
                    label = { Text("Frequência Cardíaca (Pulso)") },
                    placeholder = { Text("Ex: 75") },
                    suffix = { Text("BPM", fontWeight = FontWeight.Bold, color = SleekTertiary) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = SleekTertiary)
                    },
                    isError = isHeartRateOutOfRange,
                    supportingText = {
                        if (isHeartRateOutOfRange) {
                            Text("A frequência cardíaca deve estar entre 20 e 250 BPM")
                        } else {
                            Text("Batimentos por minuto (20 a 250 BPM)")
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_heart_rate")
                )

                // Date and Time selection row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedCard(
                        onClick = { showDatePicker() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("picker_date"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Data", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = HealthCalculator.formatEpochToDateOnly(measuredAtEpoch),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    OutlinedCard(
                        onClick = { showTimePicker() },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("picker_time"),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Hora", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = HealthCalculator.formatEpochToTimeOnly(measuredAtEpoch),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Observation text field
                OutlinedTextField(
                    value = observationText,
                    onValueChange = { if (it.length <= 500) observationText = it },
                    label = { Text("Observações (opcional)") },
                    placeholder = { Text("Ex: Após café da manhã, em repouso, braço esquerdo...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Notes, contentDescription = null)
                    },
                    supportingText = {
                        Text("${observationText.length} / 500 caracteres")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_observation")
                )
            }
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (isFormValid && systolicInt != null && diastolicInt != null && heartRateInt != null) {
                        onSave(
                            editingMeasurement?.id,
                            systolicInt,
                            diastolicInt,
                            heartRateInt,
                            measuredAtEpoch,
                            observationText
                        )
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekPrimary,
                    contentColor = Color.White
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_measurement_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (editingMeasurement != null) "Atualizar Aferição" else "Salvar Aferição",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (editingMeasurement != null) {
                OutlinedButton(
                    onClick = onCancel,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("cancel_edit_button")
                ) {
                    Text("Cancelar Edição", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
