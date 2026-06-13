package com.calor.app.data.backup

import com.calor.app.data.db.entity.DishEntity
import com.calor.app.data.db.entity.DishIngredientEntity
import com.calor.app.data.db.entity.FoodLogEntity
import com.calor.app.data.db.entity.FridgeItemEntity
import com.calor.app.data.db.entity.ProductEntity
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val schemaVersion: Int = SCHEMA_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val dailyGoalKcal: Int = 2000,
    val onboardingCompleted: Boolean = true,
    val products: List<BackupProduct> = emptyList(),
    val fridgeItems: List<BackupFridgeItem> = emptyList(),
    val dishes: List<BackupDish> = emptyList(),
    val dishIngredients: List<BackupDishIngredient> = emptyList(),
    val foodLogs: List<BackupFoodLog> = emptyList(),
) {
    companion object {
        const val SCHEMA_VERSION = 1
    }
}

@Serializable
data class BackupProduct(
    val id: Long,
    val name: String,
    val kcalPer100g: Float,
    val category: String,
    val isFavorite: Boolean,
)

@Serializable
data class BackupFridgeItem(
    val id: Long,
    val productId: Long,
    val gramsAvailable: Float,
    val addedAt: Long,
)

@Serializable
data class BackupDish(
    val id: Long,
    val name: String,
    val totalGrams: Float,
    val totalKcal: Float,
    val gramsRemaining: Float,
    val isFavorite: Boolean,
    val createdAt: Long,
)

@Serializable
data class BackupDishIngredient(
    val id: Long,
    val dishId: Long,
    val productId: Long,
    val grams: Float,
)

@Serializable
data class BackupFoodLog(
    val id: Long,
    val date: String,
    val sourceType: String,
    val sourceId: Long,
    val sourceName: String,
    val gramsEaten: Float,
    val kcalEaten: Float,
    val mealType: String,
    val fridgeItemId: Long? = null,
    val loggedAt: Long,
)

fun ProductEntity.toBackup() = BackupProduct(id, name, kcalPer100g, category, isFavorite)
fun FridgeItemEntity.toBackup() = BackupFridgeItem(id, productId, gramsAvailable, addedAt)
fun DishEntity.toBackup() = BackupDish(id, name, totalGrams, totalKcal, gramsRemaining, isFavorite, createdAt)
fun DishIngredientEntity.toBackup() = BackupDishIngredient(id, dishId, productId, grams)
fun FoodLogEntity.toBackup() = BackupFoodLog(
    id, date, sourceType, sourceId, sourceName, gramsEaten, kcalEaten, mealType, fridgeItemId, loggedAt
)

fun BackupProduct.toEntity() = ProductEntity(id, name, kcalPer100g, category, isFavorite)
fun BackupFridgeItem.toEntity() = FridgeItemEntity(id, productId, gramsAvailable, addedAt)
fun BackupDish.toEntity() = DishEntity(id, name, totalGrams, totalKcal, gramsRemaining, isFavorite, createdAt)
fun BackupDishIngredient.toEntity() = DishIngredientEntity(id, dishId, productId, grams)
fun BackupFoodLog.toEntity() = FoodLogEntity(
    id, date, sourceType, sourceId, sourceName, gramsEaten, kcalEaten, mealType, fridgeItemId, loggedAt
)
