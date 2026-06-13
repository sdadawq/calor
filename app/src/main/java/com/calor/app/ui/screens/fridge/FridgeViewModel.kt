package com.calor.app.ui.screens.fridge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calor.app.data.db.dao.FridgeItemWithProduct
import com.calor.app.data.repository.CalorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FridgeViewModel @Inject constructor(
    private val repository: CalorRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val items: StateFlow<List<FridgeItemWithProduct>> = query
        .flatMapLatest { q ->
            if (q.isBlank()) repository.observeFridge() else repository.searchFridge(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun addToFridge(productId: Long, grams: Float, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addToFridge(productId, grams)
            onDone()
        }
    }

    fun updateGrams(fridgeItemId: Long, grams: Float) {
        viewModelScope.launch { repository.updateFridgeGrams(fridgeItemId, grams) }
    }

    fun remove(fridgeItemId: Long) {
        viewModelScope.launch { repository.removeFromFridge(fridgeItemId) }
    }
}
