# Схема данных Calor

## JSON Backup schemaVersion: 1
products, fridgeItems, dishes, dishIngredients, foodLogs, dailyGoalKcal, onboardingCompleted

## Расчёты
kcal = grams * kcalPer100g / 100
dishKcalEaten = totalKcal * (gramsEaten / totalGrams)
