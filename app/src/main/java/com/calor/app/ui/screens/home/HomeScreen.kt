package com.calor.app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calor.app.domain.util.CalorieCalculator
import com.calor.app.ui.components.CalorCard
import com.calor.app.ui.theme.CoralPrimary
import com.calor.app.ui.theme.MintSecondary
import com.calor.app.ui.theme.ProgressTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onQuickEat: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val summary by viewModel.daySummary.collectAsState()
    val logs by viewModel.todayLogs.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val progress = (summary.eatenKcal / summary.goalKcal.coerceAtLeast(1)).coerceIn(0f, 1.2f)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calor") },
                actions = {
                    IconButton(onClick = onHistory) {
                        Icon(Icons.Default.History, contentDescription = "История")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CalorCard {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Сегодня", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${CalorieCalculator.roundKcal(summary.eatenKcal)} из ${summary.goalKcal} ккал",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    LinearProgressIndicator(
                        progress = { progress.coerceAtMost(1f) },
                        modifier = Modifier.fillMaxWidth().height(12.dp),
                        trackColor = ProgressTrack,
                        color = if (summary.remainingKcal >= 0) MintSecondary else CoralPrimary,
                    )
                    Text(
                        if (summary.remainingKcal >= 0) {
                            "Осталось на сегодня: ${CalorieCalculator.roundKcal(summary.remainingKcal)} ккал"
                        } else {
                            "Сегодня уже ${CalorieCalculator.roundKcal(summary.eatenKcal)} из ${summary.goalKcal} ккал"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Button(
                onClick = onQuickEat,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
            ) {
                Text("Съел что-то")
            }

            if (favorites.isNotEmpty()) {
                Text("Избранное", style = MaterialTheme.typography.titleSmall)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(favorites, key = { "${it.sourceType}-${it.id}" }) { item ->
                        FilterChip(
                            selected = false,
                            onClick = onQuickEat,
                            label = { Text(item.name) },
                        )
                    }
                }
            }

            Text("Недавние записи", style = MaterialTheme.typography.titleSmall)
            if (logs.isEmpty()) {
                Text(
                    "Сегодня ещё ничего не записано",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                logs.take(5).forEach { log ->
                    CalorCard {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(log.sourceName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    "${log.gramsEaten.toInt()} г",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text("${CalorieCalculator.roundKcal(log.kcalEaten)} ккал")
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
