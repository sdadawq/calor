package com.calor.app.domain.util

import kotlin.math.round

object CalorieCalculator {
    fun kcalForGrams(grams: Float, kcalPer100g: Float): Float =
        grams * kcalPer100g / 100f

    fun dishKcalEaten(totalKcal: Float, totalGrams: Float, gramsEaten: Float): Float {
        if (totalGrams <= 0f) return 0f
        return totalKcal * (gramsEaten / totalGrams)
    }

    fun roundKcal(value: Float): Int = round(value).toInt()
}
