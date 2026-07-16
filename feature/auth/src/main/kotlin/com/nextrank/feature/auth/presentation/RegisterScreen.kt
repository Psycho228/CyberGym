package com.nextrank.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.nextrank.core.designsystem.component.GamerScreen
import com.nextrank.feature.auth.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) onRegisterSuccess()
    }

    GamerScreen(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            AuthBrandHeader(
                title = stringResource(R.string.register_title),
                subtitle = stringResource(R.string.register_subtitle),
            )
            AuthCredentialsForm(
                uiState = uiState,
                text = AuthFormText(
                    emailLabel = stringResource(R.string.email_label),
                    emailPlaceholder = stringResource(R.string.email_placeholder),
                    passwordLabel = stringResource(R.string.password_label),
                    passwordPlaceholder = stringResource(R.string.password_placeholder),
                    submitButton = stringResource(R.string.register_button),
                    switchButton = stringResource(R.string.has_account_question),
                ),
                actions = AuthFormActions(
                    onEmailChange = viewModel::onEmailChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onSubmit = viewModel::onRegister,
                    onSwitchAuthMode = onNavigateToLogin,
                ),
            )
        }
    }
}
