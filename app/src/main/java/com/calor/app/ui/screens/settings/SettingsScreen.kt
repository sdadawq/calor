package com.calor.app.ui.screens.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val goal by viewModel.dailyGoal.collectAsState()
    var goalText by remember(goal) { mutableStateOf(goal.toString()) }
    var showImportWarning by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                }.onSuccess { json ->
                    if (json != null) {
                        pendingImport = json
                        showImportWarning = true
                    }
                }.onFailure {
                    snackbar.showSnackbar("Не удалось прочитать файл")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Назад") } },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = goalText,
                onValueChange = { goalText = it },
                label = { Text("Дневная цель (ккал)") },
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = {
                goalText.toIntOrNull()?.let { viewModel.setDailyGoal(it) }
            }) { Text("Сохранить цель") }

            TextButton(onClick = {
                scope.launch {
                    val json = viewModel.exportJson()
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(intent, "Экспорт Calor"))
                    snackbar.showSnackbar("Резервная копия готова")
                }
            }) { Text("Экспорт данных") }

            TextButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*")) }) {
                Text("Импорт данных")
            }

            Text(
                "Резервная копия хранится без шифрования. Импорт полностью заменит текущие данные.",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            )
            Text("Calor v1.0.0", style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }

    if (showImportWarning && pendingImport != null) {
        AlertDialog(
            onDismissRequest = { showImportWarning = false },
            title = { Text("Импорт данных") },
            text = { Text("Все текущие данные будут заменены. Продолжить?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.importJson(pendingImport!!) { result ->
                        scope.launch {
                            result.onSuccess {
                                snackbar.showSnackbar("Данные восстановлены")
                            }.onFailure {
                                snackbar.showSnackbar(it.message ?: "Ошибка импорта")
                            }
                        }
                    }
                    showImportWarning = false
                }) { Text("Импортировать") }
            },
            dismissButton = {
                TextButton(onClick = { showImportWarning = false }) { Text("Отмена") }
            },
        )
    }
}
