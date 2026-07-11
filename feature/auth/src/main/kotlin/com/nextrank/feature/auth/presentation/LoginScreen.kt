package com.nextrank.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.nextrank.feature.auth.R

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::onLogin,
        onNavigateToRegister = onNavigateToRegister,
        onLoginSuccess = onLoginSuccess,
        modifier = modifier,
    )
}

@Composable
private fun LoginContent(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email_label)) },
                placeholder = { Text(stringResource(R.string.email_placeholder)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() },
                ),
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester),
                singleLine = true,
            )

            OutlinedTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password_label)) },
                placeholder = { Text(stringResource(R.string.password_placeholder)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onLogin()
                    },
                ),
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                singleLine = true,
            )

            Button(
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(4.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.login_button))
                }
            }

            TextButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.no_account_question))
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
