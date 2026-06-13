package com.calor.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calor.app.data.repository.CalorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CalorRepository,
) : ViewModel() {
    val dailyGoal: StateFlow<Int> = repository.observeDailyGoal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)

    fun setDailyGoal(kcal: Int) {
        viewModelScope.launch { repository.setDailyGoal(kcal) }
    }

    suspend fun exportJson(): String = repository.exportBackup()

    fun importJson(json: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            onResult(repository.importBackup(json))
        }
    }
}
