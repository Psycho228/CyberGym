package com.nextrank.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.nextrank.feature.home.R

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        Text(
            text = stringResource(R.string.home_welcome, uiState.nickname),
            style = MaterialTheme.typography.headlineSmall,
        )

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                label = stringResource(R.string.home_level),
                value = "${uiState.level}",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.home_xp),
                value = "${uiState.totalXp}",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = stringResource(R.string.home_streak),
                value = "${uiState.streak}",
                modifier = Modifier.weight(1f),
            )
        }

        // Daily plan card
        Card(
            modifier = Modifier.fillMaxWidth(),
            onClick = onTrainingClick,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.home_daily_plan),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.home_daily_plan_desc, uiState.exerciseCount, uiState.estimatedMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.home_start),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // Navigation buttons
        OutlinedButton(
            onClick = onProgressClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_progress))
        }

        OutlinedButton(
            onClick = onProfileClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_profile))
        }

        OutlinedButton(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.home_logout))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
    ) {
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
