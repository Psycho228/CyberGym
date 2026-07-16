@file:Suppress("MagicNumber", "TooManyFunctions")

package com.nextrank.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextrank.core.common.result.Result
import com.nextrank.feature.onboarding.domain.FaceitPlayer
import com.nextrank.feature.onboarding.domain.FaceitRepository
import com.nextrank.feature.onboarding.domain.OnboardingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MAX_FAVORITE_MAPS = 3
private const val MAX_WEAK_SPOTS = 3

class OnboardingViewModel(
    private val onboardingRepository: OnboardingRepository,
    private val faceitRepository: FaceitRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onNicknameChange(value: String) {
        _uiState.update { it.copy(nickname = value.take(MAX_NICKNAME_LENGTH)) }
    }

    fun onGoalSelect(goal: OnboardingGoal) {
        _uiState.update { it.copy(goal = goal) }
    }

    fun onPremierRatingChange(value: String) {
        _uiState.update { it.copy(premierRating = value.filter(Char::isDigit).take(MAX_RATING_LENGTH)) }
    }

    fun onModeToggle(mode: PlayMode) {
        _uiState.update { it.copy(modes = it.modes.toggle(mode)) }
    }

    fun onMapToggle(map: Cs2Map) {
        _uiState.update {
            it.copy(favoriteMaps = it.favoriteMaps.toggleLimited(map, MAX_FAVORITE_MAPS))
        }
    }

    fun onTrainingDurationSelect(duration: TrainingDuration) {
        _uiState.update { it.copy(trainingDuration = duration) }
    }

    fun onTrainingFrequencySelect(frequency: TrainingFrequency) {
        _uiState.update { it.copy(trainingFrequency = frequency) }
    }

    fun onWeakSpotToggle(weakSpot: WeakSpot) {
        _uiState.update {
            it.copy(weakSpots = it.weakSpots.toggleLimited(weakSpot, MAX_WEAK_SPOTS))
        }
    }

    fun onToolToggle(tool: TrainingTool) {
        _uiState.update { it.copy(tools = it.tools.toggle(tool)) }
    }

    fun onFaceitConnectChoice(value: Boolean) {
        if (value) {
            connectFaceit()
        } else {
            _uiState.update {
                it.copy(
                    connectFaceit = false,
                    faceitStatus = FaceitConnectStatus.SKIPPED,
                    faceitError = null,
                )
            }
        }
    }

    fun onFaceitNicknameChange(value: String) {
        _uiState.update {
            it.copy(
                faceitNickname = value,
                faceitError = null,
                faceitStatus = if (it.faceitStatus == FaceitConnectStatus.ERROR) {
                    FaceitConnectStatus.IDLE
                } else {
                    it.faceitStatus
                },
            )
        }
    }

    fun connectFaceit() {
        val state = _uiState.value
        val nickname = state.faceitNickname.ifBlank { state.nickname }.trim()
        if (nickname.isBlank() || state.faceitStatus == FaceitConnectStatus.CONNECTING) return

        _uiState.update {
            it.copy(
                faceitNickname = nickname,
                faceitStatus = FaceitConnectStatus.CONNECTING,
                faceitError = null,
            )
        }
        viewModelScope.launch {
            when (val result = faceitRepository.findPlayer(nickname)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        connectFaceit = true,
                        faceitStatus = FaceitConnectStatus.CONNECTED,
                        faceitPlayer = result.data.toUiModel(),
                        faceitLevel = result.data.skillLevel.toFaceitLevel() ?: it.faceitLevel,
                        faceitError = null,
                    )
                }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        connectFaceit = null,
                        faceitStatus = FaceitConnectStatus.ERROR,
                        faceitError = result.error.message ?: "Не удалось подключить FACEIT",
                    )
                }
            }
        }
    }

    fun onSelfScoreChange(category: SelfScoreCategory, score: Int) {
        _uiState.update {
            it.copy(selfScores = it.selfScores + (category to score.coerceIn(MIN_SELF_SCORE, MAX_SELF_SCORE)))
        }
    }

    fun onComplete() {
        val state = _uiState.value
        if (state.isSaving) return

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = onboardingRepository.saveProfile(state)) {
                is Result.Success -> _uiState.update { it.copy(isSaving = false, isComplete = true) }
                is Result.Failure -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = result.error.message ?: "Не удалось сохранить профиль",
                    )
                }
            }
        }
    }

    private fun <T> Set<T>.toggle(value: T): Set<T> =
        if (value in this) this - value else this + value

    private fun <T> Set<T>.toggleLimited(value: T, limit: Int): Set<T> =
        when {
            value in this -> this - value
            size < limit -> this + value
            else -> this
        }

    private companion object {
        const val MAX_NICKNAME_LENGTH = 40
        const val MAX_RATING_LENGTH = 5
        const val MIN_SELF_SCORE = 1
        const val MAX_SELF_SCORE = 10
    }
}

private fun FaceitPlayer.toUiModel(): ConnectedFaceitPlayer =
    ConnectedFaceitPlayer(
        playerId = playerId,
        nickname = nickname,
        avatar = avatar,
        country = country,
        faceitUrl = faceitUrl,
        game = game,
        gamePlayerId = gamePlayerId,
        skillLevel = skillLevel,
        faceitElo = faceitElo,
    )

private fun Int?.toFaceitLevel(): FaceitLevel? = when (this) {
    null -> null
    in 1..2 -> FaceitLevel.LEVEL_1_2
    in 3..4 -> FaceitLevel.LEVEL_3_4
    in 5..6 -> FaceitLevel.LEVEL_5_6
    in 7..8 -> FaceitLevel.LEVEL_7_8
    in 9..10 -> FaceitLevel.LEVEL_9_10
    else -> null
}
