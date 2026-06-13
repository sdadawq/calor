package com.calor.app.ui.screens.fridge

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
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
fun FridgeScreen(
    onAdd: () -> Unit,
    viewModel: FridgeViewModel = hiltViewModel(),
) {
    val items by viewModel.items.collectAsState()
    var query by remember { mutableStateOf("") }
    var editItem by remember { mutableStateOf<com.calor.app.data.db.dao.FridgeItemWithProduct?>(null) }
    var editGrams by remember { mutableStateOf("") }
    var deleteItem by remember { mutableStateOf<com.calor.app.data.db.dao.FridgeItemWithProduct?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Холодильник") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd, containerColor = CoralPrimary) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SearchField(query, { query = it; viewModel.setQuery(it) }, "Поиск в холодильнике")
            if (items.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Kitchen,
                    title = "Холодильник пока пуст",
                    subtitle = "Добавь что-нибудь вкусное",
                    actionLabel = "Добавить продукт",
                    onAction = onAdd,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items, key = { it.id }) { item ->
                        val kcal = CalorieCalculator.kcalForGrams(item.gramsAvailable, item.kcalPer100g)
                        CalorCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(item.productName, style = MaterialTheme.typography.titleMedium)
                                    Text("${item.gramsAvailable.toInt()} г")
                                }
                                Text(
                                    "${CalorieCalculator.roundKcal(kcal)} ккал в остатке",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Row {
                                    TextButton(onClick = {
                                        editItem = item
                                        editGrams = item.gramsAvailable.toInt().toString()
                                    }) { Text("Изменить") }
                                    TextButton(onClick = { deleteItem = item }) { Text("Удалить") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (editItem != null) {
        AlertDialog(
            onDismissRequest = { editItem = null },
            title = { Text("Изменить остаток") },
            text = {
                OutlinedTextField(
                    value = editGrams,
                    onValueChange = { editGrams = it },
                    label = { Text("Граммы") },
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    editGrams.toFloatOrNull()?.let { g ->
                        viewModel.updateGrams(editItem!!.id, g)
                    }
                    editItem = null
                }) { Text("Сохранить") }
            },
            dismissButton = { TextButton(onClick = { editItem = null }) { Text("Отмена") } },
        )
    }

    if (deleteItem != null) {
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Удалить из холодильника?") },
            text = { Text("Продукт останется в каталоге") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.remove(deleteItem!!.id)
                    deleteItem = null
                }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { deleteItem = null }) { Text("Отмена") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFridgeScreen(
    onBack: () -> Unit,
    catalogViewModel: com.calor.app.ui.screens.catalog.CatalogViewModel = hiltViewModel(),
    fridgeViewModel: FridgeViewModel = hiltViewModel(),
) {
    val products by catalogViewModel.products.collectAsState()
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var grams by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val selectedName = products.find { it.id == selectedId }?.name ?: "Выберите продукт"

    Scaffold(topBar = { TopAppBar(title = { Text("В холодильник") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedName,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.name) },
                            onClick = {
                                selectedId = product.id
                                expanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = grams,
                onValueChange = { grams = it },
                label = { Text("Граммы") },
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                onClick = {
                    val id = selectedId
                    val g = grams.toFloatOrNull()
                    if (id != null && g != null) {
                        fridgeViewModel.addToFridge(id, g, onBack)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Добавить") }
        }
    }
}
