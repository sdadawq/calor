package com.calor.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "calor_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dailyGoalKey = intPreferencesKey("daily_goal_kcal")
    private val onboardingKey = booleanPreferencesKey("onboarding_completed")

    val dailyGoalKcal: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[dailyGoalKey] ?: 2000
    }

    val onboardingCompleted: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[onboardingKey] ?: false
    }

    suspend fun setDailyGoal(kcal: Int) {
        context.dataStore.edit { it[dailyGoalKey] = kcal }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[onboardingKey] = completed }
    }

    suspend fun importSettings(dailyGoal: Int, onboarding: Boolean) {
        context.dataStore.edit {
            it[dailyGoalKey] = dailyGoal
            it[onboardingKey] = onboarding
        }
    }
}
