package com.example.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import coil.compose.rememberAsyncImagePainter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.UserProfile
import com.example.domain.rules.HealthCalculator
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val AVATAR_COLORS = listOf(
    "#6750A4", // Purple
    "#006874", // Teal
    "#0061A4", // Blue
    "#9C4146", // Rose Red
    "#825500", // Amber Orange
    "#2E6B27", // Forest Green
    "#625B71"  // Slate
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditDialog(
    profileToEdit: UserProfile? = null,
    onSaveProfile: (
        id: Long?,
        fullName: String,
        sex: String,
        birthDate: String?,
        weight: Double,
        height: Double,
        avatarColorHex: String,
        photoUri: String?
    ) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isNew = profileToEdit == null

    var fullName by remember { mutableStateOf(profileToEdit?.fullName ?: "") }
    var sex by remember { mutableStateOf(profileToEdit?.sex ?: "Prefiro não informar") }
    var birthDateStr by remember { mutableStateOf(profileToEdit?.birthDate ?: "") }
    var weightInput by remember { mutableStateOf(profileToEdit?.weight?.let { if (it > 0) it.toString() else "" } ?: "70.0") }
    var heightInput by remember { mutableStateOf(profileToEdit?.height?.let { if (it > 0) it.toString() else "" } ?: "1.70") }
    var selectedColorHex by remember { mutableStateOf(profileToEdit?.avatarColorHex ?: AVATAR_COLORS.first()) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(profileToEdit?.photoUri?.let { Uri.parse(it) }) }

    var sexDropdownExpanded by remember { mutableStateOf(false) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            showPhotoOptions = false
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        // Note: TakePicturePreview returns a small bitmap. For a full resolution photo,
        // we'd need to provide a URI, but for an avatar this is often sufficient or
        // we can save it to a file. For simplicity in this edit, we'll focus on gallery
        // or small thumbnail. Actually, saving to a local file is better.
        // Let's stick to Gallery + clearing option for now as it's most robust without extra file handling logic.
    }

    val sexOptions = listOf("Masculino", "Feminino", "Outro", "Prefiro não informar")

    val parsedWeight = weightInput.replace(",", ".").toDoubleOrNull() ?: 0.0
    val parsedHeight = heightInput.replace(",", ".").toDoubleOrNull() ?: 0.0

    val calculatedBmi by remember(parsedWeight, parsedHeight) {
        derivedStateOf { HealthCalculator.calculateBmi(parsedWeight, parsedHeight) }
    }
    val calculatedAge by remember(birthDateStr) {
        derivedStateOf { HealthCalculator.calculateAge(birthDateStr) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 20.dp)
                .testTag("profile_edit_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    try {
                                        Color(android.graphics.Color.parseColor(selectedColorHex))
                                    } catch (_: Exception) {
                                        SleekPrimary
                                    }
                                )
                                .clickable { showPhotoOptions = !showPhotoOptions },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedPhotoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedPhotoUri),
                                    contentDescription = "Foto do Perfil",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = fullName.trim().take(2).uppercase().ifBlank { "P" },
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                )
                            }
                            
                            // Edit overlay icon
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp).padding(bottom = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (isNew) "Novo Perfil" else "Editar Perfil",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Toque na foto para alterar",
                                style = MaterialTheme.typography.bodySmall,
                                color = SleekPrimary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                    }
                }

                AnimatedVisibility(visible = showPhotoOptions) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .background(SleekSurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Galeria")
                            }
                            if (selectedPhotoUri != null) {
                                TextButton(onClick = { 
                                    selectedPhotoUri = null
                                    showPhotoOptions = false
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Remover", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Avatar Color Selection
                Text(
                    text = "COR DO PERFIL",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AVATAR_COLORS.forEach { colorHex ->
                        val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)
                        val color = try {
                            Color(android.graphics.Color.parseColor(colorHex))
                        } catch (_: Exception) {
                            SleekPrimary
                        }
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Nome Completo / Identificação *") },
                    placeholder = { Text("Ex: Maria da Silva, Pai, João...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_profile_fullname_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SleekOutline,
                        focusedBorderColor = SleekPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sex Selector
                ExposedDropdownMenuBox(
                    expanded = sexDropdownExpanded,
                    onExpandedChange = { sexDropdownExpanded = !sexDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = sex,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sexo Biológico") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SleekOutline,
                            focusedBorderColor = SleekPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = sexDropdownExpanded,
                        onDismissRequest = { sexDropdownExpanded = false }
                    ) {
                        sexOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    sex = option
                                    sexDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Birth Date
                OutlinedTextField(
                    value = birthDateStr,
                    onValueChange = { birthDateStr = it },
                    label = { Text("Data de Nascimento (yyyy-MM-dd)") },
                    placeholder = { Text("Ex: 1980-05-15") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePickerDialog = true }) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Selecionar Data")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_profile_birthdate_input"),
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = SleekOutline,
                        focusedBorderColor = SleekPrimary
                    )
                )

                if (calculatedAge != null) {
                    Text(
                        text = "Idade calculada: $calculatedAge anos",
                        style = MaterialTheme.typography.bodySmall,
                        color = SleekPrimary,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Weight & Height
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Peso (kg) *") },
                        placeholder = { Text("70.0") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_profile_weight_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SleekOutline,
                            focusedBorderColor = SleekPrimary
                        )
                    )
                    OutlinedTextField(
                        value = heightInput,
                        onValueChange = { heightInput = it },
                        label = { Text("Altura (m) *") },
                        placeholder = { Text("1.75") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_profile_height_input"),
                        shape = RoundedCornerShape(14.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = SleekOutline,
                            focusedBorderColor = SleekPrimary
                        )
                    )
                }

                // BMI Preview Card
                if (calculatedBmi != null) {
                    val classification = HealthCalculator.classifyBmi(calculatedBmi!!)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.outlinedCardColors(
                            containerColor = SleekSurfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "IMC Calculado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$calculatedBmi kg/m²",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = classification.color.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, classification.color.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = classification.title,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = classification.color
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (fullName.isNotBlank() && parsedWeight > 0 && parsedHeight > 0) {
                                onSaveProfile(
                                    profileToEdit?.id,
                                    fullName,
                                    sex,
                                    birthDateStr.ifBlank { null },
                                    parsedWeight,
                                    parsedHeight,
                                    selectedColorHex,
                                    selectedPhotoUri?.toString()
                                )
                                onDismiss()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                        modifier = Modifier.testTag("dialog_save_profile_button")
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNew) "Criar Perfil" else "Salvar Alterações", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedEpoch = datePickerState.selectedDateMillis
                        if (selectedEpoch != null) {
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            birthDateStr = sdf.format(Date(selectedEpoch))
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
