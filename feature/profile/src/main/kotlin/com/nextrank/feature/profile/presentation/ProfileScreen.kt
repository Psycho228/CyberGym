package com.nextrank.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.nextrank.core.designsystem.component.GamerHeader
import com.nextrank.core.designsystem.component.GamerPanel
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.core.designsystem.component.GamerSecondaryButton
import com.nextrank.core.designsystem.component.GamerStatCard
import com.nextrank.core.designsystem.component.GamerStatRow
import com.nextrank.feature.profile.R
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
                subtitle = "Твоя карточка игрока, цели и текущая форма.",
            )

            when {
                uiState.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                uiState.errorMessage != null -> {
                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
                    GamerSecondaryButton(text = "Повторить", onClick = viewModel::loadProfile)
                }
                else -> {
                    GamerPanel(accent = GamerAccentLime) {
                        Text(
                            text = uiState.nickname,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        ProfileRow(
                            label = stringResource(R.string.profile_rank),
                            value = uiState.currentRank ?: stringResource(R.string.profile_rank_unknown),
                        )
                        ProfileRow(
                            label = stringResource(R.string.profile_goal),
                            value = uiState.primaryGoal ?: stringResource(R.string.profile_goal_unknown),
                        )
                        ProfileRow(
                            label = stringResource(R.string.profile_daily_minutes),
                            value = "${uiState.dailyMinutes} мин",
                        )
                    }

                    GamerStatRow {
                        GamerStatCard(
                            label = stringResource(R.string.profile_level),
                            value = "${uiState.level}",
                            accent = GamerAccentLime,
                            modifier = Modifier.weight(1f),
                        )
                        GamerStatCard(
                            label = stringResource(R.string.profile_total_xp),
                            value = "${uiState.totalXp}",
                            modifier = Modifier.weight(1f),
                        )
                    }
                    GamerStatRow {
                        GamerStatCard(
                            label = stringResource(R.string.profile_current_streak),
                            value = "${uiState.currentStreak}",
                            accent = GamerAccentOrange,
                            modifier = Modifier.weight(1f),
                        )
                        GamerStatCard(
                            label = stringResource(R.string.profile_longest_streak),
                            value = "${uiState.longestStreak}",
                            modifier = Modifier.weight(1f),
                        )
                    }

                    GamerSecondaryButton(text = "Назад", onClick = onBack)
                    GamerSecondaryButton(text = stringResource(R.string.profile_logout), onClick = onLogout)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
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
