package com.calor.app.ui.screens.quickeat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calor.app.data.db.dao.FridgeItemWithProduct
import com.calor.app.data.db.entity.DishEntity
import com.calor.app.data.db.entity.ProductEntity
import com.calor.app.data.repository.FavoriteItem
import com.calor.app.domain.model.MealType
import com.calor.app.domain.model.SourceType
import com.calor.app.domain.util.CalorieCalculator
import com.calor.app.ui.components.CalorCard
import com.calor.app.ui.theme.CoralPrimary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickEatSheet(
    onDismiss: () -> Unit,
    viewModel: QuickEatViewModel = hiltViewModel(),
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tab by viewModel.tab.collectAsState()
    val fridge by viewModel.fridgeItems.collectAsState()
    val dishes by viewModel.dishes.collectAsState()
    val products by viewModel.products.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    var selectedFridge by remember { mutableStateOf<FridgeItemWithProduct?>(null) }
    var selectedDish by remember { mutableStateOf<DishEntity?>(null) }
    var selectedProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var grams by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(viewModel.defaultMealType()) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Съел что-то", style = MaterialTheme.typography.titleLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickEatTab.entries.forEach { t ->
                    FilterChip(
                        selected = tab == t,
                        onClick = { viewModel.setTab(t) },
                        label = {
                            Text(
                                when (t) {
                                    QuickEatTab.FRIDGE -> "Холодильник"
                                    QuickEatTab.DISHES -> "Блюда"
                                    QuickEatTab.PRODUCT -> "Продукт"
                                    QuickEatTab.FAVORITE -> "Избранное"
                                }
                            )
                        },
                    )
                }
            }
            when (tab) {
                QuickEatTab.FRIDGE -> LazyColumn {
                    items(fridge, key = { it.id }) { item ->
                        CalorCard(onClick = { selectedFridge = item; grams = "100" }) {
                            Text(item.productName, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                QuickEatTab.DISHES -> LazyColumn {
                    items(dishes.filter { it.gramsRemaining > 0 }, key = { it.id }) { dish ->
                        CalorCard(onClick = { selectedDish = dish; grams = "100" }) {
                            Text("${dish.name} (${dish.gramsRemaining.toInt()} г)", modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                QuickEatTab.PRODUCT -> LazyColumn {
                    items(products, key = { it.id }) { product ->
                        CalorCard(onClick = { selectedProduct = product; grams = "100" }) {
                            Text(product.name, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
                QuickEatTab.FAVORITE -> LazyColumn {
                    items(favorites, key = { "${it.sourceType}-${it.id}" }) { fav ->
                        CalorCard(onClick = {
                            when (fav.sourceType) {
                                SourceType.FRIDGE -> selectedFridge = fridge.find { it.productId == fav.id }
                                SourceType.DISH -> selectedDish = dishes.find { it.id == fav.id }
                                SourceType.PRODUCT -> selectedProduct = products.find { it.id == fav.id }
                            }
                            grams = "100"
                        }) {
                            Text(fav.name, modifier = Modifier.padding(12.dp))
                        }
                    }
                }
            }
            if (selectedFridge != null || selectedDish != null || selectedProduct != null) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 100, 150, 200).forEach { preset ->
                        FilterChip(
                            selected = grams == preset.toString(),
                            onClick = { grams = preset.toString() },
                            label = { Text("$preset г") },
                        )
                    }
                }
                OutlinedTextField(
                    value = grams,
                    onValueChange = { grams = it },
                    label = { Text("Граммы") },
                    modifier = Modifier.fillMaxWidth(),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealType.entries.forEach { mt ->
                        FilterChip(
                            selected = mealType == mt,
                            onClick = { mealType = mt },
                            label = { Text(mt.label) },
                        )
                    }
                }
                val g = grams.toFloatOrNull() ?: 0f
                val previewKcal = when {
                    selectedFridge != null -> CalorieCalculator.kcalForGrams(g, selectedFridge!!.kcalPer100g)
                    selectedDish != null -> CalorieCalculator.dishKcalEaten(
                        selectedDish!!.totalKcal, selectedDish!!.totalGrams, g
                    )
                    selectedProduct != null -> CalorieCalculator.kcalForGrams(g, selectedProduct!!.kcalPer100g)
                    else -> 0f
                }
                Text("${g.toInt()} г · ${CalorieCalculator.roundKcal(previewKcal)} ккал")
                Button(
                    onClick = {
                        val onResult: (Result<Unit>) -> Unit = { result ->
                            scope.launch {
                                result.onSuccess {
                                    snackbar.showSnackbar("Отлично, записали!")
                                    onDismiss()
                                }.onFailure {
                                    snackbar.showSnackbar(it.message ?: "Ошибка")
                                }
                            }
                        }
                        when {
                            selectedFridge != null -> viewModel.logFromFridge(selectedFridge!!, g, mealType, onResult)
                            selectedDish != null -> viewModel.logFromDish(selectedDish!!, g, mealType, onResult)
                            selectedProduct != null -> viewModel.logFromProduct(selectedProduct!!, g, mealType, onResult)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                ) { Text("Записать") }
            }
            SnackbarHost(snackbar)
        }
    }
}
