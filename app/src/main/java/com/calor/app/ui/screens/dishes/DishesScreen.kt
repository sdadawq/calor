package com.calor.app.ui.screens.dishes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calor.app.domain.util.CalorieCalculator
import com.calor.app.ui.components.CalorCard
import com.calor.app.ui.components.EmptyState
import com.calor.app.ui.components.SearchField
import com.calor.app.ui.theme.CoralPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishesScreen(
    onCreate: () -> Unit,
    onOpen: (Long) -> Unit,
    viewModel: DishesViewModel = hiltViewModel(),
) {
    val dishes by viewModel.dishes.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Блюда") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreate, containerColor = CoralPrimary) {
                Icon(Icons.Default.Add, contentDescription = "Создать блюдо")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchField(query, { query = it; viewModel.setQuery(it) }, "Поиск блюд")
            if (dishes.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Restaurant,
                    title = "Пока нет блюд",
                    subtitle = "Создай первое блюдо с ингредиентами",
                    actionLabel = "Создать блюдо",
                    onAction = onCreate,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dishes, key = { it.id }) { dish ->
                        val remainingKcal = CalorieCalculator.dishKcalEaten(
                            dish.totalKcal, dish.totalGrams, dish.gramsRemaining
                        )
                        CalorCard(onClick = { onOpen(dish.id) }) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(dish.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Осталось ${dish.gramsRemaining.toInt()} г · ${CalorieCalculator.roundKcal(remainingKcal)} ккал",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
