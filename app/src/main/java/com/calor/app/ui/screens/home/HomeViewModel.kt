package com.calor.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calor.app.data.db.entity.FoodLogEntity
import com.calor.app.data.repository.CalorRepository
import com.calor.app.data.repository.DaySummary
import com.calor.app.data.repository.FavoriteItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: CalorRepository,
) : ViewModel() {
    val daySummary: StateFlow<DaySummary> = repository.observeDaySummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DaySummary(0f, 2000, 2000f))

    val todayLogs: StateFlow<List<FoodLogEntity>> = repository.observeTodayLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteItem>> = repository.observeFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
