package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DateFilter
import com.example.domain.model.Measurement
import com.example.domain.model.SortOrder
import com.example.domain.rules.HealthCalculator
import com.example.ui.components.MeasurementCard
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    measurements: List<Measurement>,
    dateFilter: DateFilter,
    onFilterChange: (startDate: Long?, endDate: Long?, sortOrder: SortOrder) -> Unit,
    onClearFilter: () -> Unit,
    onEditMeasurement: (Measurement) -> Unit,
    onDeleteMeasurement: (String) -> Unit,
    onNavigateToNewMeasurement: () -> Unit,
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var measurementToDelete by remember { mutableStateOf<Measurement?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }

    fun showStartDatePicker() {
        val cal = Calendar.getInstance()
        if (dateFilter.startDate != null) cal.timeInMillis = dateFilter.startDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                onFilterChange(newCal.timeInMillis, dateFilter.endDate, dateFilter.sortOrder)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showEndDatePicker() {
        val cal = Calendar.getInstance()
        if (dateFilter.endDate != null) cal.timeInMillis = dateFilter.endDate
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59)
                    set(Calendar.MILLISECOND, 999)
                }
                onFilterChange(dateFilter.startDate, newCal.timeInMillis, dateFilter.sortOrder)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun applyQuickFilter(days: Int?, currentMonth: Boolean = false, currentWeek: Boolean = false) {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            when {
                currentMonth -> set(Calendar.DAY_OF_MONTH, 1)
                currentWeek -> {
                    val dayOfWeek = get(Calendar.DAY_OF_WEEK)
                    add(Calendar.DAY_OF_YEAR, -(dayOfWeek - 1))
                }
                days != null -> add(Calendar.DAY_OF_YEAR, -(days - 1))
            }
        }

        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        onFilterChange(start.timeInMillis, end.timeInMillis, dateFilter.sortOrder)
    }

    val hasActiveFilter = dateFilter.startDate != null || dateFilter.endDate != null || dateFilter.sortOrder != SortOrder.NEWEST_FIRST

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            // Header & Filter section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "FILTROS E ORDENAÇÃO",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box {
                            TextButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.height(32.dp).testTag("sort_order_button"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (dateFilter.sortOrder == SortOrder.NEWEST_FIRST) "Recentes" else "Antigas",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekPrimary
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Mais recentes primeiro") },
                                    onClick = {
                                        onFilterChange(dateFilter.startDate, dateFilter.endDate, SortOrder.NEWEST_FIRST)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Mais antigas primeiro") },
                                    onClick = {
                                        onFilterChange(dateFilter.startDate, dateFilter.endDate, SortOrder.OLDEST_FIRST)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))

                        // PDF Export Button
                        TextButton(
                            onClick = onExportPdf,
                            modifier = Modifier.height(32.dp).testTag("export_pdf_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PDF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Date range picker buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedCard(
                            onClick = { showStartDatePicker() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_start_date"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Data Inicial", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = dateFilter.startDate?.let { HealthCalculator.formatEpochToDateOnly(it) } ?: "Todas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        OutlinedCard(
                            onClick = { showEndDatePicker() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_end_date"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = SleekPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Data Final", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = dateFilter.endDate?.let { HealthCalculator.formatEpochToDateOnly(it) } ?: "Todas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick filters row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickFilterChip(
                            label = "Semana",
                            onClick = { applyQuickFilter(null, currentWeek = true) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickFilterChip(
                            label = "15 dias",
                            onClick = { applyQuickFilter(15) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickFilterChip(
                            label = "Mês Atual",
                            onClick = { applyQuickFilter(null, currentMonth = true) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Active filter reset
                    if (hasActiveFilter) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Encontrados: ${measurements.size}",
                                fontSize = 10.sp,
                                color = SleekPrimary,
                                fontWeight = FontWeight.SemiBold
                            )

                            TextButton(
                                onClick = onClearFilter,
                                modifier = Modifier.height(28.dp).testTag("clear_filter_button"),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpar", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SleekPrimary)
                            }
                        }
                    }
                }
            }
        }

        // Measurement Cards or Empty state
        if (measurements.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = MaterialTheme.shapes.large,
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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = SleekPrimary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (hasActiveFilter) "Nenhum registro no período" else "Nenhum registro cadastrado",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (hasActiveFilter) {
                                "Tente alterar ou limpar os filtros de data para visualizar seus registros."
                            } else {
                                "Comece registrando suas aferições de pressão arterial e frequência cardíaca."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (hasActiveFilter) {
                            OutlinedButton(
                                onClick = onClearFilter,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Limpar Filtros", fontWeight = FontWeight.SemiBold)
                            }
                        } else {
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
                            ) {
                                Text("Nova Aferição", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(measurements, key = { it.id }) { measurement ->
                MeasurementCard(
                    measurement = measurement,
                    onEdit = { onEditMeasurement(measurement) },
                    onDelete = { measurementToDelete = measurement }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Explicit Delete confirmation dialog
    measurementToDelete?.let { measurement ->
        AlertDialog(
            onDismissRequest = { measurementToDelete = null },
            title = { Text("Excluir Aferição", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Deseja realmente excluir a aferição de ${measurement.formattedDate} com resultado ${measurement.systolic}/${measurement.diastolic} mmHg? Esta ação é definitiva e não pode ser desfeita."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMeasurement(measurement.id)
                        measurementToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_history_button")
                ) {
                    Text("Excluir Definitivamente")
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

@Composable
private fun QuickFilterChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
        }
    }
}
