package com.nextrank.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
        selectedGoals = uiState.selectedGoals,
        onNicknameChange = viewModel::onNicknameChange,
        onRankSelect = viewModel::onRankSelect,
        onGoalToggle = viewModel::onGoalToggle,
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
    selectedGoals: Set<PlayerGoal>,
    onNicknameChange: (String) -> Unit,
    onRankSelect: (Cs2Rank?) -> Unit,
    onGoalToggle: (PlayerGoal) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F0F1A),
            Color(0xFF1A1A2E),
            Color(0xFF16213E),
        ),
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(gradient)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header with step indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "🎮",
                fontSize = MaterialTheme.typography.displaySmall.fontSize,
            )
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                ),
                textAlign = TextAlign.Center,
            )
            // Step indicator with neon glow
            Box(
                modifier = Modifier
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF6C63FF).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E1E2E))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "${currentPage + 1} / 3",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8B83FF),
                    ),
                )
            }
        }

        // Content steps
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
                selectedGoals = selectedGoals,
                onGoalToggle = onGoalToggle,
            )
        }

        // Navigation buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Next/Finish button with gradient
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color(0xFF6C63FF).copy(alpha = 0.3f),
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = if (uiState.isSaving) {
                                    listOf(Color(0xFF6C63FF).copy(alpha = 0.5f))
                                } else {
                                    listOf(Color(0xFF6C63FF), Color(0xFF8B83FF))
                                },
                            ),
                            RoundedCornerShape(16.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (uiState.isSaving) {
                            "⚡ Сохранение…"
                        } else if (currentPage == 2) {
                            "🚀 Начать игру"
                        } else {
                            "Далее →"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                    )
                }
            }

            // Error message
            uiState.errorMessage?.let { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(1.dp, Color(0xFFE74C3C).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .background(Color(0xFF1A0F0F), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                ) {
                    Text(
                        text = "⚠️ $message",
                        color = Color(0xFFE74C3C),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Back button
            if (currentPage > 0) {
                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        text = "← Назад",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9999B3),
                        ),
                    )
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
        // Icon and title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "👤",
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
            )
            Text(
                text = stringResource(R.string.onboarding_step_nickname),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                ),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.onboarding_step_nickname_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF9999B3),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Neon input field
        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = {
                Text(
                    stringResource(R.string.nickname_label),
                    color = Color(0xFF8B83FF),
                    fontWeight = FontWeight.Medium,
                )
            },
            placeholder = {
                Text(
                    stringResource(R.string.nickname_placeholder),
                    color = Color(0xFF6B6B8D),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6C63FF),
                unfocusedBorderColor = Color(0xFF2A2A38),
                focusedContainerColor = Color(0xFF1E1E2E),
                unfocusedContainerColor = Color(0xFF1A1A24),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFF8B83FF),
                unfocusedLabelColor = Color(0xFF6B6B8D),
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Icon and title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "🏆",
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
            )
            Text(
                text = stringResource(R.string.onboarding_step_rank),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                ),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.onboarding_step_rank_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF9999B3),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Rank cards
        Cs2Rank.defaultOptions.forEach { rank ->
            RankCard(
                rank = rank,
                isSelected = rank == selectedRank,
                onSelect = { onRankSelect(rank) },
            )
        }
    }
}

@Composable
private fun RankCard(
    rank: Cs2Rank,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val borderColor = if (isSelected) {
        Color(0xFF6C63FF)
    } else {
        Color(0xFF2A2A38)
    }

    val backgroundColor = if (isSelected) {
        Color(0xFF2A2660)
    } else {
        Color(0xFF1E1E2E)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .shadow(
                elevation = if (isSelected) 12.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isSelected) Color(0xFF6C63FF).copy(alpha = 0.4f) else Color.Transparent,
                spotColor = if (isSelected) Color(0xFF6C63FF).copy(alpha = 0.2f) else Color.Transparent,
            )
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        RowWithSpacing(
            horizontalSpace = 16.dp,
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = androidx.compose.material3.RadioButtonDefaults.colors(
                    selectedColor = Color(0xFF6C63FF),
                    unselectedColor = Color(0xFF6B6B8D),
                ),
            )
            Text(
                text = rank.displayName,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF9999B3),
                ),
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
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Icon and title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "🎯",
                fontSize = MaterialTheme.typography.displayMedium.fontSize,
            )
            Text(
                text = stringResource(R.string.onboarding_step_goal),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                ),
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.onboarding_step_goal_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF9999B3),
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Goal cards with checkboxes
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
    val borderColor = if (isSelected) {
        Color(0xFF6C63FF)
    } else {
        Color(0xFF2A2A38)
    }

    val backgroundColor = if (isSelected) {
        Color(0xFF2A2660)
    } else {
        Color(0xFF1E1E2E)
    }

    val icon = when (goal) {
        PlayerGoal.RANK_UP -> "📈"
        PlayerGoal.AIM -> "🎯"
        PlayerGoal.MOVEMENT -> "💨"
        PlayerGoal.SPRAY -> "🔫"
        PlayerGoal.DISCIPLINE -> "🧠"
        PlayerGoal.TEAMPLAY -> "🤝"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                role = Role.Checkbox,
                onClick = onToggle,
            )
            .shadow(
                elevation = if (isSelected) 12.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (isSelected) Color(0xFF6C63FF).copy(alpha = 0.4f) else Color.Transparent,
                spotColor = if (isSelected) Color(0xFF6C63FF).copy(alpha = 0.2f) else Color.Transparent,
            )
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        RowWithSpacing(
            horizontalSpace = 16.dp,
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF6C63FF),
                    uncheckedColor = Color(0xFF6B6B8D),
                ),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "$icon ${goal.displayName}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF9999B3),
                    ),
                )
            }
        }
    }
}

@Composable
private fun RowWithSpacing(
    horizontalSpace: Dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpace),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
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