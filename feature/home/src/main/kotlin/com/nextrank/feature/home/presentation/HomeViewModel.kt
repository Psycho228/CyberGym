package com.nextrank.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.feature.home.domain.HomeRepository
import com.nextrank.core.common.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val homeRepository: HomeRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = homeRepository.loadHome()) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        nickname = result.data.nickname,
                        level = result.data.level,
                        totalXp = result.data.totalXp,
                        streak = result.data.streak,
                        planId = result.data.planId,
                        exerciseCount = result.data.exerciseCount,
                        estimatedMinutes = result.data.estimatedMinutes,
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Не удалось загрузить план на сегодня")
                }
            }
        }
    }
}
