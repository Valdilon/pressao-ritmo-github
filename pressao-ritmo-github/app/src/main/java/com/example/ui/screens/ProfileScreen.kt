package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wc
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserProfile
import com.example.domain.rules.HealthCalculator
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceVariant
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    profile: UserProfile?,
    allProfiles: List<UserProfile> = emptyList(),
    onSelectProfile: (Long) -> Unit = {},
    onAddNewProfile: () -> Unit = {},
    onSaveProfile: (fullName: String, sex: String, birthDate: String?, weight: Double, height: Double, photoUri: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var fullName by remember(profile) { mutableStateOf(profile?.fullName ?: "") }
    var sex by remember(profile) { mutableStateOf(profile?.sex ?: "Prefiro não informar") }
    var birthDateStr by remember(profile) { mutableStateOf(profile?.birthDate ?: "") }
    var weightText by remember(profile) { mutableStateOf(profile?.weight?.toString() ?: "") }
    var heightText by remember(profile) { mutableStateOf(profile?.height?.toString() ?: "") }

    val sexOptions = listOf("Feminino", "Masculino", "Outro", "Prefiro não informar")
    var sexDropdownExpanded by remember { mutableStateOf(false) }

    val weightDouble = weightText.replace(",", ".").toDoubleOrNull()
    val heightDouble = heightText.replace(",", ".").toDoubleOrNull()

    // Calculated fields
    val calculatedAge = HealthCalculator.calculateAge(birthDateStr.ifBlank { null }) ?: profile?.age
    val calculatedBmi = if (weightDouble != null && heightDouble != null && weightDouble > 0 && heightDouble > 0) {
        HealthCalculator.calculateBmi(weightDouble, heightDouble)
    } else null
    val bmiClassification = calculatedBmi?.let { HealthCalculator.classifyBmi(it) }

    val isWeightValid = weightDouble != null && weightDouble in 1.0..500.0
    val isHeightValid = heightDouble != null && heightDouble in 0.30..2.70
    val isNameValid = fullName.trim().isNotBlank() && fullName.length <= 120

    val isFormValid = isNameValid && isWeightValid && isHeightValid

    fun showBirthDatePicker() {
        val cal = Calendar.getInstance()
        if (birthDateStr.isNotBlank()) {
            try {
                val d = LocalDate.parse(birthDateStr)
                cal.set(d.year, d.monthValue - 1, d.dayOfMonth)
            } catch (_: Exception) {}
        }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val monthStr = (month + 1).toString().padStart(2, '0')
                val dayStr = dayOfMonth.toString().padStart(2, '0')
                birthDateStr = "$year-$monthStr-$dayStr"
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(SleekPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (profile?.photoUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(profile.photoUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (profile != null) profile.fullName else "Perfil do Usuário",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "Dados biométricos para cálculo de IMC e idade",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allProfiles.isNotEmpty()) {
                FilledTonalButton(
                    onClick = onAddNewProfile,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("profile_screen_add_profile_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Novo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Multi-Profile Switcher List Card if there are multiple profiles
        if (allProfiles.size > 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = SleekSurfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SELECIONAR PERFIL ATIVO",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${allProfiles.size} perfis",
                            style = MaterialTheme.typography.labelSmall,
                            color = SleekPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allProfiles.forEach { p ->
                            val isActive = p.id == profile?.id
                            val avatarColor = try {
                                Color(android.graphics.Color.parseColor(p.avatarColorHex))
                            } catch (_: Exception) {
                                SleekPrimary
                            }

                            Surface(
                                onClick = { onSelectProfile(p.id) },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isActive) SleekPrimary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    if (isActive) 2.dp else 1.dp,
                                    if (isActive) SleekPrimary else SleekOutline.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier
                                    .width(100.dp)
                                    .testTag("profile_pill_${p.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (isActive) Color.White.copy(alpha = 0.2f) else avatarColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (p.photoUri != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(p.photoUri),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Text(
                                                text = p.initials,
                                                color = if (isActive) Color.White else Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = p.fullName.split(" ").firstOrNull() ?: p.fullName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        
                        // Explicit "Add New" Pill in the list
                        Surface(
                            onClick = onAddNewProfile,
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.4f)),
                            modifier = Modifier.width(100.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = SleekPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Adicionar",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SleekPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Calculated BMI & Age Preview Card
        if (calculatedBmi != null && bmiClassification != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, bmiClassification.color.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ÍNDICE DE MASSA CORPORAL (IMC)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Text(
                            text = calculatedBmi.toString(),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = bmiClassification.color
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "kg/m²",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Surface(
                            color = bmiClassification.color.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = bmiClassification.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = bmiClassification.color,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (calculatedAge != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Idade calculada: $calculatedAge anos",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                // Full Name
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { if (it.length <= 120) fullName = it },
                    label = { Text("Nome Completo *") },
                    placeholder = { Text("Ex: Maria Silva") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = SleekPrimary) },
                    isError = fullName.isBlank() && profile != null,
                    supportingText = { Text("${fullName.length} / 120 caracteres") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    modifier = Modifier.fillMaxWidth().testTag("input_profile_name")
                )

                // Sex Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = sexDropdownExpanded,
                    onExpandedChange = { sexDropdownExpanded = !sexDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = sex,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sexo *") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Wc, contentDescription = null, tint = SleekPrimary) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("picker_profile_sex")
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

                // Birth Date
                OutlinedCard(
                    onClick = { showBirthDatePicker() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("picker_profile_birthdate"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Data de Nascimento (opcional)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (birthDateStr.isNotBlank()) birthDateStr else "Toque para selecionar",
                                    fontSize = 13.sp,
                                    fontWeight = if (birthDateStr.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (birthDateStr.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (calculatedAge != null) {
                            Surface(
                                color = SleekPrimaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "$calculatedAge anos",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Weight & Height Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Weight
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = { if (it.length <= 6) weightText = it },
                        label = { Text("Peso *") },
                        placeholder = { Text("Ex: 70.5") },
                        suffix = { Text("kg", fontWeight = FontWeight.Bold, color = SleekPrimary) },
                        leadingIcon = { Icon(imageVector = Icons.Default.FitnessCenter, contentDescription = null, tint = SleekPrimary) },
                        isError = weightText.isNotBlank() && !isWeightValid,
                        supportingText = { Text("1 a 500 kg") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_profile_weight")
                    )

                    // Height
                    OutlinedTextField(
                        value = heightText,
                        onValueChange = { if (it.length <= 5) heightText = it },
                        label = { Text("Altura *") },
                        placeholder = { Text("Ex: 1.75") },
                        suffix = { Text("m", fontWeight = FontWeight.Bold, color = SleekPrimary) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Height, contentDescription = null, tint = SleekPrimary) },
                        isError = heightText.isNotBlank() && !isHeightValid,
                        supportingText = { Text("0.30 a 2.70 m") },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_profile_height")
                    )
                }
            }
        }

        // Save Button
        Button(
            onClick = {
                if (isFormValid && weightDouble != null && heightDouble != null) {
                    onSaveProfile(
                        fullName.trim(),
                        sex.trim(),
                        birthDateStr.ifBlank { null },
                        weightDouble,
                        heightDouble,
                        profile?.photoUri
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
                .testTag("save_profile_button")
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Salvar Perfil", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        // Privacy & Medical Disclaimer Info
        Surface(
            color = Color(0xFFF7F2FA),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, Color(0xFFE7E0EC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SleekPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Privacidade: Seus dados de perfil são armazenados localmente e nunca são enviados para a nuvem.",
                    fontSize = 10.sp,
                    color = Color(0xFF49454F),
                    lineHeight = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
