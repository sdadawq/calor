package com.calor.app.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calor.app.data.db.entity.ProductEntity
import com.calor.app.data.repository.CalorRepository
import com.calor.app.domain.model.ProductCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val repository: CalorRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val category = MutableStateFlow<ProductCategory?>(null)

    val products: StateFlow<List<ProductEntity>> = combine(query, category) { q, cat ->
        q to cat
    }.flatMapLatest { (q, cat) ->
        if (q.isBlank()) repository.observeProducts() else repository.searchProducts(q)
    }.combine(category) { list, cat ->
        if (cat == null) list else list.filter { it.category == cat.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setCategory(value: ProductCategory?) {
        category.value = value
    }

    fun saveProduct(product: ProductEntity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.upsertProduct(product)
            onDone()
        }
    }

    fun deleteProduct(id: Long, onError: (String) -> Unit, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProduct(id)
                .onSuccess { onDone() }
                .onFailure { onError(it.message ?: "Ошибка удаления") }
        }
    }

    fun toggleFavorite(product: ProductEntity) {
        viewModelScope.launch { repository.toggleProductFavorite(product) }
    }

    suspend fun getProduct(id: Long): ProductEntity? = repository.getProduct(id)
}
