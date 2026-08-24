package top.role.android.feature.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp


private enum class SignInMode {
    Code,
    Password,
}


private enum class PendingAuthAction {
    SendOtp,
    PasswordSignIn,
}


@Composable
fun SignInScreen(
    state: SignInUiState,

    hasAcceptedAgreement: Boolean,
    onAcceptAgreement: () -> Unit,

    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,

    onPhoneChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,

    onSendOtp: () -> Unit,
    onSignIn: () -> Unit,
    onPasswordSignIn: () -> Unit,

    onBackToPhone: () -> Unit,
) {

    var mode by
    rememberSaveable {
        mutableStateOf(
            SignInMode.Password
        )
    }


    var pendingAuthAction by
    remember {
        mutableStateOf<PendingAuthAction?>(
            null
        )
    }


    fun performPendingAction(
        action: PendingAuthAction,
    ) {
        when (
            action
        ) {

            PendingAuthAction.SendOtp ->
                onSendOtp()

            PendingAuthAction.PasswordSignIn ->
                onPasswordSignIn()
        }
    }


    fun requestAuthAction(
        action: PendingAuthAction,
    ) {

        if (
            hasAcceptedAgreement
        ) {

            performPendingAction(
                action
            )

        } else {

            pendingAuthAction =
                action
        }
    }


    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .imePadding(),
    ) {

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .padding(
                        horizontal = 24.dp,
                    )
                    .padding(
                        top = 56.dp,
                        bottom = 48.dp,
                    ),

            horizontalAlignment =
                Alignment.CenterHorizontally,
        ) {

            /*
             * ------------------------------------------------
             * Brand
             * ------------------------------------------------
             */

            Spacer(
                modifier =
                    Modifier.height(
                        28.dp
                    )
            )


            Text(
                text =
                    "TopRole",

                style =
                    MaterialTheme
                        .typography
                        .headlineLarge,

                fontWeight =
                    FontWeight.Bold,

                color =
                    MaterialTheme
                        .colorScheme
                        .onBackground,
            )


            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )


            Text(
                text =
                    "登录你的账户",

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )


            Spacer(
                modifier =
                    Modifier.height(
                        36.dp
                    )
            )


            /*
             * ------------------------------------------------
             * Login container
             * ------------------------------------------------
             */

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .widthIn(
                            max = 420.dp
                        ),
            ) {

                SignInModePicker(
                    mode =
                        mode,

                    onModeChange = { targetMode ->

                        if (
                            targetMode !=
                            mode
                        ) {

                            mode =
                                targetMode

                            if (
                                state.otpSent
                            ) {

                                onBackToPhone()
                            }
                        }
                    },
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            16.dp
                        )
                )


                Card(
                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults.cardColors(
                            containerColor =
                                MaterialTheme
                                    .colorScheme
                                    .surfaceContainerLow,
                        ),
                ) {

                    Column(
                        modifier =
                            Modifier.padding(
                                horizontal = 20.dp,
                                vertical = 24.dp,
                            ),
                    ) {

                        when (
                            mode
                        ) {

                            SignInMode.Code -> {

                                if (
                                    !state.otpSent
                                ) {

                                    PhoneOtpStep(
                                        state =
                                            state,

                                        onPhoneChange =
                                            onPhoneChange,

                                        onSendOtp = {

                                            requestAuthAction(
                                                PendingAuthAction.SendOtp
                                            )
                                        },
                                    )

                                } else {

                                    OtpStep(
                                        state =
                                            state,

                                        onCodeChange =
                                            onCodeChange,

                                        /*
                                         * 发送验证码之前已经完成协议确认，
                                         * 因此这里不重复要求确认。
                                         */
                                        onSignIn =
                                            onSignIn,

                                        onBackToPhone =
                                            onBackToPhone,
                                    )
                                }
                            }


                            SignInMode.Password -> {

                                PasswordStep(
                                    state =
                                        state,

                                    onPhoneChange =
                                        onPhoneChange,

                                    onPasswordChange =
                                        onPasswordChange,

                                    onSignIn = {

                                        requestAuthAction(
                                            PendingAuthAction.PasswordSignIn
                                        )
                                    },
                                )
                            }
                        }


                        state.errorMessage
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?.let { message ->

                                Spacer(
                                    modifier =
                                        Modifier.height(
                                            16.dp
                                        )
                                )


                                Text(
                                    text =
                                        message,

                                    style =
                                        MaterialTheme
                                            .typography
                                            .bodySmall,

                                    color =
                                        MaterialTheme
                                            .colorScheme
                                            .error,
                                )
                            }


                        Spacer(
                            modifier =
                                Modifier.height(
                                    20.dp
                                )
                        )


                        SignInAgreementText(
                            hasAcceptedAgreement =
                                hasAcceptedAgreement,

                            onOpenTerms =
                                onOpenTerms,

                            onOpenPrivacy =
                                onOpenPrivacy,
                        )
                    }
                }
            }
        }
    }


    /*
     * ------------------------------------------------
     * Agreement confirmation
     * ------------------------------------------------
     */

    if (
        pendingAuthAction != null
    ) {

        AuthAgreementDialog(
            onDismiss = {

                pendingAuthAction =
                    null
            },

            onOpenTerms =
                onOpenTerms,

            onOpenPrivacy =
                onOpenPrivacy,

            onAgree = {

                val action =
                    pendingAuthAction

                pendingAuthAction =
                    null


                /*
                 * DataStore 写入可以异步进行。
                 *
                 * 用户点击「同意并继续」本身就已经完成
                 * 本次明确授权，因此无需等待 Flow 更新后
                 * 才执行登录动作。
                 */
                onAcceptAgreement()


                if (
                    action != null
                ) {

                    performPendingAction(
                        action
                    )
                }
            },
        )
    }
}


