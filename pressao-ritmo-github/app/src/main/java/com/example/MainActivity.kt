package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.UserProfile
import com.example.domain.backup.ReportExporter
import com.example.ui.dialogs.BackupExportDialog
import com.example.ui.dialogs.HelpDialog
import com.example.ui.dialogs.ProfileEditDialog
import com.example.ui.dialogs.ProfileManagerDialog
import com.example.ui.dialogs.RemindersDialog
import com.example.ui.components.VideoSplashScreen
import com.example.ui.screens.ChartsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MeasurementFormScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekPrimaryContainer
import com.example.ui.theme.SleekSurfaceVariant
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenTab
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val profile by viewModel.profileState.collectAsState()
                val allProfiles by viewModel.allProfiles.collectAsState()
                val reminders by viewModel.allReminders.collectAsState()
                val allMeasurements by viewModel.allMeasurements.collectAsState()
                val recentMeasurements by viewModel.recentMeasurements.collectAsState()
                val filteredMeasurements by viewModel.filteredMeasurements.collectAsState()
                val dateFilter by viewModel.dateFilter.collectAsState()
                val overallStats by viewModel.overallStats.collectAsState()
                val filteredStats by viewModel.filteredStats.collectAsState()
                val editingMeasurement by viewModel.editingMeasurement.collectAsState()

                var showSplash by remember { mutableStateOf(true) }
                val snackbarHostState = remember { SnackbarHostState() }
                var showHelpDialog by remember { mutableStateOf(value = false) }
                var showBackupDialog by remember { mutableStateOf(false) }
                var showRemindersDialog by remember { mutableStateOf(false) }
                var showProfileManagerDialog by remember { mutableStateOf(false) }
                var showProfileEditDialog by remember { mutableStateOf(false) }
                var profileToEdit by remember { mutableStateOf<UserProfile?>(null) }

                val enabledRemindersCount = reminders.count { it.isEnabled }

                // Collect transient snackbar messages
                LaunchedEffect(Unit) {
                    viewModel.userMessage.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "splash_transition"
                ) { isSplashScreen ->
                    if (isSplashScreen) {
                        VideoSplashScreen { showSplash = false }
                    } else {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = MaterialTheme.colorScheme.background,
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_launcher_fg_img),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape),
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Pressão & Ritmo",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                letterSpacing = (-0.2).sp
                                            )
                                        }
                                    },
                                    actions = {
                                        // Reminders Button with badge
                                        IconButton(
                                            onClick = { showRemindersDialog = true },
                                            modifier = Modifier.testTag("top_bar_reminders_button")
                                        ) {
                                            if (enabledRemindersCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(
                                                            containerColor = MaterialTheme.colorScheme.primary,
                                                            contentColor = Color.White
                                                        ) {
                                                            Text(text = enabledRemindersCount.toString())
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Alarm,
                                                        contentDescription = "Lembretes de Medição",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Alarm,
                                                    contentDescription = "Lembretes de Medição",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        // Profile Switcher/Manager Button
                                        IconButton(
                                            onClick = { showProfileManagerDialog = true },
                                            modifier = Modifier.testTag("top_bar_profiles_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Group,
                                                contentDescription = "Gerenciar Perfis",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        // Backup & Export Button
                                        IconButton(
                                            onClick = { showBackupDialog = true },
                                            modifier = Modifier.testTag("top_bar_backup_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Backup,
                                                contentDescription = "Backup e Exportação",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Help Guide Button
                                        IconButton(
                                            onClick = { showHelpDialog = true },
                                            modifier = Modifier.testTag("top_bar_help_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                                contentDescription = "Guia e Ajuda",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 4.dp
                                ) {
                                    NavigationBarItem(
                                        selected = currentTab == ScreenTab.DASHBOARD,
                                        onClick = { viewModel.selectTab(ScreenTab.DASHBOARD) },
                                        icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Início") },
                                        label = { Text("Painel", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_dashboard"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == ScreenTab.NEW_MEASUREMENT,
                                        onClick = { viewModel.selectTab(ScreenTab.NEW_MEASUREMENT) },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.AddCircle,
                                                contentDescription = if (editingMeasurement != null) "Editar" else "Aferir"
                                            )
                                        },
                                        label = { Text(if (editingMeasurement != null) "Editar" else "Aferir", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_new_measurement"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == ScreenTab.HISTORY,
                                        onClick = { viewModel.selectTab(ScreenTab.HISTORY) },
                                        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Histórico") },
                                        label = { Text("Histórico", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_history"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == ScreenTab.CHARTS,
                                        onClick = { viewModel.selectTab(ScreenTab.CHARTS) },
                                        icon = { Icon(imageVector = Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Gráficos") },
                                        label = { Text("Gráficos", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_charts"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == ScreenTab.PROFILE,
                                        onClick = { viewModel.selectTab(ScreenTab.PROFILE) },
                                        icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Perfil") },
                                        label = { Text("Perfil", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                        modifier = Modifier.testTag("nav_profile"),
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                                AnimatedContent(
                                    targetState = currentTab,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "screen_transition"
                                ) { tab ->
                                    when (tab) {
                                        ScreenTab.DASHBOARD -> DashboardScreen(
                                            profile = profile,
                                            allProfiles = allProfiles,
                                            reminders = reminders,
                                            stats = overallStats,
                                            recentMeasurements = recentMeasurements,
                                            onNavigateToNewMeasurement = { viewModel.selectTab(ScreenTab.NEW_MEASUREMENT) },
                                            onNavigateToProfile = { viewModel.selectTab(ScreenTab.PROFILE) },
                                            onNavigateToHistory = { viewModel.selectTab(ScreenTab.HISTORY) },
                                            onOpenProfileManager = { showProfileManagerDialog = true },
                                            onOpenReminders = { showRemindersDialog = true },
                                            onEditMeasurement = { viewModel.startEditMeasurement(it) },
                                            onDeleteMeasurement = { viewModel.deleteMeasurement(it) }
                                        )
                                        ScreenTab.NEW_MEASUREMENT -> MeasurementFormScreen(
                                            editingMeasurement = editingMeasurement,
                                            profile = profile,
                                            onSave = { id, sys, dia, hr, epoch, obs ->
                                                viewModel.saveMeasurement(id, sys, dia, hr, epoch, obs) {}
                                            },
                                            onCancel = { viewModel.cancelEditMeasurement() },
                                            onNavigateToProfile = { viewModel.selectTab(ScreenTab.PROFILE) }
                                        )
                                        ScreenTab.HISTORY -> HistoryScreen(
                                            measurements = filteredMeasurements,
                                            dateFilter = dateFilter,
                                            onFilterChange = { start, end, order ->
                                                viewModel.updateDateFilter(start, end, order)
                                            },
                                            onClearFilter = { viewModel.clearDateFilter() },
                                            onEditMeasurement = { viewModel.startEditMeasurement(it) },
                                            onDeleteMeasurement = { viewModel.deleteMeasurement(it) },
                                            onNavigateToNewMeasurement = { viewModel.selectTab(ScreenTab.NEW_MEASUREMENT) },
                                            onExportPdf = {
                                                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                                                val shareIntent = ReportExporter.createShareIntent(
                                                    context = this@MainActivity,
                                                    fileName = "relatorio_pressao_$timestamp.pdf",
                                                    mimeType = "application/pdf"
                                                ) { fos ->
                                                    ReportExporter.writePdfReport(
                                                        outputStream = fos,
                                                        profile = profile,
                                                        measurements = filteredMeasurements,
                                                        stats = filteredStats,
                                                        context = this@MainActivity,
                                                        startDate = dateFilter.startDate,
                                                        endDate = dateFilter.endDate
                                                    )
                                                }
                                                startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório PDF"))
                                            }
                                        )
                                        ScreenTab.CHARTS -> ChartsScreen(
                                            measurements = filteredMeasurements,
                                            stats = filteredStats,
                                            dateFilter = dateFilter,
                                            onFilterChange = { start, end, order ->
                                                viewModel.updateDateFilter(start, end, order)
                                            },
                                            onNavigateToNewMeasurement = { viewModel.selectTab(ScreenTab.NEW_MEASUREMENT) }
                                        )
                                        ScreenTab.PROFILE -> ProfileScreen(
                                            profile = profile,
                                            allProfiles = allProfiles,
                                            onSelectProfile = { viewModel.selectProfile(it) },
                                            onAddNewProfile = {
                                                profileToEdit = null
                                                showProfileEditDialog = true
                                            },
                                            onSaveProfile = { name, sex, birth, weight, height, photoUri ->
                                                viewModel.saveProfile(
                                                    id = profile?.id,
                                                    fullName = name,
                                                    sex = sex,
                                                    birthDate = birth,
                                                    weight = weight,
                                                    height = height,
                                                    photoUri = photoUri
                                                ) {}
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Help Dialog
                if (showHelpDialog) {
                    HelpDialog { showHelpDialog = false }
                }

                if (showBackupDialog) {
                    BackupExportDialog(
                        profile = profile,
                        allProfiles = allProfiles,
                        measurements = allMeasurements,
                        onImportBackupJson = { json ->
                            viewModel.importJsonData(json)
                        },
                        onDismiss = { showBackupDialog = false }
                    )
                }

                // Reminders Dialog
                if (showRemindersDialog) {
                    RemindersDialog(
                        reminders = reminders,
                        onSaveReminder = { id, hour, minute, label, category, soundUri ->
                            viewModel.saveReminder(id = id, hour = hour, minute = minute, label = label, category = category, soundUri = soundUri)
                        },
                        onToggleReminder = { id, enabled ->
                            viewModel.toggleReminder(id, enabled)
                        },
                        onDeleteReminder = { id ->
                            viewModel.deleteReminder(id)
                        },
                        onSendTestReminder = {
                            viewModel.sendTestReminder()
                        },
                        onDismiss = { showRemindersDialog = false }
                    )
                }

                // Profile Manager Dialog
                if (showProfileManagerDialog) {
                    ProfileManagerDialog(
                        profiles = allProfiles,
                        activeProfile = profile,
                        onSelectProfile = { id ->
                            viewModel.selectProfile(id)
                        },
                        onAddNewProfile = {
                            profileToEdit = null
                            showProfileEditDialog = true
                        },
                        onEditProfile = { p ->
                            profileToEdit = p
                            showProfileEditDialog = true
                        },
                        onDeleteProfile = { id ->
                            viewModel.deleteProfile(id)
                        },
                        onDismiss = { showProfileManagerDialog = false }
                    )
                }

                // Profile Edit / Create Dialog
                if (showProfileEditDialog) {
                    ProfileEditDialog(
                        profileToEdit = profileToEdit,
                        onSaveProfile = { id, fullName, sex, birthDate, weight, height, avatarColorHex, photoUri ->
                            viewModel.saveProfile(
                                id = id,
                                fullName = fullName,
                                sex = sex,
                                birthDate = birthDate,
                                weight = weight,
                                height = height,
                                avatarColorHex = avatarColorHex,
                                photoUri = photoUri
                            ) {}
                            showProfileEditDialog = false
                        },
                        onDismiss = { showProfileEditDialog = false }
                    )
                }
            }
        }
    }
}


