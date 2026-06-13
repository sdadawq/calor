package com.calor.app.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calor.app.data.db.entity.ProductEntity
import com.calor.app.domain.model.ProductCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Long?,
    onBack: () -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    var name by remember { mutableStateOf("") }
    var kcal by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ProductCategory.OTHER) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(productId) {
        if (productId != null && productId > 0) {
            viewModel.getProduct(productId)?.let { product ->
                name = product.name
                kcal = product.kcalPer100g.toInt().toString()
                category = ProductCategory.fromString(product.category)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (productId == null || productId == 0L) "Новый продукт" else "Редактировать") })
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = kcal,
                onValueChange = { kcal = it },
                label = { Text("Ккал на 100 г") },
                modifier = Modifier.fillMaxWidth(),
            )
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = category.label,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ProductCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.label) },
                            onClick = {
                                category = cat
                                expanded = false
                            },
                        )
                    }
                }
            }
            TextButton(
                onClick = {
                    val kcalValue = kcal.toFloatOrNull() ?: return@TextButton
                    if (name.isBlank()) return@TextButton
                    viewModel.saveProduct(
                        ProductEntity(
                            id = productId ?: 0L,
                            name = name.trim(),
                            kcalPer100g = kcalValue,
                            category = category.name,
                        ),
                        onBack,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Сохранить") }
        }
    }
}