@Composable
private fun SignInModePicker(
    mode: SignInMode,
    onModeChange: (SignInMode) -> Unit,
) {

    val modes =
        listOf(
            SignInMode.Code,
            SignInMode.Password,
        )


    SingleChoiceSegmentedButtonRow(
        modifier =
            Modifier.fillMaxWidth(),
    ) {

        modes.forEachIndexed {
                index,
                item ->

            SegmentedButton(
                selected =
                    mode == item,

                onClick = {
                    onModeChange(
                        item
                    )
                },

                shape =
                    SegmentedButtonDefaults.itemShape(
                        index =
                            index,

                        count =
                            modes.size,
                    ),
            ) {

                Text(
                    text =
                        when (
                            item
                        ) {

                            SignInMode.Code ->
                                "验证码登录"

                            SignInMode.Password ->
                                "密码登录"
                        },
                )
            }
        }
    }
}


@Composable
private fun PhoneOtpStep(
    state: SignInUiState,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
) {

    SignInSectionTitle(
        title =
            "验证码登录",

        description =
            "使用手机号接收验证码",
    )


    Spacer(
        modifier =
            Modifier.height(
                20.dp
            )
    )


    PhoneField(
        phone =
            state.phone,

        enabled =
            !state.isSendingOtp,

        onPhoneChange =
            onPhoneChange,
    )


    Spacer(
        modifier =
            Modifier.height(
                20.dp
            )
    )


    Button(
        modifier =
            Modifier.fillMaxWidth(),

        enabled =
            !state.isSendingOtp &&
                    state.phone.isNotBlank(),

        onClick =
            onSendOtp,
    ) {

        if (
            state.isSendingOtp
        ) {

            SmallProgressIndicator()

        } else {

            Text(
                text =
                    "获取验证码",
            )
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

    SignInSectionTitle(
        title =
            "输入验证码",

        description =
            state.maskedPhone
                ?.let {
                    "验证码已发送至 $it"
                }
                ?: "请输入收到的短信验证码",
    )


    Spacer(
        modifier =
            Modifier.height(
                20.dp
            )
    )


    OutlinedTextField(
        modifier =
            Modifier.fillMaxWidth(),

        value =
            state.code,

        onValueChange =
            onCodeChange,

        enabled =
            !state.isSigningIn,

        singleLine =
            true,

        label = {

            Text(
                text =
                    "验证码",
            )
        },

        keyboardOptions =
            KeyboardOptions(
                keyboardType =
                    KeyboardType.Number,
            ),
    )


    Spacer(
        modifier =
            Modifier.height(
                20.dp
            )
    )


    Button(
        modifier =
            Modifier.fillMaxWidth(),

        enabled =
            !state.isSigningIn &&
                    state.code.isNotBlank(),

        onClick =
            onSignIn,
    ) {

        if (
            state.isSigningIn
        ) {

            SmallProgressIndicator()

        } else {

            Text(
                text =
                    "登录",
            )
        }
    }


    Spacer(
        modifier =
            Modifier.height(
                10.dp
            )
    )


    OutlinedButton(
        modifier =
            Modifier.fillMaxWidth(),

        enabled =
            !state.isSigningIn,

        onClick =
            onBackToPhone,
    ) {

        Text(
            text =
                "更换手机号",
        )
    }
}


@Composable
private fun PasswordStep(
    state: SignInUiState,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
) {

    SignInSectionTitle(
        title =
            "密码登录",

        description =
            "使用手机号和密码登录",
    )


    Spacer(
        modifier =
            Modifier.height(
                20.dp
            )
    )


    PhoneField(
        phone =
            state.phone,

        enabled =
            !state.isSigningIn,

        onPhoneChange =
            onPhoneChange,
    )


    Spacer(
        modifier =
            Modifier.height(
                14.dp
            )
    )


    OutlinedTextField(
        modifier =
            Modifier.fillMaxWidth(),

        value =
            state.password,

        onValueChange =
            onPasswordChange,

        enabled =
            !state.isSigningIn,

        singleLine =
            true,

        label = {

            Text(
                text =
                    "密码",
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


    Spacer(
        modifier =
            Modifier.height(
                24.dp
            )
    )


    Button(
        modifier =
            Modifier.fillMaxWidth(),

        enabled =
            !state.isSigningIn &&
                    state.phone.isNotBlank() &&
                    state.password.isNotBlank(),

        onClick =
            onSignIn,
    ) {

        if (
            state.isSigningIn
        ) {

            SmallProgressIndicator()

        } else {

            Text(
                text =
                    "登录",
            )
        }
    }
}


@Composable
private fun SignInAgreementText(
    hasAcceptedAgreement: Boolean,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
) {

    Column(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {

        Text(
            text =
                if (
                    hasAcceptedAgreement
                ) {
                    "已确认用户协议和隐私政策"
                } else {
                    "首次登录需要确认用户协议和隐私政策"
                },

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

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


        Row(
            verticalAlignment =
                Alignment.CenterVertically,
        ) {

            Text(
                text =
                    "查看",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )


            Text(
                modifier =
                    Modifier
                        .clickable(
                            onClick =
                                onOpenTerms,
                        )
                        .padding(
                            horizontal = 3.dp,
                            vertical = 4.dp,
                        ),

                text =
                    "《用户协议》",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,

                textDecoration =
                    TextDecoration.Underline,
            )


            Text(
                text =
                    "和",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )


            Text(
                modifier =
                    Modifier
                        .clickable(
                            onClick =
                                onOpenPrivacy,
                        )
                        .padding(
                            horizontal = 3.dp,
                            vertical = 4.dp,
                        ),

                text =
                    "《隐私政策》",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,

                textDecoration =
                    TextDecoration.Underline,
            )
        }
    }
}


@Composable
private fun AuthAgreementDialog(
    onDismiss: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onAgree: () -> Unit,
) {

    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {

            Text(
                text =
                    "用户协议与隐私政策",
            )
        },

        text = {

            Column {

                Text(
                    text =
                        "登录 TopRole 前，请阅读并同意用户协议和隐私政策。",
                )


                Spacer(
                    modifier =
                        Modifier.height(
                            12.dp
                        )
                )


                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,
                ) {

                    TextButton(
                        onClick =
                            onOpenTerms,
                    ) {

                        Text(
                            text =
                                "用户协议",
                        )
                    }


                    TextButton(
                        onClick =
                            onOpenPrivacy,
                    ) {

                        Text(
                            text =
                                "隐私政策",
                        )
                    }
                }
            }
        },

        dismissButton = {

            TextButton(
                onClick =
                    onDismiss,
            ) {

                Text(
                    text =
                        "取消",
                )
            }
        },

        confirmButton = {

            Button(
                onClick =
                    onAgree,
            ) {

                Text(
                    text =
                        "同意并继续",
                )
            }
        },
    )
}


@Composable
private fun SignInSectionTitle(
    title: String,
    description: String,
) {

    Text(
        text =
            title,

        style =
            MaterialTheme
                .typography
                .titleLarge,

        fontWeight =
            FontWeight.SemiBold,
    )


    Spacer(
        modifier =
            Modifier.height(
                6.dp
            )
    )


    Text(
        text =
            description,

        style =
            MaterialTheme
                .typography
                .bodyMedium,

        color =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant,
    )
}


@Composable
private fun PhoneField(
    phone: String,
    enabled: Boolean,
    onPhoneChange: (String) -> Unit,
) {

    OutlinedTextField(
        modifier =
            Modifier.fillMaxWidth(),

        value =
            phone,

        onValueChange =
            onPhoneChange,

        enabled =
            enabled,

        singleLine =
            true,

        label = {

            Text(
                text =
                    "手机号",
            )
        },

        prefix = {

            Text(
                text =
                    "+86 ",
            )
        },

        keyboardOptions =
            KeyboardOptions(
                keyboardType =
                    KeyboardType.Phone,
            ),
    )
}


@Composable
private fun SmallProgressIndicator() {

    CircularProgressIndicator(
        modifier =
            Modifier
                .width(
                    18.dp
                )
                .height(
                    18.dp
                ),

        strokeWidth =
            2.dp,
    )
}