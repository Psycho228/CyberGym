@file:Suppress("FunctionNaming", "TooManyFunctions", "LongMethod", "MaxLineLength", "MagicNumber")

package com.nextrank.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.core.designsystem.component.GamerSecondaryButton
import com.nextrank.core.designsystem.component.GamerStatCard
import com.nextrank.core.designsystem.component.GamerStatRow
import com.nextrank.feature.profile.R
import com.nextrank.feature.profile.domain.FaceitProfileStats
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    GamerScreen(modifier = modifier) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GamerHeader(
                title = stringResource(R.string.profile_title),
                subtitle = "FACEIT-данные, игровой профиль, режим тренировок и настройки CyberGym.",
            )

            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    GamerSecondaryButton(text = "Повторить", onClick = viewModel::loadProfile)
                }
                else -> {
                    PlayerCard(state = uiState)
                    FaceitSection(
                        state = uiState,
                        onRefreshFaceitStats = viewModel::refreshFaceitStats,
                    )
                    GameProfileSection(uiState)
                    TrainingSettingsSection(uiState)
                    ProfileSettingsSection()
                    AppSettingsSection(onLogout)
                    GamerSecondaryButton(text = "Назад", onClick = onBack)
                }
            }
        }
    }
}

@Composable
private fun PlayerCard(state: ProfileUiState) {
    GamerPanel(accent = GamerAccentLime) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = state.nickname.ifBlank { "CyberGym Player" },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = "Ваш игровой профиль CyberGym",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GamerChip(text = "LVL ${state.level}", accent = GamerAccentLime)
        }
        GamerStatRow {
            GamerStatCard(
                label = "XP",
                value = "${state.totalXp}",
                modifier = Modifier.weight(1f),
            )
            GamerStatCard(
                label = "Серия",
                value = "${state.currentStreak}",
                accent = GamerAccentOrange,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FaceitSection(
    state: ProfileUiState,
    onRefreshFaceitStats: () -> Unit,
) {
    val faceit = state.faceit

    ProfileSection(title = "FACEIT", accent = GamerAccentPink) {
        if (faceit == null) {
            Text(
                text = "FACEIT пока не подключён. Подключение можно повторить через настройки профиля.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            GamerSecondaryButton(text = "Подключить FACEIT", onClick = {})
            return@ProfileSection
        }

        GamerSecondaryButton(
            text = if (state.isFaceitStatsRefreshing) {
                "Обновляем..."
            } else {
                "Обновить FACEIT статистику"
            },
            onClick = onRefreshFaceitStats,
            enabled = !state.isFaceitStatsRefreshing,
        )
        state.faceitStatsError?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        GamerStatRow {
            GamerStatCard(
                label = "ELO",
                value = faceit.faceitElo?.toString() ?: "—",
                accent = GamerAccentLime,
                modifier = Modifier.weight(1f),
            )
            GamerStatCard(
                label = "Level",
                value = faceit.skillLevel?.toString() ?: "—",
                accent = GamerAccentOrange,
                modifier = Modifier.weight(1f),
            )
        }
        GamerStatRow {
            GamerStatCard(
                label = "K/D",
                value = faceit.averageKd ?: "—",
                modifier = Modifier.weight(1f),
            )
            GamerStatCard(
                label = "Winrate",
                value = faceit.winRate ?: "—",
                modifier = Modifier.weight(1f),
            )
        }

        ProfileRow(label = "FACEIT ник", value = faceit.nickname ?: "—")
        ProfileRow(label = "Steam/Game ID", value = faceit.gamePlayerId ?: "—")
        ProfileRow(label = "Страна", value = faceit.country ?: "—")
        ProfileRow(label = "Матчей", value = faceit.matches?.toString() ?: "после синхронизации")
        ProfileRow(label = "Headshots", value = faceit.headshots ?: "после синхронизации")
        ProfileRow(label = "Игровой диапазон", value = faceit.skillBand())
        ProfileRow(label = "Тренировочный фокус", value = faceit.trainingFocus())
        Text(
            text = "Обновление FACEIT помогает CyberGym точнее выбирать фокус: aim, стабильность, дисциплина или перенос навыка в матч.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GameProfileSection(state: ProfileUiState) {
    ProfileSection(title = "Игровой профиль", accent = GamerAccentPink) {
        ProfileRow(
            label = stringResource(R.string.profile_rank),
            value = state.currentRank ?: stringResource(R.string.profile_rank_unknown),
        )
        ProfileRow(
            label = stringResource(R.string.profile_goal),
            value = state.primaryGoal ?: stringResource(R.string.profile_goal_unknown),
        )
        ProfileRow(
            label = "Связанный FACEIT",
            value = if (state.faceit != null) "подключён" else "не подключён",
        )
        ProfileRow(
            label = "Любимые карты",
            value = state.favoriteMaps.takeIf(List<String>::isNotEmpty)?.joinToString(", ") ?: "не указаны",
        )
        ProfileRow(
            label = "Слабые места",
            value = state.weakSpots.takeIf(List<String>::isNotEmpty)?.joinToString(", ") ?: "не указаны",
        )
    }
}

@Composable
private fun TrainingSettingsSection(state: ProfileUiState) {
    ProfileSection(title = "Настройки тренировок", accent = GamerAccentOrange) {
        ProfileRow(
            label = stringResource(R.string.profile_daily_minutes),
            value = "${state.dailyMinutes} мин",
        )
        ProfileRow(
            label = "Тренировочных дней",
            value = state.trainingFrequencyDays?.toString() ?: "не указано",
        )
        ProfileRow(label = "Напоминания", value = "выключены")
        ProfileRow(label = "Лучший стрик", value = "${state.longestStreak}")
        GamerSecondaryButton(text = "Настроить расписание", onClick = {})
    }
}

@Composable
private fun ProfileSettingsSection() {
    ProfileSection(title = "Настройка профиля", accent = GamerAccentLime) {
        Text(
            text = "Здесь будут редактироваться ник, цель, карты, слабые места, длительность занятий и повторное подключение FACEIT.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        GamerSecondaryButton(text = "Изменить ник", onClick = {})
        GamerSecondaryButton(text = "Изменить цель и карты", onClick = {})
        GamerSecondaryButton(text = "Переподключить FACEIT", onClick = {})
    }
}

@Composable
private fun AppSettingsSection(onLogout: () -> Unit) {
    ProfileSection(title = "Приложение") {
        ProfileRow(label = "Уведомления", value = "скоро")
        ProfileRow(label = "Язык", value = "Русский")
        ProfileRow(label = "Версия", value = "MVP")
        GamerSecondaryButton(text = stringResource(R.string.profile_logout), onClick = onLogout)
    }
}

private fun FaceitProfileStats.skillBand(): String =
    when (skillLevel) {
        null -> "после синхронизации"
        in 1..3 -> "база и стабильность"
        in 4..6 -> "средний уровень"
        in 7..8 -> "продвинутый"
        else -> "high elo"
    }

private fun FaceitProfileStats.trainingFocus(): String {
    val kd = averageKd?.replace(",", ".")?.toDoubleOrNull()
    val winRateValue = winRate
        ?.removeSuffix("%")
        ?.replace(",", ".")
        ?.toDoubleOrNull()

    return when {
        kd != null && kd < 0.9 -> "выживаемость и дуэли"
        kd != null && kd >= 1.2 && winRateValue != null && winRateValue < 50.0 -> "impact и teamplay"
        winRateValue != null && winRateValue < 48.0 -> "стабильность решений"
        winRateValue != null && winRateValue >= 55.0 -> "закрепить сильные стороны"
        else -> "aim + consistency"
    }
}

@Composable
private fun ProfileSection(
    title: String,
    accent: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    GamerPanel(accent = accent) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
        )
        content()
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
