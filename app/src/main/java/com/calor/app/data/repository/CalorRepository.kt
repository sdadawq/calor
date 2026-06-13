package com.calor.app.data.repository

import androidx.room.withTransaction
import com.calor.app.data.backup.BackupData
import com.calor.app.data.backup.toBackup
import com.calor.app.data.backup.toEntity
import com.calor.app.data.db.AppDatabase
import com.calor.app.data.db.dao.DishIngredientWithProduct
import com.calor.app.data.db.dao.FridgeItemWithProduct
import com.calor.app.data.db.entity.DishEntity
import com.calor.app.data.db.entity.DishIngredientEntity
import com.calor.app.data.db.entity.FoodLogEntity
import com.calor.app.data.db.entity.FridgeItemEntity
import com.calor.app.data.db.entity.ProductEntity
import com.calor.app.data.preferences.UserPreferences
import com.calor.app.domain.model.MealType
import com.calor.app.domain.model.SourceType
import com.calor.app.domain.util.CalorieCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class DaySummary(
    val eatenKcal: Float,
    val goalKcal: Int,
    val remainingKcal: Float,
)

data class FavoriteItem(
    val id: Long,
    val name: String,
    val sourceType: SourceType,
    val kcalPer100g: Float? = null,
    val gramsRemaining: Float? = null,
)

data class LogFoodRequest(
    val sourceType: SourceType,
    val sourceId: Long,
    val sourceName: String,
    val grams: Float,
    val kcalEaten: Float,
    val mealType: MealType,
    val fridgeItemId: Long? = null,
)

