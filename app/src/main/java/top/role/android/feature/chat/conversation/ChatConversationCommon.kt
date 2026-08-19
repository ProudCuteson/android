package top.role.android.feature.chat.conversation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun ChatConversationOperationErrors(
    state:
    ChatConversationUiState,
) {
    state.sendErrorMessage
        ?.takeIf {
            it.isNotBlank()
        }
        ?.let { error ->
            ChatInlineError(
                error
            )
        }

    state.imageSendErrorMessage
        ?.takeIf {
            it.isNotBlank() &&
                    state.imageDraft == null
        }
        ?.let { error ->
            ChatInlineError(
                error
            )
        }

    state.messageOperationError
        ?.takeIf {
            it.isNotBlank()
        }
        ?.let { error ->
            ChatInlineError(
                error
            )
        }
}

@Composable
private fun ChatInlineError(
    text:
    String,
) {
    androidx.compose.material3.Text(
        text =
            text,

        style =
            androidx.compose.material3
                .MaterialTheme
                .typography
                .bodySmall,

        color =
            androidx.compose.material3
                .MaterialTheme
                .colorScheme
                .error,

        modifier =
            Modifier
                .fillMaxWidth(),
    )
}

@Composable
internal fun ChatConversationLoading() {
    Box(
        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
internal fun ChatConversationUnavailable(
    title:
    String,

    onBack:
        () -> Unit,

    actionLabel:
    String? = null,

    onAction:
    (() -> Unit)? = null,
) {
    Column(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        TopAppBar(
            title = {
                Text(
                    "聊天"
                )
            },

            navigationIcon = {
                IconButton(
                    onClick =
                        onBack,
                ) {
                    Icon(
                        imageVector =
                            Icons
                                .AutoMirrored
                                .Filled
                                .ArrowBack,

                        contentDescription =
                            "返回",
                    )
                }
            },
        )

        Box(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),

            contentAlignment =
                Alignment.Center,
        ) {
            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally,

                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    ),
            ) {
                Text(
                    text =
                        title,
                )

                if (
                    actionLabel != null &&
                    onAction != null
                ) {
                    TextButton(
                        onClick =
                            onAction,
                    ) {
                        Text(
                            text =
                                actionLabel,
                        )
                    }
                }
            }
        }
    }
}

internal fun formatMessageTime(
    value:
    Double,
): String {
    return SimpleDateFormat(
        "HH:mm",
        Locale.getDefault(),
    ).format(
        Date(
            value.toLong()
        )
    )
}