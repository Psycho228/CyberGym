package com.nextrank.feature.onboarding.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nextrank.core.designsystem.component.GamerAccentLime
import com.nextrank.core.designsystem.component.GamerAccentOrange
import com.nextrank.core.designsystem.component.GamerAccentPink
import com.nextrank.core.designsystem.component.GamerChip
import com.nextrank.core.designsystem.component.GamerHeader
import com.nextrank.core.designsystem.component.GamerPanel
import com.nextrank.core.designsystem.component.GamerPrimaryButton
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.core.designsystem.component.GamerSecondaryButton
import com.nextrank.domain.model.Cs2Rank
import com.nextrank.domain.model.PlayerGoal
import com.nextrank.feature.onboarding.R
import org.koin.compose.viewmodel.koinViewModel

private const val LastPage = 2

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
        selectedGoals = uiState.selectedGoals,
        onNicknameChange = viewModel::onNicknameChange,
        onRankSelect = viewModel::onRankSelect,
        onGoalToggle = viewModel::onGoalToggle,
        onNext = {
            if (currentPage < LastPage) {
                currentPage += 1
            } else {
                viewModel.onComplete()
            }
        },
        onBack = {
            if (currentPage > 0) currentPage -= 1
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
    selectedGoals: Set<PlayerGoal>,
    onNicknameChange: (String) -> Unit,
    onRankSelect: (Cs2Rank?) -> Unit,
    onGoalToggle: (PlayerGoal) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GamerScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GamerHeader(
                title = stringResource(R.string.onboarding_title),
                subtitle = "Настроим профиль, чтобы тренировки ощущались как персональные миссии.",
            )
            StepHud(currentPage = currentPage)

            when (currentPage) {
                0 -> NicknameStep(nickname = nickname, onNicknameChange = onNicknameChange)
                1 -> RankStep(selectedRank = selectedRank, onRankSelect = onRankSelect)
                2 -> GoalStep(selectedGoals = selectedGoals, onGoalToggle = onGoalToggle)
            }

            uiState.errorMessage?.let { message ->
                GamerPanel(accent = MaterialTheme.colorScheme.error) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        GamerPrimaryButton(
            text = when {
                uiState.isSaving -> "Сохранение..."
                currentPage == LastPage -> "Начать игру"
                else -> stringResource(R.string.onboarding_next)
            },
            onClick = onNext,
            enabled = !uiState.isSaving,
        )
        if (currentPage > 0) {
            GamerSecondaryButton(text = stringResource(R.string.onboarding_back), onClick = onBack)
        }
    }
}

@Composable
private fun StepHud(currentPage: Int) {
    GamerPanel(accent = GamerAccentPink) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Шаг ${currentPage + 1} из 3",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
            )
            GamerChip(text = "${((currentPage + 1) * 100) / 3}% готово", accent = GamerAccentLime)
        }
    }
}

@Composable
private fun NicknameStep(
    nickname: String,
    onNicknameChange: (String) -> Unit,
) {
    GamerPanel {
        Text(
            text = stringResource(R.string.onboarding_step_nickname),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
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
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
            ),
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
        GamerPanel(accent = GamerAccentOrange) {
            Text(
                text = stringResource(R.string.onboarding_step_rank),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.onboarding_step_rank_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Cs2Rank.defaultOptions.forEach { rank ->
            RankCard(rank = rank, isSelected = rank == selectedRank, onSelect = { onRankSelect(rank) })
        }
    }
}

@Composable
private fun RankCard(
    rank: Cs2Rank,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    GamerPanel(
        modifier = Modifier.clickable(onClick = onSelect),
        accent = if (isSelected) GamerAccentLime else MaterialTheme.colorScheme.outline,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = GamerAccentLime,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Text(
                text = rank.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GoalStep(
    selectedGoals: Set<PlayerGoal>,
    onGoalToggle: (PlayerGoal) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GamerPanel(accent = GamerAccentPink) {
            Text(
                text = stringResource(R.string.onboarding_step_goal),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.onboarding_step_goal_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PlayerGoal.values().forEach { goal ->
            GoalCard(
                goal = goal,
                isSelected = goal in selectedGoals,
                onToggle = { onGoalToggle(goal) },
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: PlayerGoal,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    GamerPanel(
        modifier = Modifier.clickable(onClick = onToggle),
        accent = if (isSelected) GamerAccentLime else MaterialTheme.colorScheme.outline,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = GamerAccentLime,
                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
            Text(
                text = goal.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
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
