package com.example.ui.dialogs

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.backup.JsonBackupManager
import com.example.domain.backup.ReportExporter
import com.example.domain.model.Measurement
import com.example.domain.model.MeasurementStats
import com.example.domain.model.UserProfile
import com.example.ui.theme.SleekOutline
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekTertiary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupExportDialog(
    profile: UserProfile?,
    allProfiles: List<UserProfile>,
    measurements: List<Measurement>,
    onImportBackupJson: suspend (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pendingImportJson by remember { mutableStateOf<String?>(null) }
    var pendingImportStats by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    val timestamp = remember {
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }

    // Save JSON Document Launcher
    val saveJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                context.contentResolver.openOutputStream(it)?.use { os ->
                    val json = JsonBackupManager.createBackupJson(profile, allProfiles, measurements, context)
                    os.write(json.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    // Import JSON File Picker Launcher
    val importFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val stringBuilder = StringBuilder()
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                            var line = reader.readLine()
                            while (line != null) {
                                stringBuilder.append(line).append("\n")
                                line = reader.readLine()
                            }
                        }
                    }
                    val jsonContent = stringBuilder.toString()
                    val result = JsonBackupManager.parseBackupJson(jsonContent, context)
                    if (result.isSuccess) {
                        val profileName = result.userProfile?.fullName ?: "Sem perfil cadastrado"
                        withContext(Dispatchers.Main) {
                            pendingImportStats = Pair(profileName, result.importedCount)
                            pendingImportJson = jsonContent
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            importErrorMessage = result.errorMessage ?: "Arquivo JSON inválido ou incompatível."
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        importErrorMessage = "Falha ao ler arquivo: ${e.localizedMessage}"
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = null,
                        tint = SleekPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Backup & Exportação", fontWeight = FontWeight.Bold)
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section: Backup JSON
                Text(
                    text = "BACKUP",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, SleekOutline.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Exportar / Importar JSON",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Permite migrar ou restaurar todos os seus registros e dados de perfil entre dispositivos.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val shareIntent = ReportExporter.createShareIntent(
                                        context = context,
                                        fileName = "minha_pressao_backup_$timestamp.json",
                                        mimeType = "application/json"
                                    ) { fos ->
                                        val json = JsonBackupManager.createBackupJson(profile, allProfiles, measurements, context)
                                        fos.write(json.toByteArray(Charsets.UTF_8))
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Backup JSON"))
                                },
                                modifier = Modifier.weight(1f).height(54.dp).testTag("export_json_share_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Compartilhar", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    saveJsonLauncher.launch("minha_pressao_backup_$timestamp.json")
                                },
                                modifier = Modifier.weight(1f).height(54.dp).testTag("export_json_save_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Salvar", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Button(
                            onClick = { importFilePicker.launch("application/json") },
                            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("import_json_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = SleekSecondary, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restaurar / Importar Backup", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Removed PDF section as requested
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary, contentColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Concluído", fontWeight = FontWeight.Bold)
            }
        }
    )

    // Overwrite Confirmation Dialog when JSON is picked
    pendingImportStats?.let { (profileName, count) ->
        AlertDialog(
            onDismissRequest = {
                pendingImportStats = null
                pendingImportJson = null
            },
            title = { Text("Confirmar Restauração de Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFFFFD8E4),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Atenção: A importação substituirá todos os registros e dados do perfil atuais no dispositivo.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF31111D)
                            )
                        }
                    }

                    Text("Dados do backup a serem restaurados:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("• Perfil: $profileName", fontSize = 12.sp)
                    Text("• Aferições de pressão: $count registros", fontSize = 12.sp)
                    Text("Deseja prosseguir com a restauração?", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val json = pendingImportJson
                        pendingImportStats = null
                        pendingImportJson = null
                        if (json != null) {
                            coroutineScope.launch {
                                onImportBackupJson(json)
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("confirm_import_button")
                ) {
                    Text("Substituir e Restaurar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        pendingImportStats = null
                        pendingImportJson = null
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Error alert dialog
    importErrorMessage?.let { err ->
        AlertDialog(
            onDismissRequest = { importErrorMessage = null },
            title = { Text("Erro na Importação", fontWeight = FontWeight.Bold) },
            text = { Text(err) },
            confirmButton = {
                Button(
                    onClick = { importErrorMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("OK")
                }
            }
        )
    }
}
