@file:Suppress("FunctionNaming", "MagicNumber")

package com.nextrank.feature.progress.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import com.nextrank.core.designsystem.component.GamerCircularProgress
import com.nextrank.core.designsystem.component.GamerHeader
import com.nextrank.core.designsystem.component.GamerPanel
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.core.designsystem.component.GamerSecondaryButton
import com.nextrank.core.designsystem.component.GamerStatCard
import com.nextrank.core.designsystem.component.GamerStatRow
import com.nextrank.feature.progress.R
import com.nextrank.feature.progress.domain.AchievementInfo
import com.nextrank.feature.progress.domain.SessionHistoryItem
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    GamerScreen(modifier = modifier) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GamerHeader(
                title = stringResource(R.string.progress_title),
                subtitle = "Регулярность, навыки, рейтинг и достижения в одном журнале без лишних дублей.",
            )

            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    GamerSecondaryButton(text = "Повторить", onClick = viewModel::loadProgress)
                }
                else -> {
                    LevelCycleProgress(uiState)
                    OverallProgress(uiState)
                    SkillsSection(uiState)
                    RatingSection()
                    AchievementsSection(uiState.achievements)
                    RecentSessionsSection(uiState.recentSessions)
                    GamerSecondaryButton(text = "Открыть недельный отчёт", onClick = {})
                    GamerSecondaryButton(text = "Назад", onClick = onBack)
                }
            }
        }
    }
}

@Composable
private fun LevelCycleProgress(state: ProgressUiState) {
    val currentLevelBase = (state.level - 1).coerceAtLeast(0)
    val currentLevelXp = (currentLevelBase * currentLevelBase * 100).toLong()
    val nextLevelXp = (state.level * state.level * 100).toLong()
    val xpInLevel = (state.totalXp - currentLevelXp).coerceAtLeast(0)
    val xpForLevel = (nextLevelXp - currentLevelXp).coerceAtLeast(1)
    val progress = xpInLevel.toFloat() / xpForLevel.toFloat()
    val xpLeft = (nextLevelXp - state.totalXp).coerceAtLeast(0)

    GamerCircularProgress(
        title = "Общий прогресс",
        primaryValue = "LVL ${state.level}",
        subtitle = "$xpLeft XP до следующего уровня",
        progress = progress,
        badgeLabel = "Серия",
        badgeValue = "${state.currentStreak}",
        accent = GamerAccentLime,
        secondaryAccent = GamerAccentOrange,
    )
}

@Composable
private fun OverallProgress(state: ProgressUiState) {
    Text(
        text = "Общий прогресс",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
    )
    GamerStatRow {
        GamerStatCard(
            label = stringResource(R.string.progress_total_trainings),
            value = "${state.totalTrainings}",
            accent = GamerAccentLime,
            modifier = Modifier.weight(1f),
        )
        GamerStatCard(
            label = stringResource(R.string.progress_total_xp),
            value = "${state.totalXp}",
            modifier = Modifier.weight(1f),
        )
    }
    GamerStatRow {
        GamerStatCard(
            label = stringResource(R.string.progress_current_streak),
            value = "${state.currentStreak}",
            accent = GamerAccentOrange,
            modifier = Modifier.weight(1f),
        )
        GamerStatCard(
            label = stringResource(R.string.progress_longest_streak),
            value = "${state.longestStreak}",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SkillsSection(state: ProgressUiState) {
    Text(
        text = "Навыки",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
    )
    val skillRows = listOf(
        SkillRowData("Aim", "развивается", (state.totalTrainings * 0.12f).coerceIn(0.18f, 0.82f), GamerAccentLime),
        SkillRowData("Movement", "стабильно", (state.currentStreak * 0.14f).coerceIn(0.16f, 0.74f), GamerAccentOrange),
        SkillRowData("Game sense", "требует внимания", 0.34f, GamerAccentPink),
    )
    GamerPanel {
        skillRows.forEach { row ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(row.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    GamerChip(text = row.status, accent = row.accent)
                }
                LinearProgressIndicator(
                    progress = { row.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = row.accent,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun RatingSection() {
    Text(
        text = "Рейтинг",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
    )
    GamerPanel(accent = GamerAccentOrange) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("FACEIT / Premier", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = "Рейтинг — шумная метрика. Главный сигнал MVP: регулярность и контроль навыков.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            GamerChip(text = "скоро")
        }
    }
}

@Composable
private fun AchievementsSection(achievements: List<AchievementInfo>) {
    if (achievements.isEmpty()) return
    Text(
        text = stringResource(R.string.progress_achievements),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
    )
    achievements.forEach { achievement ->
        val unlocked = achievement.isUnlocked
        GamerPanel(accent = if (unlocked) GamerAccentLime else MaterialTheme.colorScheme.outline) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GamerChip(
                    text = if (unlocked) "Открыто" else "Закрыто",
                    accent = if (unlocked) GamerAccentLime else MaterialTheme.colorScheme.outline,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(achievement.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                GamerChip(text = "+${achievement.xpReward} XP")
            }
        }
    }
}

@Composable
private fun RecentSessionsSection(sessions: List<SessionHistoryItem>) {
    Text(
        text = stringResource(R.string.progress_recent_sessions),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
    )
    if (sessions.isEmpty()) {
        GamerPanel {
            Text(
                text = stringResource(R.string.progress_no_sessions),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    } else {
        sessions.forEach { session ->
            GamerPanel {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = session.completedAt ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    GamerChip(text = stringResource(R.string.progress_xp_awarded, session.awardedXp))
                }
            }
        }
    }
}

private data class SkillRowData(
    val name: String,
    val status: String,
    val progress: Float,
    val accent: androidx.compose.ui.graphics.Color,
)
