@file:Suppress("FunctionNaming", "LongParameterList", "MagicNumber", "TooManyFunctions")

package com.nextrank.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.nextrank.core.designsystem.component.GamerStatCard
import com.nextrank.core.designsystem.component.GamerStatRow
import com.nextrank.feature.home.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToTraining: (String) -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        onTrainingClick = { uiState.planId?.let(onNavigateToTraining) },
        onProgressClick = onNavigateToProgress,
        onProfileClick = onNavigateToProfile,
        onLogoutClick = onLogout,
        onRetryClick = viewModel::loadHomeData,
        modifier = modifier,
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onTrainingClick: () -> Unit,
    onProgressClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GamerScreen(modifier = modifier) {
        when {
            uiState.isLoading -> HomeLoading()
            uiState.errorMessage != null -> {
                GamerHeader(
                    title = "План недоступен",
                    subtitle = "Не смогли загрузить данные игрока и тренировку дня. Попробуй обновить экран.",
                )
                HomeError(uiState.errorMessage, onRetryClick)
            }
            else -> {
                HomeHeader(uiState.nickname)
                TodaySummary(uiState)
                TodayTrainingCard(uiState, onTrainingClick)
                TaskPreview(uiState)
                MatchFocusCard()
                AssistantCard()
                QuickActions(onProgressClick, onProfileClick, onLogoutClick)
            }
        }
    }
}

@Composable
private fun HomeLoading() {
    GamerHeader(
        title = "Загружаем план",
        subtitle = "Собираем твой профиль, тренировку дня и match focus.",
    )
    CircularProgressIndicator(Modifier.fillMaxWidth())
}

@Composable
private fun HomeHeader(nickname: String) {
    GamerHeader(
        title = stringResource(R.string.home_welcome, nickname),
        subtitle = "Открой план дня, забери короткую тренировку и перенеси один фокус в следующий матч.",
    )
}

@Composable
private fun HomeError(
    message: String?,
    onRetryClick: () -> Unit,
) {
    GamerPanel(accent = GamerAccentOrange) {
        Text(
            text = message ?: "Не удалось загрузить план на сегодня",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
        )
        GamerSecondaryButton(text = "Повторить", onClick = onRetryClick)
    }
}

@Composable
private fun TodaySummary(state: HomeUiState) {
    GamerStatRow {
        GamerStatCard(
            label = "День трека",
            value = "#${state.streak + 1}",
            accent = GamerAccentLime,
            modifier = Modifier.weight(1f),
        )
        GamerStatCard(
            label = stringResource(R.string.home_streak),
            value = "${state.streak}",
            accent = GamerAccentOrange,
            modifier = Modifier.weight(1f),
        )
        GamerStatCard(
            label = stringResource(R.string.home_level),
            value = "${state.level}",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun TodayTrainingCard(
    state: HomeUiState,
    onTrainingClick: () -> Unit,
) {
    val hasPlan = state.planId != null
    val minutes = state.estimatedMinutes.takeIf { it > 0 } ?: 20
    val exerciseCount = state.exerciseCount.takeIf { it > 0 } ?: 3

    GamerPanel(accent = GamerAccentPink) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_daily_plan),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = if (hasPlan) {
                        stringResource(R.string.home_daily_plan_desc, exerciseCount, minutes)
                    } else {
                        "Профиль загружен, но план дня пока не сгенерировался. Можно обновить экран или перейти в трек."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GamerChip(text = "$minutes мин")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GamerChip(text = "Aim", accent = GamerAccentLime)
            GamerChip(text = "Movement")
            GamerChip(text = "Focus", accent = GamerAccentPink)
        }
        GamerPrimaryButton(
            text = if (hasPlan) stringResource(R.string.home_start) else "План пока не готов",
            onClick = onTrainingClick,
            enabled = hasPlan,
        )
    }
}

@Composable
private fun TaskPreview(state: HomeUiState) {
    if (state.planId == null) return

    GamerPanel {
        Text(
            text = "Сегодня в программе",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        val count = state.exerciseCount.coerceAtLeast(3)
        listOf(
            "Разогрев механики · 5 мин",
            "Главный drill дня · ${state.estimatedMinutes.coerceAtLeast(20) - 10} мин",
            "Контрольный перенос в матч · 5 мин",
        ).take(count.coerceAtMost(3)).forEachIndexed { index, task ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${index + 1}. $task",
                    style = MaterialTheme.typography.bodyMedium,
                )
                GamerChip(text = if (index == 0) "не начато" else "план")
            }
        }
    }
}

@Composable
private fun MatchFocusCard() {
    GamerPanel(accent = GamerAccentOrange) {
        Text(
            text = "Match Focus",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = "Перед каждым первым выстрелом проверяй полную остановку персонажа. Один фокус — один матч.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GamerSecondaryButton(text = "Возьму в следующий матч", onClick = {})
    }
}

@Composable
private fun AssistantCard() {
    GamerPanel(accent = GamerAccentLime) {
        Text(
            text = "AI-ассистент",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Быстрые сценарии для MVP: сократить до 15 минут, объяснить тренировку или заменить упражнение.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GamerChip(text = "15 мин")
            GamerChip(text = "Объясни")
            GamerChip(text = "Замени")
        }
    }
}

@Composable
private fun QuickActions(
    onProgressClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
) {
    GamerPanel {
        Text(
            text = "Быстрый доступ",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        GamerSecondaryButton(text = stringResource(R.string.home_progress), onClick = onProgressClick)
        GamerSecondaryButton(text = stringResource(R.string.home_profile), onClick = onProfileClick)
        GamerSecondaryButton(text = stringResource(R.string.home_logout), onClick = onLogoutClick)
    }
}
