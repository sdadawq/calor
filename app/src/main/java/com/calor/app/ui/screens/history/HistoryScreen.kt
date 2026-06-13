package com.calor.app.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calor.app.domain.model.MealType
import com.calor.app.domain.util.CalorieCalculator
import com.calor.app.ui.components.CalorCard
import com.calor.app.ui.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val logs by viewModel.logs.collectAsState()
    val grouped = logs.groupBy { MealType.fromString(it.mealType) }
    val total = logs.sumOf { CalorieCalculator.roundKcal(it.kcalEaten) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История за сегодня") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Назад") }
                },
            )
        },
    ) { padding ->
        if (logs.isEmpty()) {
            EmptyState(
                icon = Icons.Default.History,
                title = "Пусто",
                subtitle = "Сегодня ещё ничего не записано",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text("Итого: $total ккал", style = MaterialTheme.typography.titleMedium)
                }
                grouped.forEach { (meal, items) ->
                    item {
                        Text(meal.label, style = MaterialTheme.typography.titleSmall)
                    }
                    items(items, key = { it.id }) { log ->
                        CalorCard {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(log.sourceName)
                                    Text(
                                        "${log.gramsEaten.toInt()} г",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Row {
                                    Text("${CalorieCalculator.roundKcal(log.kcalEaten)} ккал")
                                    IconButton(onClick = { viewModel.deleteLog(log.id) }) {
                                        Text("✕")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
