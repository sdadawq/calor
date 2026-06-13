package com.calor.app.ui.screens.dishes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calor.app.ui.screens.catalog.CatalogViewModel

data class IngredientDraft(val productId: Long, val productName: String, val grams: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDishScreen(
    onBack: () -> Unit,
    dishesViewModel: DishesViewModel = hiltViewModel(),
    catalogViewModel: CatalogViewModel = hiltViewModel(),
) {
    val products by catalogViewModel.products.collectAsState()
    var name by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<IngredientDraft>() }
    var selectedId by remember { mutableStateOf<Long?>(null) }
    var grams by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val selectedName = products.find { it.id == selectedId }?.name ?: "Продукт"

    Scaffold(topBar = { TopAppBar(title = { Text("Новое блюдо") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название блюда") },
                modifier = Modifier.fillMaxWidth(),
            )
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it },
                    label = { Text("Граммы") },
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = {
                    val id = selectedId
                    val g = grams.toFloatOrNull()
                    val product = products.find { it.id == id }
                    if (id != null && g != null && product != null) {
                        ingredients.add(IngredientDraft(id, product.name, g))
                        grams = ""
                    }
                }) { Text("Добавить") }
            }
            ingredients.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("${item.productName} — ${item.grams.toInt()} г")
                    IconButton(onClick = { ingredients.removeAt(index) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                    }
                }
            }
            TextButton(
                onClick = {
                    if (name.isNotBlank() && ingredients.isNotEmpty()) {
                        dishesViewModel.createDish(
                            name.trim(),
                            ingredients.map { it.productId to it.grams },
                            onBack,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Сохранить блюдо") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DishDetailScreen(
    dishId: Long,
    onBack: () -> Unit,
    viewModel: DishesViewModel = hiltViewModel(),
) {
    val dishes by viewModel.dishes.collectAsState()
    val dish = dishes.find { it.id == dishId }
    val ingredients by viewModel.observeIngredients(dishId).collectAsState()
    var showFinish by remember { mutableStateOf(false) }

    Scaffold(topBar = { TopAppBar(title = { Text(dish?.name ?: "Блюдо") }) }) { padding ->
        if (dish == null) {
            Text("Блюдо не найдено", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Всего: ${dish.totalGrams.toInt()} г")
            Text("Осталось: ${dish.gramsRemaining.toInt()} г")
            Text("Ккал всего: ${dish.totalKcal.toInt()}")
            Text("Состав:", style = MaterialTheme.typography.titleSmall)
            LazyColumn {
                items(ingredients) { ing ->
                    Text("${ing.productName} — ${ing.grams.toInt()} г")
                }
            }
            if (dish.gramsRemaining < dish.totalGrams) {
                Text(
                    "Состав нельзя изменить — блюдо уже частично съедено",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = { showFinish = true }) { Text("Закончилось") }
            TextButton(onClick = { viewModel.deleteDish(dishId); onBack() }) { Text("Удалить блюдо") }
        }
    }

    if (showFinish) {
        AlertDialog(
            onDismissRequest = { showFinish = false },
            title = { Text("Блюдо закончилось?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.finishDish(dishId)
                    showFinish = false
                }) { Text("Да") }
            },
            dismissButton = { TextButton(onClick = { showFinish = false }) { Text("Отмена") } },
        )
    }
}
