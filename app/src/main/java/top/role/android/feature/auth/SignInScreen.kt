package top.role.android.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun SignInScreen(
    state: SignInUiState,
    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onSignIn: () -> Unit,
    onBackToPhone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("TopRole")

        Spacer(
            modifier = Modifier.height(32.dp)
        )

        if (!state.otpSent) {
            PhoneStep(
                state = state,
                onPhoneChange = onPhoneChange,
                onSendOtp = onSendOtp,
            )
        } else {
            OtpStep(
                state = state,
                onCodeChange = onCodeChange,
                onSignIn = onSignIn,
                onBackToPhone = onBackToPhone,
            )
        }

        state.errorMessage?.let { message ->
            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(message)
        }
    }
}

@Composable
private fun PhoneStep(
    state: SignInUiState,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
) {
    Text("使用手机号登录")

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("+86")

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        OutlinedTextField(
            modifier = Modifier.weight(1f),
            value = state.phone,
            onValueChange = onPhoneChange,
            singleLine = true,
            label = {
                Text("手机号")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
            ),
        )
    }

    Spacer(
        modifier = Modifier.height(20.dp)
    )

    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isSendingOtp,
        onClick = onSendOtp,
    ) {
        if (state.isSendingOtp) {
            CircularProgressIndicator()
        } else {
            Text("获取验证码")
        }
    }
}

@Composable
private fun OtpStep(
    state: SignInUiState,
    onCodeChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onBackToPhone: () -> Unit,
) {
    Text(
        text = state.maskedPhone?.let {
            "验证码已发送至 $it"
        } ?: "请输入验证码"
    )

    Spacer(
        modifier = Modifier.height(16.dp)
    )

    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = state.code,
        onValueChange = onCodeChange,
        singleLine = true,
        label = {
            Text("验证码")
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
        ),
    )

    Spacer(
        modifier = Modifier.height(20.dp)
    )

    Button(
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isSigningIn,
        onClick = onSignIn,
    ) {
        if (state.isSigningIn) {
            CircularProgressIndicator()
        } else {
            Text("登录")
        }
    }

    Spacer(
        modifier = Modifier.height(8.dp)
    )

    OutlinedButton(
        modifier = Modifier.fillMaxWidth(),
        enabled = !state.isSigningIn,
        onClick = onBackToPhone,
    ) {
        Text("更换手机号")
    }
}