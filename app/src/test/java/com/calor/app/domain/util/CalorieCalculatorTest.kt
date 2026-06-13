package com.calor.app.domain.util

import org.junit.Assert.assertEquals
import org.junit.Test

class CalorieCalculatorTest {

    @Test
    fun kcalForGrams_calculatesCorrectly() {
        assertEquals(165f, CalorieCalculator.kcalForGrams(100f, 165f), 0.01f)
        assertEquals(82.5f, CalorieCalculator.kcalForGrams(50f, 165f), 0.01f)
    }

    @Test
    fun dishKcalEaten_isProportional() {
        assertEquals(250f, CalorieCalculator.dishKcalEaten(500f, 1000f, 500f), 0.01f)
        assertEquals(125f, CalorieCalculator.dishKcalEaten(500f, 1000f, 250f), 0.01f)
    }

    @Test
    fun roundKcal_roundsToInt() {
        assertEquals(243, CalorieCalculator.roundKcal(243.4f))
        assertEquals(244, CalorieCalculator.roundKcal(243.6f))
    }
}
