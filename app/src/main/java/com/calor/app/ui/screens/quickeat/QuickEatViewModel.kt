package com.calor.app.ui.screens.quickeat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calor.app.data.db.dao.FridgeItemWithProduct
import com.calor.app.data.db.entity.DishEntity
import com.calor.app.data.db.entity.ProductEntity
import com.calor.app.data.repository.CalorRepository
import com.calor.app.data.repository.LogFoodRequest
import com.calor.app.domain.model.MealType
import com.calor.app.domain.model.SourceType
import com.calor.app.domain.util.CalorieCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

enum class QuickEatTab { FRIDGE, DISHES, PRODUCT, FAVORITE }

@HiltViewModel
class QuickEatViewModel @Inject constructor(
    private val repository: CalorRepository,
) : ViewModel() {
    private val _tab = MutableStateFlow(QuickEatTab.FRIDGE)
    val tab: StateFlow<QuickEatTab> = _tab.asStateFlow()

    val fridgeItems: StateFlow<List<FridgeItemWithProduct>> = repository.observeFridge()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dishes: StateFlow<List<DishEntity>> = repository.observeDishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<ProductEntity>> = repository.observeProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites = repository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: QuickEatTab) {
        _tab.value = tab
    }

    fun defaultMealType(): MealType =
        MealType.guessByTime(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))

    fun logFromFridge(item: FridgeItemWithProduct, grams: Float, mealType: MealType, onResult: (Result<Unit>) -> Unit) {
        val kcal = CalorieCalculator.kcalForGrams(grams, item.kcalPer100g)
        viewModelScope.launch {
            val result = repository.logFood(
                LogFoodRequest(
                    sourceType = SourceType.FRIDGE,
                    sourceId = item.productId,
                    sourceName = item.productName,
                    grams = grams,
                    kcalEaten = kcal,
                    mealType = mealType,
                    fridgeItemId = item.id,
                )
            )
            onResult(result.map { })
        }
    }

    fun logFromDish(dish: DishEntity, grams: Float, mealType: MealType, onResult: (Result<Unit>) -> Unit) {
        val kcal = CalorieCalculator.dishKcalEaten(dish.totalKcal, dish.totalGrams, grams)
        viewModelScope.launch {
            val result = repository.logFood(
                LogFoodRequest(
                    sourceType = SourceType.DISH,
                    sourceId = dish.id,
                    sourceName = dish.name,
                    grams = grams,
                    kcalEaten = kcal,
                    mealType = mealType,
                )
            )
            onResult(result.map { })
        }
    }

    fun logFromProduct(product: ProductEntity, grams: Float, mealType: MealType, onResult: (Result<Unit>) -> Unit) {
        val kcal = CalorieCalculator.kcalForGrams(grams, product.kcalPer100g)
        viewModelScope.launch {
            val result = repository.logFood(
                LogFoodRequest(
                    sourceType = SourceType.PRODUCT,
                    sourceId = product.id,
                    sourceName = product.name,
                    grams = grams,
                    kcalEaten = kcal,
                    mealType = mealType,
                )
            )
            onResult(result.map { })
        }
    }
}
