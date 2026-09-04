package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DateFilter
import com.example.domain.model.Measurement
import com.example.domain.model.MeasurementStats
import com.example.domain.model.SortOrder
import com.example.domain.rules.HealthCalculator
import com.example.ui.components.ClassificationBadge
import com.example.ui.components.StatCard
import com.example.ui.theme.NormalGreen
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekTertiary
import java.util.Calendar

@Composable
fun ChartsScreen(
    measurements: List<Measurement>,
    stats: MeasurementStats,
    dateFilter: DateFilter,
    onFilterChange: (startDate: Long?, endDate: Long?, sortOrder: SortOrder) -> Unit,
    onNavigateToNewMeasurement: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Chronologically sorted for chart graphs: oldest to newest
    val chronologicalList = remember(measurements) {
        measurements.sortedBy { it.measuredAtEpoch }
    }

    var selectedBpIndex by remember(chronologicalList) {
        mutableStateOf(if (chronologicalList.isNotEmpty()) chronologicalList.lastIndex else null)
    }

    var selectedHrIndex by remember(chronologicalList) {
        mutableStateOf(if (chronologicalList.isNotEmpty()) chronologicalList.lastIndex else null)
    }

    fun applyQuickFilter(days: Int?, currentMonth: Boolean = false, currentWeek: Boolean = false) {
        if (days == null && !currentMonth && !currentWeek) {
            onFilterChange(null, null, SortOrder.NEWEST_FIRST)
            return
        }
        
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

        onFilterChange(start.timeInMillis, end.timeInMillis, SortOrder.NEWEST_FIRST)
    }

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
                onFilterChange(newCal.timeInMillis, dateFilter.endDate, SortOrder.NEWEST_FIRST)
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
                onFilterChange(dateFilter.startDate, newCal.timeInMillis, SortOrder.NEWEST_FIRST)
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
        // Period selector & Quick filter chips
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PERÍODO DO GRÁFICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Quick range buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    QuickFilterChip(
                        label = "Tudo",
                        onClick = { applyQuickFilter(null) },
                        modifier = Modifier.weight(1f)
                    )
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

                Spacer(modifier = Modifier.height(8.dp))

                // Custom date pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedCard(
                        onClick = { showStartDatePicker() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Início", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = dateFilter.startDate?.let { HealthCalculator.formatEpochToDateOnly(it) } ?: "Início",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    OutlinedCard(
                        onClick = { showEndDatePicker() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fim", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = dateFilter.endDate?.let { HealthCalculator.formatEpochToDateOnly(it) } ?: "Hoje",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (chronologicalList.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = null, tint = SleekPrimary, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sem dados para o período",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cadastre medições ou amplie o período para visualizar os gráficos de evolução.",
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
                    ) {
                        Text("Registrar Aferição", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Period stats summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Média Sistólica",
                    value = stats.avgSystolic?.let { "$it" } ?: "-",
                    unit = "mmHg",
                    icon = Icons.Default.Speed,
                    iconColor = SleekPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Média Diastólica",
                    value = stats.avgDiastolic?.let { "$it" } ?: "-",
                    unit = "mmHg",
                    icon = Icons.Default.Speed,
                    iconColor = SleekSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Média Pulso",
                    value = stats.avgHeartRate?.let { "$it" } ?: "-",
                    unit = "BPM",
                    icon = Icons.Default.Favorite,
                    iconColor = SleekTertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // 1. Blood Pressure Line Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "PRESSÃO ARTERIAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${chronologicalList.size} pontos de medição",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Legend
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SleekPrimary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sistólica", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SleekPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SleekSecondary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Diastólica", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SleekSecondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selected Point Info Banner
                    selectedBpIndex?.let { idx ->
                        if (idx in chronologicalList.indices) {
                            val selected = chronologicalList[idx]
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = selected.formattedDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = "${selected.systolic} / ${selected.diastolic} mmHg",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = selected.classification.color
                                        )
                                    }
                                    ClassificationBadge(classification = selected.classification)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Canvas Line Chart for Blood Pressure
                    BloodPressureCanvas(
                        measurements = chronologicalList,
                        selectedIndex = selectedBpIndex,
                        onSelectIndex = { selectedBpIndex = it }
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Toque nos pontos para inspecionar os valores e datas",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }

            // 2. Heart Rate Line Chart
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FREQUÊNCIA CARDÍACA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Faixa de repouso: 60 - 100 BPM",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(SleekTertiary))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BPM", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SleekTertiary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selected Heart Rate point info
                    selectedHrIndex?.let { idx ->
                        if (idx in chronologicalList.indices) {
                            val selected = chronologicalList[idx]
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = selected.formattedDate, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        text = "${selected.heartRate} BPM",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekTertiary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Canvas Line Chart for Heart Rate
                    HeartRateCanvas(
                        measurements = chronologicalList,
                        selectedIndex = selectedHrIndex,
                        onSelectIndex = { selectedHrIndex = it }
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Faixa sombreada indica zona normal de 60 a 100 BPM",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
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

@Composable
private fun BloodPressureCanvas(
    measurements: List<Measurement>,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("bp_chart_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(measurements) {
                    detectTapGestures { offset ->
                        if (measurements.isEmpty()) return@detectTapGestures
                        val stepX = if (measurements.size > 1) {
                            (size.width - 70f) / (measurements.size - 1)
                        } else 0f
                        val leftMargin = 45f
                        val clickedIdx = if (measurements.size == 1) 0 else {
                            val relX = offset.x - leftMargin
                            val idx = (relX / stepX).toInt().coerceIn(0, measurements.lastIndex)
                            idx
                        }
                        onSelectIndex(clickedIdx)
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val leftPadding = 45f
            val rightPadding = 25f
            val topPadding = 20f
            val bottomPadding = 30f
            val plotWidth = width - leftPadding - rightPadding
            val plotHeight = height - topPadding - bottomPadding

            val minY = 40f
            val maxY = 220f
            val rangeY = maxY - minY

            fun getY(value: Float): Float {
                val clamped = value.coerceIn(minY, maxY)
                return topPadding + plotHeight - ((clamped - minY) / rangeY * plotHeight)
            }

            fun getX(index: Int): Float {
                if (measurements.size <= 1) return leftPadding + (plotWidth / 2f)
                return leftPadding + (index.toFloat() / (measurements.size - 1)) * plotWidth
            }

            // Grid lines (80, 120, 140, 180)
            val gridLevels = listOf(80f, 120f, 140f, 180f)
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

            for (level in gridLevels) {
                val y = getY(level)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.5f,
                    pathEffect = dashedEffect
                )

                // Draw Y-axis labels
                drawText(
                    textMeasurer = textMeasurer,
                    text = level.toInt().toString(),
                    style = TextStyle(
                        color = onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    topLeft = Offset(5f, y - 15f)
                )
            }

            // Reference standard lines: 120 (Normal Systolic limit) & 80 (Normal Diastolic limit)
            val y120 = getY(120f)
            drawLine(
                color = SleekPrimary.copy(alpha = 0.25f),
                start = Offset(leftPadding, y120),
                end = Offset(width - rightPadding, y120),
                strokeWidth = 2f
            )

            val y80 = getY(80f)
            drawLine(
                color = SleekSecondary.copy(alpha = 0.25f),
                start = Offset(leftPadding, y80),
                end = Offset(width - rightPadding, y80),
                strokeWidth = 2f
            )

            // Draw Systolic Line & Diastolic Line
            val sysPath = Path()
            val diaPath = Path()

            val sysPoints = mutableListOf<Offset>()
            val diaPoints = mutableListOf<Offset>()

            for (i in measurements.indices) {
                val m = measurements[i]
                val x = getX(i)
                val ySys = getY(m.systolic.toFloat())
                val yDia = getY(m.diastolic.toFloat())

                sysPoints.add(Offset(x, ySys))
                diaPoints.add(Offset(x, yDia))

                if (i == 0) {
                    sysPath.moveTo(x, ySys)
                    diaPath.moveTo(x, yDia)
                } else {
                    sysPath.lineTo(x, ySys)
                    diaPath.lineTo(x, yDia)
                }
            }

            // Draw paths
            drawPath(
                path = sysPath,
                color = SleekPrimary,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            drawPath(
                path = diaPath,
                color = SleekSecondary,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Draw data points & selected indicator
            for (i in measurements.indices) {
                val pSys = sysPoints[i]
                val pDia = diaPoints[i]
                val isSelected = selectedIndex == i

                // Systolic Point
                drawCircle(
                    color = if (isSelected) Color.White else SleekPrimary,
                    radius = if (isSelected) 6f else 4f,
                    center = pSys
                )
                if (isSelected) {
                    drawCircle(
                        color = SleekPrimary,
                        radius = 7f,
                        center = pSys,
                        style = Stroke(width = 3f)
                    )
                }

                // Diastolic Point
                drawCircle(
                    color = if (isSelected) Color.White else SleekSecondary,
                    radius = if (isSelected) 6f else 4f,
                    center = pDia
                )
                if (isSelected) {
                    drawCircle(
                        color = SleekSecondary,
                        radius = 7f,
                        center = pDia,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeartRateCanvas(
    measurements: List<Measurement>,
    selectedIndex: Int?,
    onSelectIndex: (Int) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("hr_chart_canvas")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(measurements) {
                    detectTapGestures { offset ->
                        if (measurements.isEmpty()) return@detectTapGestures
                        val stepX = if (measurements.size > 1) {
                            (size.width - 70f) / (measurements.size - 1)
                        } else 0f
                        val leftMargin = 45f
                        val clickedIdx = if (measurements.size == 1) 0 else {
                            val relX = offset.x - leftMargin
                            val idx = (relX / stepX).toInt().coerceIn(0, measurements.lastIndex)
                            idx
                        }
                        onSelectIndex(clickedIdx)
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val leftPadding = 45f
            val rightPadding = 25f
            val topPadding = 20f
            val bottomPadding = 25f
            val plotWidth = width - leftPadding - rightPadding
            val plotHeight = height - topPadding - bottomPadding

            val minY = 40f
            val maxY = 160f
            val rangeY = maxY - minY

            fun getY(value: Float): Float {
                val clamped = value.coerceIn(minY, maxY)
                return topPadding + plotHeight - ((clamped - minY) / rangeY * plotHeight)
            }

            fun getX(index: Int): Float {
                if (measurements.size <= 1) return leftPadding + (plotWidth / 2f)
                return leftPadding + (index.toFloat() / (measurements.size - 1)) * plotWidth
            }

            // Normal range band 60 to 100 BPM
            val y60 = getY(60f)
            val y100 = getY(100f)
            drawRect(
                color = NormalGreen.copy(alpha = 0.08f),
                topLeft = Offset(leftPadding, y100),
                size = androidx.compose.ui.geometry.Size(plotWidth, y60 - y100)
            )

            // Grid lines
            val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            val gridLevels = listOf(60f, 100f, 140f)
            for (level in gridLevels) {
                val y = getY(level)
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(leftPadding, y),
                    end = Offset(width - rightPadding, y),
                    strokeWidth = 1.5f,
                    pathEffect = dashedEffect
                )

                // Draw Y-axis labels
                drawText(
                    textMeasurer = textMeasurer,
                    text = level.toInt().toString(),
                    style = TextStyle(
                        color = onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    topLeft = Offset(5f, y - 15f)
                )
            }

            val hrPath = Path()
            val hrPoints = mutableListOf<Offset>()

            for (i in measurements.indices) {
                val m = measurements[i]
                val x = getX(i)
                val yHr = getY(m.heartRate.toFloat())
                hrPoints.add(Offset(x, yHr))

                if (i == 0) {
                    hrPath.moveTo(x, yHr)
                } else {
                    hrPath.lineTo(x, yHr)
                }
            }

            drawPath(
                path = hrPath,
                color = SleekTertiary,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            for (i in measurements.indices) {
                val pHr = hrPoints[i]
                val isSelected = selectedIndex == i

                drawCircle(
                    color = if (isSelected) Color.White else SleekTertiary,
                    radius = if (isSelected) 6f else 4f,
                    center = pHr
                )
                if (isSelected) {
                    drawCircle(
                        color = SleekTertiary,
                        radius = 7f,
                        center = pHr,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}
