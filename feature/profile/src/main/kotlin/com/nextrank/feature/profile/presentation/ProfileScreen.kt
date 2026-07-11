package com.nextrank.feature.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.nextrank.feature.profile.R

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
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
            text = stringResource(R.string.profile_title),
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
                    onClick = viewModel::loadProfile,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Повторить") }
            }
            else -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = uiState.nickname,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
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
                            value = "${uiState.dailyMinutes}",
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label = stringResource(R.string.profile_level),
                        value = "${uiState.level}",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.profile_total_xp),
                        value = "${uiState.totalXp}",
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    StatCard(
                        label = stringResource(R.string.profile_current_streak),
                        value = "${uiState.currentStreak}",
                        modifier = Modifier.weight(1f),
                    )
                    StatCard(
                        label = stringResource(R.string.profile_longest_streak),
                        value = "${uiState.longestStreak}",
                        modifier = Modifier.weight(1f),
                    )
                }

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Назад") }

                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.profile_logout)) }
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
            fontWeight = FontWeight.Medium,
        )
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