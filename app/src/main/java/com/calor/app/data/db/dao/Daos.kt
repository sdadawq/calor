package com.calor.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.calor.app.data.db.entity.DishEntity
import com.calor.app.data.db.entity.DishIngredientEntity
import com.calor.app.data.db.entity.FoodLogEntity
import com.calor.app.data.db.entity.FridgeItemEntity
import com.calor.app.data.db.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

data class FridgeItemWithProduct(
    val id: Long,
    val productId: Long,
    val gramsAvailable: Float,
    val addedAt: Long,
    val productName: String,
    val kcalPer100g: Float,
    val category: String,
    val isFavorite: Boolean,
)

data class DishIngredientWithProduct(
    val id: Long,
    val dishId: Long,
    val productId: Long,
    val grams: Float,
    val productName: String,
    val kcalPer100g: Float,
)

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE isFavorite = 1 ORDER BY name ASC")
    fun observeFavorites(): Flow<List<ProductEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM fridge_items WHERE productId = :productId")
    suspend fun countInFridge(productId: Long): Int

    @Query("SELECT COUNT(*) FROM dish_ingredients WHERE productId = :productId")
    suspend fun countInDishes(productId: Long): Int

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}

@Dao
interface FridgeDao {
    @Query(
        """
        SELECT f.id, f.productId, f.gramsAvailable, f.addedAt,
               p.name AS productName, p.kcalPer100g, p.category, p.isFavorite
        FROM fridge_items f
        INNER JOIN products p ON p.id = f.productId
        ORDER BY f.addedAt DESC
        """
    )
    fun observeAll(): Flow<List<FridgeItemWithProduct>>

    @Query(
        """
        SELECT f.id, f.productId, f.gramsAvailable, f.addedAt,
               p.name AS productName, p.kcalPer100g, p.category, p.isFavorite
        FROM fridge_items f
        INNER JOIN products p ON p.id = f.productId
        WHERE p.name LIKE '%' || :query || '%'
        ORDER BY p.name ASC
        """
    )
    fun search(query: String): Flow<List<FridgeItemWithProduct>>

    @Query("SELECT * FROM fridge_items WHERE id = :id")
    suspend fun getById(id: Long): FridgeItemEntity?

    @Query("SELECT * FROM fridge_items WHERE productId = :productId LIMIT 1")
    suspend fun getByProductId(productId: Long): FridgeItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: FridgeItemEntity): Long

    @Update
    suspend fun update(item: FridgeItemEntity)

    @Query("DELETE FROM fridge_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM fridge_items")
    suspend fun deleteAll()
}

@Dao
interface DishDao {
    @Query("SELECT * FROM dishes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<DishEntity>>

    @Query("SELECT * FROM dishes WHERE id = :id")
    suspend fun getById(id: Long): DishEntity?

    @Query("SELECT * FROM dishes WHERE isFavorite = 1 AND gramsRemaining > 0 ORDER BY name ASC")
    fun observeFavoriteWithRemaining(): Flow<List<DishEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(dish: DishEntity): Long

    @Update
    suspend fun update(dish: DishEntity)

    @Query("DELETE FROM dishes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM dishes")
    suspend fun deleteAll()
}

@Dao
interface DishIngredientDao {
    @Query(
        """
        SELECT di.id, di.dishId, di.productId, di.grams,
               p.name AS productName, p.kcalPer100g
        FROM dish_ingredients di
        INNER JOIN products p ON p.id = di.productId
        WHERE di.dishId = :dishId
        """
    )
    fun observeByDish(dishId: Long): Flow<List<DishIngredientWithProduct>>

    @Query("SELECT * FROM dish_ingredients WHERE dishId = :dishId")
    suspend fun getByDish(dishId: Long): List<DishIngredientEntity>

    @Query("SELECT COUNT(*) FROM dish_ingredients WHERE dishId = :dishId")
    suspend fun countByDish(dishId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ingredient: DishIngredientEntity): Long

    @Query("DELETE FROM dish_ingredients WHERE dishId = :dishId")
    suspend fun deleteByDish(dishId: Long)

    @Query("DELETE FROM dish_ingredients")
    suspend fun deleteAll()
}

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs WHERE date = :date ORDER BY loggedAt DESC")
    fun observeByDate(date: String): Flow<List<FoodLogEntity>>

    @Query("SELECT COALESCE(SUM(kcalEaten), 0) FROM food_logs WHERE date = :date")
    fun observeKcalSumByDate(date: String): Flow<Float>

    @Query("SELECT * FROM food_logs WHERE id = :id")
    suspend fun getById(id: Long): FoodLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: FoodLogEntity): Long

    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM food_logs")
    suspend fun deleteAll()
}

@Dao
interface BackupDao {
    @Transaction
    suspend fun clearAll(
        productDao: ProductDao,
        fridgeDao: FridgeDao,
        dishDao: DishDao,
        dishIngredientDao: DishIngredientDao,
        foodLogDao: FoodLogDao,
    ) {
        foodLogDao.deleteAll()
        dishIngredientDao.deleteAll()
        fridgeDao.deleteAll()
        dishDao.deleteAll()
        productDao.deleteAll()
    }
}
