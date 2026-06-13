package com.calor.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calor.app.data.db.entity.FoodLogEntity
import com.calor.app.data.repository.CalorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: CalorRepository,
) : ViewModel() {
    val logs: StateFlow<List<FoodLogEntity>> = repository.observeTodayLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteLog(logId: Long) {
        viewModelScope.launch { repository.deleteFoodLog(logId) }
    }
}
