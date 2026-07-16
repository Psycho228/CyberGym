@file:Suppress("FunctionNaming")

package com.nextrank.feature.auth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nextrank.core.designsystem.component.GamerAccentLime
import com.nextrank.core.designsystem.component.GamerPanel
import com.nextrank.core.designsystem.component.GamerPrimaryButton

@Composable
internal fun AuthBrandHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Text(
            text = "CYBERGYM",
            style = MaterialTheme.typography.displayMedium,
            color = GamerAccentLime,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 24.dp),
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

internal data class AuthTextFieldConfig(
    val label: String,
    val placeholder: String,
    val keyboardOptions: KeyboardOptions,
    val keyboardActions: KeyboardActions,
    val enabled: Boolean,
    val isPassword: Boolean = false,
)

internal data class AuthFormText(
    val emailLabel: String,
    val emailPlaceholder: String,
    val passwordLabel: String,
    val passwordPlaceholder: String,
    val submitButton: String,
    val switchButton: String,
)

internal data class AuthFormActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onSubmit: () -> Unit,
    val onSwitchAuthMode: () -> Unit,
)

@Composable
internal fun AuthCredentialsForm(
    uiState: AuthUiState,
    text: AuthFormText,
    actions: AuthFormActions,
) {
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    GamerPanel {
        AuthEmailField(
            uiState = uiState,
            text = text,
            actions = actions,
            passwordFocusRequester = passwordFocusRequester,
        )
        AuthPasswordField(
            uiState = uiState,
            text = text,
            actions = actions,
            passwordFocusRequester = passwordFocusRequester,
            onDone = { focusManager.clearFocus() },
        )
        GamerPrimaryButton(
            text = text.submitButton,
            onClick = actions.onSubmit,
            enabled = !uiState.isLoading && uiState.email.isNotBlank() && uiState.password.isNotBlank(),
        )
        if (uiState.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }
        TextButton(
            onClick = actions.onSwitchAuthMode,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            Text(text.switchButton, fontWeight = FontWeight.Bold)
        }
        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun AuthEmailField(
    uiState: AuthUiState,
    text: AuthFormText,
    actions: AuthFormActions,
    passwordFocusRequester: FocusRequester,
) {
    val emailFocusRequester = remember { FocusRequester() }

    AuthTextField(
        value = uiState.email,
        onValueChange = actions.onEmailChange,
        config = AuthTextFieldConfig(
            label = text.emailLabel,
            placeholder = text.emailPlaceholder,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
            enabled = !uiState.isLoading,
        ),
        modifier = Modifier.focusRequester(emailFocusRequester),
    )
}

@Composable
private fun AuthPasswordField(
    uiState: AuthUiState,
    text: AuthFormText,
    actions: AuthFormActions,
    passwordFocusRequester: FocusRequester,
    onDone: () -> Unit,
) {
    AuthTextField(
        value = uiState.password,
        onValueChange = actions.onPasswordChange,
        config = AuthTextFieldConfig(
            label = text.passwordLabel,
            placeholder = text.passwordPlaceholder,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onDone()
                    actions.onSubmit()
                },
            ),
            enabled = !uiState.isLoading,
            isPassword = true,
        ),
        modifier = Modifier.focusRequester(passwordFocusRequester),
    )
}

@Composable
internal fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    config: AuthTextFieldConfig,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(config.label) },
        placeholder = { Text(config.placeholder) },
        visualTransformation = if (config.isPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = config.keyboardOptions,
        keyboardActions = config.keyboardActions,
        enabled = config.enabled,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
        ),
    )
}
