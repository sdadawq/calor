package com.calor.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kcalPer100g: Float,
    val category: String,
    val isFavorite: Boolean = false,
)

@Entity(tableName = "fridge_items")
data class FridgeItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,
    val gramsAvailable: Float,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "dishes")
data class DishEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val totalGrams: Float,
    val totalKcal: Float,
    val gramsRemaining: Float,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "dish_ingredients")
data class DishIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dishId: Long,
    val productId: Long,
    val grams: Float,
)

@Entity(tableName = "food_logs")
data class FoodLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val sourceType: String,
    val sourceId: Long,
    val sourceName: String,
    val gramsEaten: Float,
    val kcalEaten: Float,
    val mealType: String,
    val fridgeItemId: Long? = null,
    val loggedAt: Long = System.currentTimeMillis(),
)
