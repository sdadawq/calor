package com.calor.app.ui.screens.dishes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calor.app.data.db.dao.DishIngredientWithProduct
import com.calor.app.data.db.entity.DishEntity
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
class DishesViewModel @Inject constructor(
    private val repository: CalorRepository,
) : ViewModel() {
    private val query = MutableStateFlow("")

    val dishes: StateFlow<List<DishEntity>> = query
        .flatMapLatest { q ->
            if (q.isBlank()) repository.observeDishes() else repository.searchDishes(q)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(value: String) {
        query.value = value
    }

    fun createDish(name: String, ingredients: List<Pair<Long, Float>>, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.createDish(name, ingredients)
            onDone()
        }
    }

    fun toggleFavorite(dish: DishEntity) {
        viewModelScope.launch { repository.toggleDishFavorite(dish) }
    }

    fun finishDish(dishId: Long) {
        viewModelScope.launch { repository.finishDish(dishId) }
    }

    fun deleteDish(dishId: Long) {
        viewModelScope.launch { repository.deleteDish(dishId) }
    }

    fun observeIngredients(dishId: Long): StateFlow<List<DishIngredientWithProduct>> =
        repository.observeDishIngredients(dishId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
