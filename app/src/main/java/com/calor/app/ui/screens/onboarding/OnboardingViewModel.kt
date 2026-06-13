package com.calor.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calor.app.data.repository.CalorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: CalorRepository,
) : ViewModel() {
    fun complete(goalKcal: Int, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.completeOnboarding(goalKcal)
            onDone()
        }
    }
}
