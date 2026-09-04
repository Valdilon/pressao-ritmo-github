package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Measurement
import com.example.domain.model.MeasurementStats
import com.example.domain.model.PressureClassification
import com.example.domain.model.Reminder
import com.example.domain.model.UserProfile
import com.example.domain.rules.HealthCalculator
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.ClinicalReferenceCard
import com.example.ui.components.MeasurementCard
import com.example.ui.components.ProfileSwitcherHeader
import com.example.ui.components.StatCard
import com.example.ui.components.WarningBanner
import com.example.ui.theme.SleekOnPrimaryContainer
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekTertiary

@Composable
fun DashboardScreen(
    profile: UserProfile?,
    allProfiles: List<UserProfile> = emptyList(),
    reminders: List<Reminder> = emptyList(),
    stats: MeasurementStats,
    recentMeasurements: List<Measurement>,
    onNavigateToNewMeasurement: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onOpenProfileManager: () -> Unit,
    onOpenReminders: () -> Unit,
    onEditMeasurement: (Measurement) -> Unit,
    onDeleteMeasurement: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var measurementToDelete by remember { mutableStateOf<Measurement?>(null) }
    val enabledRemindersCount = reminders.count { it.isEnabled }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Multi-Profile & Reminders Header
        item {
            Spacer(modifier = Modifier.height(2.dp))
            ProfileSwitcherHeader(
                activeProfile = profile,
                profileCount = allProfiles.size,
                activeRemindersCount = enabledRemindersCount,
                onOpenProfileManager = onOpenProfileManager,
                onOpenReminders = onOpenReminders
            )
        }

        // Profile warning if no profile exists
        if (profile == null) {
            item {
                WarningBanner(
                    title = "Cadastre seu Primeiro Perfil",
                    message = "Cadastre os dados biométricos (peso, altura, idade) para habilitar o cálculo automático do IMC e acompanhamento personalizado.",
                    buttonText = "Cadastrar Perfil",
                    onButtonClick = onNavigateToProfile,
                    icon = Icons.Default.Person,
                    modifier = Modifier.testTag("profile_warning_banner")
                )
            }
        }

        // Hero Monthly/Overall Average Card (from Sleek Interface design)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(
                    containerColor = SleekPrimaryContainer
                ),
                border = BorderStroke(1.dp, Color(0xFFD0BCFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Header of hero card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (profile != null) "MÉDIA DE ${profile.fullName.uppercase()}" else "MÉDIA GERAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = Color(0xFF49454F),
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            color = Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (stats.totalCount > 0) "${stats.totalCount} aferições" else "Sem dados",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF49454F),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Large Average Blood Pressure
                    Row(verticalAlignment = Alignment.Bottom) {
                        val avgSys = stats.avgSystolic?.let { "$it" } ?: "--"
                        val avgDia = stats.avgDiastolic?.let { "$it" } ?: "--"
                        Text(
                            text = "$avgSys/$avgDia",
                            style = MaterialTheme.typography.displayLarge,
                            color = SleekOnPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "mmHg",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF49454F),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFFD0BCFF).copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom info row of hero card
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FREQUÊNCIA: ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF49454F),
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = stats.avgHeartRate?.let { "$it BPM" } ?: "-- BPM",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekOnPrimaryContainer
                            )
                        }

                        // Classification badge for average
                        if (stats.avgSystolic != null && stats.avgDiastolic != null) {
                            val avgClassification = HealthCalculator.classifyPressure(
                                stats.avgSystolic.toInt(),
                                stats.avgDiastolic.toInt()
                            )
                            ClassificationBadge(
                                classification = avgClassification,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4 Summary Metric Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "MÉTRICAS DO PERFIL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Média Sistólica",
                        value = stats.avgSystolic?.let { "$it" } ?: "-",
                        unit = "mmHg",
                        icon = Icons.Default.Speed,
                        iconColor = SleekPrimary,
                        modifier = Modifier.weight(1f).testTag("stat_card_systolic")
                    )
                    StatCard(
                        title = "Média Diastólica",
                        value = stats.avgDiastolic?.let { "$it" } ?: "-",
                        unit = "mmHg",
                        icon = Icons.Default.Speed,
                        iconColor = SleekSecondary,
                        modifier = Modifier.weight(1f).testTag("stat_card_diastolic")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Frequência Cardíaca",
                        value = stats.avgHeartRate?.let { "$it" } ?: "-",
                        unit = "BPM",
                        icon = Icons.Default.Favorite,
                        iconColor = SleekTertiary,
                        modifier = Modifier.weight(1f).testTag("stat_card_heart_rate")
                    )
                    StatCard(
                        title = "Total de Aferições",
                        value = "${stats.totalCount}",
                        unit = if (stats.totalCount == 1) "registro" else "registros",
                        icon = Icons.Default.MedicalServices,
                        iconColor = SleekPrimary,
                        modifier = Modifier.weight(1f).testTag("stat_card_total")
                    )
                }
            }
        }

        // Clinical Reference Table
        item {
            ClinicalReferenceCard()
        }

        // Recent measurements header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AFERIÇÕES RECENTES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (recentMeasurements.isNotEmpty()) {
                    TextButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.testTag("view_all_history_button")
                    ) {
                        Text(
                            text = "Ver tudo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SleekPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = SleekPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Empty state or recent list
        if (recentMeasurements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(SleekPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MedicalServices,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Nenhuma medição encontrada",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Toque no botão abaixo para registrar a primeira aferição para ${profile?.fullName ?: "este perfil"}.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToNewMeasurement,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekPrimary,
                                contentColor = Color.White
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("dashboard_empty_add_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nova Aferição", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            items(recentMeasurements, key = { it.id }) { measurement ->
                MeasurementCard(
                    measurement = measurement,
                    onEdit = { onEditMeasurement(measurement) },
                    onDelete = { measurementToDelete = measurement }
                )
            }
        }

        // Sleek Medical disclaimer footer
        item {
            Surface(
                color = Color(0xFFF7F2FA),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFFE7E0EC)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
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
                        text = "Aviso: " + PressureClassification.GENERAL_DISCLAIMER,
                        fontSize = 10.sp,
                        color = Color(0xFF49454F),
                        lineHeight = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Delete confirmation dialog
    measurementToDelete?.let { measurement ->
        AlertDialog(
            onDismissRequest = { measurementToDelete = null },
            title = { Text("Excluir Aferição", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Deseja realmente excluir o registro de ${measurement.formattedDate} (${measurement.systolic}/${measurement.diastolic} mmHg)? Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMeasurement(measurement.id)
                        measurementToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { measurementToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

