package com.calor.app.ui.screens.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calor.app.data.db.entity.ProductEntity
import com.calor.app.domain.model.ProductCategory
import com.calor.app.ui.components.CalorCard
import com.calor.app.ui.components.EmptyState
import com.calor.app.ui.components.SearchField
import com.calor.app.ui.theme.CoralPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val products by viewModel.products.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Продукты") }) },
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
            SearchField(query, { query = it; viewModel.setQuery(it) }, "Поиск продуктов")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("Все") },
                    )
                }
                items(ProductCategory.entries) { cat ->
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.setCategory(cat) },
                        label = { Text(cat.label) },
                    )
                }
            }
            if (products.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.ShoppingBasket,
                    title = "Пока нет продуктов",
                    subtitle = "Добавь продукты, чтобы начать считать калории",
                    actionLabel = "Добавить продукт",
                    onAction = onAdd,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onEdit = { onEdit(product.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(product) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductEntity,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    CalorCard(onClick = onEdit) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${product.kcalPer100g.toInt()} ккал / 100 г",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (product.isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = "Избранное",
                    tint = if (product.isFavorite) CoralPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
