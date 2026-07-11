package com.nextrank.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    modifier: Modifier = Modifier,
) {
    GamerScreen(modifier = modifier) {
        GamerHeader(
            title = stringResource(R.string.home_welcome, uiState.nickname),
            subtitle = "Короткая тренировка, понятная цель и видимый прогресс каждый день.",
        )

        GamerStatRow {
            GamerStatCard(
                label = stringResource(R.string.home_level),
                value = "${uiState.level}",
                accent = GamerAccentLime,
                modifier = Modifier.weight(1f),
            )
            GamerStatCard(
                label = stringResource(R.string.home_xp),
                value = "${uiState.totalXp}",
                modifier = Modifier.weight(1f),
            )
            GamerStatCard(
                label = stringResource(R.string.home_streak),
                value = "${uiState.streak}",
                accent = GamerAccentOrange,
                modifier = Modifier.weight(1f),
            )
        }

        GamerPanel(accent = GamerAccentPink) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.home_daily_plan),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = stringResource(
                            R.string.home_daily_plan_desc,
                            uiState.exerciseCount,
                            uiState.estimatedMinutes,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                GamerChip(text = "${uiState.estimatedMinutes} MIN")
            }
            GamerPrimaryButton(
                text = stringResource(R.string.home_start),
                onClick = onTrainingClick,
                enabled = uiState.planId != null,
            )
        }

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
}
