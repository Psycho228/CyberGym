package com.nextrank.feature.onboarding.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.nextrank.feature.onboarding.R
import com.nextrank.domain.model.Cs2Rank
import com.nextrank.domain.model.PlayerGoal

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentPage by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onComplete()
    }

    OnboardingContent(
        uiState = uiState,
        currentPage = currentPage,
        nickname = uiState.nickname,
        selectedRank = uiState.selectedRank,
        selectedGoal = uiState.selectedGoal,
        onNicknameChange = viewModel::onNicknameChange,
        onRankSelect = viewModel::onRankSelect,
        onGoalSelect = viewModel::onGoalSelect,
        onNext = {
            if (currentPage < 2) {
                currentPage += 1
            } else {
                viewModel.onComplete()
            }
        },
        onBack = {
            if (currentPage > 0) {
                currentPage -= 1
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun OnboardingContent(
    uiState: OnboardingUiState,
    currentPage: Int,
    nickname: String,
    selectedRank: Cs2Rank?,
    selectedGoal: PlayerGoal?,
    onNicknameChange: (String) -> Unit,
    onRankSelect: (Cs2Rank?) -> Unit,
    onGoalSelect: (PlayerGoal?) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "${currentPage + 1} / 3",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
        )

        when (currentPage) {
            0 -> NicknameStep(
                nickname = nickname,
                onNicknameChange = onNicknameChange,
            )
            1 -> RankStep(
                selectedRank = selectedRank,
                onRankSelect = onRankSelect,
            )
            2 -> GoalStep(
                selectedGoal = selectedGoal,
                onGoalSelect = onGoalSelect,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text(
                    if (uiState.isSaving) {
                        "Сохранение…"
                    } else if (currentPage == 2) {
                        stringResource(R.string.onboarding_finish)
                    } else {
                        stringResource(R.string.onboarding_next)
                    },
                )
            }

            uiState.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (currentPage > 0) {
                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = true,
                ) {
                    Text(stringResource(R.string.onboarding_back))
                }
            }
        }
    }
}

@Composable
private fun NicknameStep(
    nickname: String,
    onNicknameChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_nickname),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.onboarding_step_nickname_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text(stringResource(R.string.nickname_label)) },
            placeholder = { Text(stringResource(R.string.nickname_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun RankStep(
    selectedRank: Cs2Rank?,
    onRankSelect: (Cs2Rank?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_rank),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.onboarding_step_rank_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Cs2Rank.defaultOptions.forEach { rank ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = rank == selectedRank,
                        role = Role.RadioButton,
                        onClick = { onRankSelect(rank) },
                    ),
                horizontalAlignment = Alignment.Start,
            ) {
                RadioButton(
                    selected = rank == selectedRank,
                    onClick = { onRankSelect(rank) },
                )
                Text(
                    text = rank.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun GoalStep(
    selectedGoal: PlayerGoal?,
    onGoalSelect: (PlayerGoal?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.onboarding_step_goal),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.onboarding_step_goal_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        PlayerGoal.values().forEach { goal ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = goal == selectedGoal,
                        role = Role.RadioButton,
                        onClick = { onGoalSelect(goal) },
                    ),
                horizontalAlignment = Alignment.Start,
            ) {
                RadioButton(
                    selected = goal == selectedGoal,
                    onClick = { onGoalSelect(goal) },
                )
                Text(
                    text = goal.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}

private val PlayerGoal.displayName: String
    get() = when (this) {
        PlayerGoal.RANK_UP -> "Повысить ранг"
        PlayerGoal.AIM -> "Улучшить aim"
        PlayerGoal.MOVEMENT -> "Улучшить movement"
        PlayerGoal.SPRAY -> "Улучшить spray control"
        PlayerGoal.DISCIPLINE -> "Развить дисциплину"
        PlayerGoal.TEAMPLAY -> "Подготовиться к командной игре"
    }
