package com.calor.app.domain.model

enum class ProductCategory(val label: String) {
    VEGETABLES("Овощи"),
    MEAT("Мясо"),
    DAIRY("Молочное"),
    GRAINS("Крупы"),
    READY("Готовое"),
    OTHER("Другое");

    companion object {
        fun fromString(value: String): ProductCategory =
            entries.find { it.name == value } ?: OTHER
    }
}

enum class SourceType {
    FRIDGE,
    DISH,
    PRODUCT;

    companion object {
        fun fromString(value: String): SourceType =
            entries.find { it.name == value } ?: PRODUCT
    }
}

enum class MealType(val label: String) {
    BREAKFAST("Завтрак"),
    LUNCH("Обед"),
    DINNER("Ужин"),
    SNACK("Перекус");

    companion object {
        fun fromString(value: String): MealType =
            entries.find { it.name == value } ?: SNACK

        fun guessByTime(hour: Int): MealType = when (hour) {
            in 5..10 -> BREAKFAST
            in 11..15 -> LUNCH
            in 16..21 -> DINNER
            else -> SNACK
        }
    }
}