@Singleton
class CalorRepository @Inject constructor(
    private val database: AppDatabase,
    private val preferences: UserPreferences,
) {
    private val productDao = database.productDao()
    private val fridgeDao = database.fridgeDao()
    private val dishDao = database.dishDao()
    private val dishIngredientDao = database.dishIngredientDao()
    private val foodLogDao = database.foodLogDao()

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun today(): String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    fun observeProducts(): Flow<List<ProductEntity>> = productDao.observeAll()
    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.search(query)
    fun observeFavoriteProducts(): Flow<List<ProductEntity>> = productDao.observeFavorites()

    fun observeFridge(): Flow<List<FridgeItemWithProduct>> = fridgeDao.observeAll()
    fun searchFridge(query: String): Flow<List<FridgeItemWithProduct>> = fridgeDao.search(query)

    fun observeDishes(): Flow<List<DishEntity>> = dishDao.observeAll()
    fun searchDishes(query: String): Flow<List<DishEntity>> = dishDao.search(query)
    fun observeDishIngredients(dishId: Long): Flow<List<DishIngredientWithProduct>> =
        dishIngredientDao.observeByDish(dishId)

    fun observeTodayLogs(): Flow<List<FoodLogEntity>> = foodLogDao.observeByDate(today())

    fun observeDaySummary(): Flow<DaySummary> = combine(
        foodLogDao.observeKcalSumByDate(today()),
        preferences.dailyGoalKcal,
    ) { eaten, goal ->
        DaySummary(eaten, goal, goal - eaten)
    }

    fun observeOnboardingCompleted(): Flow<Boolean> = preferences.onboardingCompleted
    fun observeDailyGoal(): Flow<Int> = preferences.dailyGoalKcal

    fun observeFavorites(): Flow<List<FavoriteItem>> = combine(
        productDao.observeFavorites(),
        dishDao.observeFavoriteWithRemaining(),
    ) { products, dishes ->
        products.map { FavoriteItem(it.id, it.name, SourceType.PRODUCT, it.kcalPer100g) } +
            dishes.map {
                FavoriteItem(
                    it.id,
                    it.name,
                    SourceType.DISH,
                    gramsRemaining = it.gramsRemaining,
                    kcalPer100g = if (it.totalGrams > 0) it.totalKcal / it.totalGrams * 100f else 0f,
                )
            }
    }

    suspend fun getProduct(id: Long): ProductEntity? = productDao.getById(id)
    suspend fun getDish(id: Long): DishEntity? = dishDao.getById(id)
    suspend fun getFridgeItem(id: Long): FridgeItemEntity? = fridgeDao.getById(id)

    suspend fun upsertProduct(product: ProductEntity): Long {
        return if (product.id == 0L) {
            productDao.insert(product)
        } else {
            productDao.update(product)
            product.id
        }
    }

    suspend fun deleteProduct(id: Long): Result<Unit> {
        val inFridge = productDao.countInFridge(id)
        val inDishes = productDao.countInDishes(id)
        if (inFridge > 0 || inDishes > 0) {
            return Result.failure(IllegalStateException("Продукт используется в холодильнике или блюдах"))
        }
        productDao.deleteById(id)
        return Result.success(Unit)
    }

    suspend fun toggleProductFavorite(product: ProductEntity) {
        productDao.update(product.copy(isFavorite = !product.isFavorite))
    }

    suspend fun addToFridge(productId: Long, grams: Float) {
        val existing = fridgeDao.getByProductId(productId)
        if (existing != null) {
            fridgeDao.update(existing.copy(gramsAvailable = existing.gramsAvailable + grams))
        } else {
            fridgeDao.insert(FridgeItemEntity(productId = productId, gramsAvailable = grams))
        }
    }

    suspend fun updateFridgeGrams(fridgeItemId: Long, grams: Float) {
        val item = fridgeDao.getById(fridgeItemId) ?: return
        fridgeDao.update(item.copy(gramsAvailable = grams))
    }

    suspend fun removeFromFridge(fridgeItemId: Long) {
        fridgeDao.deleteById(fridgeItemId)
    }

    suspend fun createDish(name: String, ingredients: List<Pair<Long, Float>>): Long {
        var totalGrams = 0f
        var totalKcal = 0f
        val resolved = ingredients.map { (productId, grams) ->
            val product = productDao.getById(productId)
                ?: throw IllegalArgumentException("Продукт не найден")
            totalGrams += grams
            totalKcal += CalorieCalculator.kcalForGrams(grams, product.kcalPer100g)
            productId to grams
        }
        val dishId = dishDao.insert(
            DishEntity(
                name = name,
                totalGrams = totalGrams,
                totalKcal = totalKcal,
                gramsRemaining = totalGrams,
            )
        )
        resolved.forEach { (productId, grams) ->
            dishIngredientDao.insert(DishIngredientEntity(dishId = dishId, productId = productId, grams = grams))
        }
        return dishId
    }

    suspend fun toggleDishFavorite(dish: DishEntity) {
        dishDao.update(dish.copy(isFavorite = !dish.isFavorite))
    }

    suspend fun finishDish(dishId: Long) {
        val dish = dishDao.getById(dishId) ?: return
        dishDao.update(dish.copy(gramsRemaining = 0f))
    }

    suspend fun deleteDish(dishId: Long) {
        dishIngredientDao.deleteByDish(dishId)
        dishDao.deleteById(dishId)
    }

    suspend fun logFood(request: LogFoodRequest): Result<Long> {
        if (request.grams <= 0f) {
            return Result.failure(IllegalArgumentException("Укажите количество граммов"))
        }
        when (request.sourceType) {
            SourceType.FRIDGE -> {
                val fridgeItem = fridgeDao.getById(request.fridgeItemId ?: request.sourceId)
                    ?: return Result.failure(IllegalStateException("Позиция не найдена в холодильнике"))
                if (request.grams > fridgeItem.gramsAvailable) {
                    return Result.failure(IllegalStateException("Кажется, это больше, чем есть в холодильнике"))
                }
                fridgeDao.update(fridgeItem.copy(gramsAvailable = fridgeItem.gramsAvailable - request.grams))
            }
            SourceType.DISH -> {
                val dish = dishDao.getById(request.sourceId)
                    ?: return Result.failure(IllegalStateException("Блюдо не найдено"))
                if (request.grams > dish.gramsRemaining) {
                    return Result.failure(IllegalStateException("Недостаточно остатка блюда"))
                }
                dishDao.update(dish.copy(gramsRemaining = dish.gramsRemaining - request.grams))
            }
            SourceType.PRODUCT -> Unit
        }
        val logId = foodLogDao.insert(
            FoodLogEntity(
                date = today(),
                sourceType = request.sourceType.name,
                sourceId = request.sourceId,
                sourceName = request.sourceName,
                gramsEaten = request.grams,
                kcalEaten = request.kcalEaten,
                mealType = request.mealType.name,
                fridgeItemId = request.fridgeItemId,
            )
        )
        return Result.success(logId)
    }

    suspend fun deleteFoodLog(logId: Long): Result<Unit> {
        val log = foodLogDao.getById(logId) ?: return Result.failure(IllegalStateException("Запись не найдена"))
        when (SourceType.fromString(log.sourceType)) {
            SourceType.FRIDGE -> {
                val fridgeId = log.fridgeItemId
                if (fridgeId != null) {
                    val item = fridgeDao.getById(fridgeId)
                    if (item != null) {
                        fridgeDao.update(item.copy(gramsAvailable = item.gramsAvailable + log.gramsEaten))
                    }
                }
            }
            SourceType.DISH -> {
                val dish = dishDao.getById(log.sourceId)
                if (dish != null) {
                    dishDao.update(dish.copy(gramsRemaining = dish.gramsRemaining + log.gramsEaten))
                }
            }
            SourceType.PRODUCT -> Unit
        }
        foodLogDao.deleteById(logId)
        return Result.success(Unit)
    }

    suspend fun setDailyGoal(kcal: Int) = preferences.setDailyGoal(kcal)
    suspend fun completeOnboarding(goalKcal: Int) {
        preferences.setDailyGoal(goalKcal)
        preferences.setOnboardingCompleted(true)
    }

    suspend fun exportBackup(): String {
        val products = productDao.observeAll().first()
        val fridge = fridgeDao.observeAll().first().map {
            FridgeItemEntity(it.id, it.productId, it.gramsAvailable, it.addedAt)
        }
        val dishes = dishDao.observeAll().first()
        val ingredients = dishes.flatMap { dish ->
            dishIngredientDao.getByDish(dish.id)
        }
        val backup = BackupData(
            dailyGoalKcal = preferences.dailyGoalKcal.first(),
            onboardingCompleted = preferences.onboardingCompleted.first(),
            products = products.map { it.toBackup() },
            fridgeItems = fridge.map { it.toBackup() },
            dishes = dishes.map { it.toBackup() },
            dishIngredients = ingredients.map { it.toBackup() },
            foodLogs = getAllFoodLogs().map { it.toBackup() },
        )
        return json.encodeToString(backup)
    }

    private suspend fun getAllFoodLogs(): List<FoodLogEntity> {
        // Collect from last 365 days as practical export scope
        val logs = mutableListOf<FoodLogEntity>()
        var date = LocalDate.now()
        repeat(365) {
            logs += foodLogDao.observeByDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE)).first()
            date = date.minusDays(1)
        }
        return logs.distinctBy { it.id }
    }

    suspend fun importBackup(jsonString: String): Result<Unit> = runCatching {
        val backup = json.decodeFromString<BackupData>(jsonString)
        require(backup.schemaVersion == BackupData.SCHEMA_VERSION) { "Неподдерживаемая версия схемы" }
        database.withTransaction {
            foodLogDao.deleteAll()
            dishIngredientDao.deleteAll()
            fridgeDao.deleteAll()
            dishDao.deleteAll()
            productDao.deleteAll()
            backup.products.forEach { productDao.insert(it.toEntity()) }
            backup.fridgeItems.forEach { fridgeDao.insert(it.toEntity()) }
            backup.dishes.forEach { dishDao.insert(it.toEntity()) }
            backup.dishIngredients.forEach { dishIngredientDao.insert(it.toEntity()) }
            backup.foodLogs.forEach { foodLogDao.insert(it.toEntity()) }
        }
        preferences.importSettings(backup.dailyGoalKcal, backup.onboardingCompleted)
    }
}
