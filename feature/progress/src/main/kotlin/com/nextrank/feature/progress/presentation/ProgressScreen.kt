package com.nextrank.feature.progress.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.nextrank.feature.progress.R

@Composable
fun ProgressScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.progress_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
            uiState.errorMessage != null -> {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                )
                OutlinedButton(
                    onClick = viewModel::loadProgress,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Повторить") }
            }
            else -> {
                StatsGrid(uiState)

                AchievementsSection(uiState.achievements)

                RecentSessionsSection(uiState.recentSessions)

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Назад") }
            }
        }
    }
}

@Composable
private fun StatsGrid(state: ProgressUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            label = stringResource(R.string.progress_level),
            value = "${state.level}",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.progress_total_xp),
            value = "${state.totalXp}",
            modifier = Modifier.weight(1f),
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            label = stringResource(R.string.progress_current_streak),
            value = "${state.currentStreak}",
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = stringResource(R.string.progress_longest_streak),
            value = "${state.longestStreak}",
            modifier = Modifier.weight(1f),
        )
    }
    StatCard(
        label = stringResource(R.string.progress_total_trainings),
        value = "${state.totalTrainings}",
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AchievementsSection(achievements: List<com.nextrank.feature.progress.domain.AchievementInfo>) {
    if (achievements.isEmpty()) return
    Text(
        text = stringResource(R.string.progress_achievements),
        style = MaterialTheme.typography.titleLarge,
    )
    achievements.forEach { achievement ->
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (achievement.isUnlocked) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (achievement.isUnlocked) "✓" else "🔒",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (achievement.isUnlocked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "+${achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun RecentSessionsSection(sessions: List<com.nextrank.feature.progress.domain.SessionHistoryItem>) {
    Text(
        text = stringResource(R.string.progress_recent_sessions),
        style = MaterialTheme.typography.titleLarge,
    )
    if (sessions.isEmpty()) {
        Text(
            text = stringResource(R.string.progress_no_sessions),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        sessions.forEach { session ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = session.completedAt ?: "—",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = stringResource(R.string.progress_xp_awarded, session.awardedXp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}