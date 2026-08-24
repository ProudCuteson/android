package top.role.android.feature.account.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle


@Composable
fun AccountPasswordScreen(
    viewModel: AccountPasswordViewModel,
    onReSignIn: () -> Unit,
) {
    val state by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(
        state.shouldLogout
    ) {
        if (
            state.shouldLogout
        ) {
            viewModel.consumeShouldLogout()

            onReSignIn()
        }
    }

    AccountPasswordContent(
        state = state,

        onCurrentPasswordChange =
            viewModel::updateCurrentPassword,

        onNextPasswordChange =
            viewModel::updateNextPassword,

        onConfirmPasswordChange =
            viewModel::updateConfirmPassword,

        onSubmit =
            viewModel::submit,
    )
}


@Composable
private fun AccountPasswordContent(
    state: AccountPasswordUiState,

    onCurrentPasswordChange: (String) -> Unit,

    onNextPasswordChange: (String) -> Unit,

    onConfirmPasswordChange: (String) -> Unit,

    onSubmit: () -> Unit,
) {
    if (
        state.loading
    ) {
        Column(
            modifier =
                Modifier.fillMaxSize(),

            horizontalAlignment =
                Alignment.CenterHorizontally,

            verticalArrangement =
                Arrangement.Center,
        ) {
            CircularProgressIndicator()
        }

        return
    }

    PasswordForm(
        state = state,

        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 20.dp,
                    vertical = 20.dp,
                ),

        onCurrentPasswordChange =
            onCurrentPasswordChange,

        onNextPasswordChange =
            onNextPasswordChange,

        onConfirmPasswordChange =
            onConfirmPasswordChange,

        onSubmit =
            onSubmit,
    )
}


@Composable
private fun PasswordForm(
    state: AccountPasswordUiState,

    modifier: Modifier = Modifier,

    onCurrentPasswordChange: (String) -> Unit,

    onNextPasswordChange: (String) -> Unit,

    onConfirmPasswordChange: (String) -> Unit,

    onSubmit: () -> Unit,
) {
    Column(
        modifier =
            modifier,

        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            ),
    ) {

        Text(
            text =
                if (
                    state.hasPassword
                ) {
                    "修改登录密码"
                } else {
                    "设置登录密码"
                },

            style =
                MaterialTheme
                    .typography
                    .titleLarge,
        )


        Text(
            text =
                if (
                    state.hasPassword
                ) {
                    "修改密码后，需要重新登录账户。"
                } else {
                    "设置密码后，可使用手机号和密码登录。设置完成后需要重新登录。"
                },

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )


        Spacer(
            modifier =
                Modifier.height(
                    4.dp
                )
        )


        if (
            state.hasPassword
        ) {
            OutlinedTextField(
                value =
                    state.currentPassword,

                onValueChange =
                    onCurrentPasswordChange,

                modifier =
                    Modifier.fillMaxWidth(),

                enabled =
                    !state.submitting,

                singleLine =
                    true,

                label = {
                    Text(
                        "当前密码"
                    )
                },

                visualTransformation =
                    PasswordVisualTransformation(),

                keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                            KeyboardType.Password,
                    ),
            )
        }


        OutlinedTextField(
            value =
                state.nextPassword,

            onValueChange =
                onNextPasswordChange,

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !state.submitting,

            singleLine =
                true,

            label = {
                Text(
                    if (
                        state.hasPassword
                    ) {
                        "新密码"
                    } else {
                        "密码"
                    }
                )
            },

            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Password,
                ),
        )


        OutlinedTextField(
            value =
                state.confirmPassword,

            onValueChange =
                onConfirmPasswordChange,

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !state.submitting,

            singleLine =
                true,

            label = {
                Text(
                    "确认密码"
                )
            },

            visualTransformation =
                PasswordVisualTransformation(),

            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        KeyboardType.Password,
                ),
        )


        state.errorMessage
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { message ->

                Text(
                    text =
                        message,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                )
            }


        Button(
            onClick =
                onSubmit,

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !state.submitting,
        ) {

            if (
                state.submitting
            ) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.height(
                            18.dp
                        ),

                    strokeWidth =
                        2.dp,
                )
            } else {
                Text(
                    text =
                        if (
                            state.hasPassword
                        ) {
                            "修改密码"
                        } else {
                            "设置密码"
                        },
                )
            }
        }
    }
}