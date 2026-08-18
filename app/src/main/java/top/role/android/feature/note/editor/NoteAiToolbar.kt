package top.role.android.feature.note.editor

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.core.data.note.ai.NoteAiMode

@Composable
fun NoteAiToolbar(
    running: Boolean,
    suggestion: String?,
    suggestionMode: NoteAiMode?,
    errorMessage: String?,
    onRunAi: (NoteAiMode) -> Unit,
    onReplaceText: () -> Unit,
    onAppendText: () -> Unit,
    onUseAsTitle: () -> Unit,
    onDismissSuggestion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp,
            ),
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            Icon(
                imageVector =
                    Icons.Default.AutoAwesome,

                contentDescription =
                    null,

                tint =
                    MaterialTheme
                        .colorScheme
                        .primary,
            )

            Text(
                text =
                    "AI 助力",

                modifier =
                    Modifier.padding(
                        start =
                            6.dp,
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
            )

            Spacer(
                modifier =
                    Modifier.weight(
                        1f,
                    ),
            )

            if (
                running
            ) {
                CircularProgressIndicator()
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(
                        rememberScrollState(),
                    ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp,
                ),
        ) {
            NoteAiModeChip(
                label =
                    "润色",

                mode =
                    NoteAiMode.Polish,

                enabled =
                    !running,

                onClick =
                    onRunAi,
            )

            NoteAiModeChip(
                label =
                    "扩写",

                mode =
                    NoteAiMode.Expand,

                enabled =
                    !running,

                onClick =
                    onRunAi,
            )

            NoteAiModeChip(
                label =
                    "缩短",

                mode =
                    NoteAiMode.Shorten,

                enabled =
                    !running,

                onClick =
                    onRunAi,
            )

            NoteAiModeChip(
                label =
                    "标题",

                mode =
                    NoteAiMode.Title,

                enabled =
                    !running,

                onClick =
                    onRunAi,
            )

            NoteAiModeChip(
                label =
                    "生成",

                mode =
                    NoteAiMode.Free,

                enabled =
                    !running,

                onClick =
                    onRunAi,
            )
        }

        if (
            !errorMessage.isNullOrBlank()
        ) {
            Text(
                text =
                    errorMessage,

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

        if (
            !suggestion.isNullOrBlank() &&
            suggestionMode !=
            null
        ) {
            NoteAiSuggestion(
                suggestion =
                    suggestion,

                mode =
                    suggestionMode,

                onReplaceText =
                    onReplaceText,

                onAppendText =
                    onAppendText,

                onUseAsTitle =
                    onUseAsTitle,

                onDismiss =
                    onDismissSuggestion,
            )
        }
    }
}


@Composable
private fun NoteAiModeChip(
    label: String,
    mode: NoteAiMode,
    enabled: Boolean,
    onClick: (NoteAiMode) -> Unit,
) {
    AssistChip(
        onClick = {
            onClick(
                mode,
            )
        },

        enabled =
            enabled,

        label = {
            Text(
                text =
                    label,
            )
        },
    )
}

@Composable
private fun NoteAiSuggestion(
    suggestion: String,
    mode: NoteAiMode,
    onReplaceText: () -> Unit,
    onAppendText: () -> Unit,
    onUseAsTitle: () -> Unit,
    onDismiss: () -> Unit,
) {
    Surface(
        modifier =
            Modifier.fillMaxWidth(),

        shape =
            MaterialTheme
                .shapes
                .medium,

        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant,
    ) {
        Column(
            modifier =
                Modifier.padding(
                    12.dp,
                ),
        ) {
            Text(
                text =
                    if (
                        mode ==
                        NoteAiMode.Title
                    ) {
                        "AI 标题建议"
                    } else {
                        "AI 建议"
                    },

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp,
                    ),
            )

            Text(
                text =
                    suggestion,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
            )

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp,
                    ),
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.End,

                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                if (
                    mode ==
                    NoteAiMode.Title
                ) {
                    TextButton(
                        onClick =
                            onUseAsTitle,
                    ) {
                        Text(
                            text =
                                "使用为标题",
                        )
                    }
                } else {
                    TextButton(
                        onClick =
                            onReplaceText,
                    ) {
                        Text(
                            text =
                                "替换正文",
                        )
                    }

                    TextButton(
                        onClick =
                            onAppendText,
                    ) {
                        Text(
                            text =
                                "追加",
                        )
                    }
                }

                TextButton(
                    onClick =
                        onDismiss,
                ) {
                    Text(
                        text =
                            "取消",
                    )
                }
            }
        }
    }
}