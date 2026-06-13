package com.calor.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.calor.app.data.db.dao.DishDao
import com.calor.app.data.db.dao.DishIngredientDao
import com.calor.app.data.db.dao.FoodLogDao
import com.calor.app.data.db.dao.FridgeDao
import com.calor.app.data.db.dao.ProductDao
import com.calor.app.data.db.entity.DishEntity
import com.calor.app.data.db.entity.DishIngredientEntity
import com.calor.app.data.db.entity.FoodLogEntity
import com.calor.app.data.db.entity.FridgeItemEntity
import com.calor.app.data.db.entity.ProductEntity

@Database(
    entities = [
        ProductEntity::class,
        FridgeItemEntity::class,
        DishEntity::class,
        DishIngredientEntity::class,
        FoodLogEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun fridgeDao(): FridgeDao
    abstract fun dishDao(): DishDao
    abstract fun dishIngredientDao(): DishIngredientDao
    abstract fun foodLogDao(): FoodLogDao
}
