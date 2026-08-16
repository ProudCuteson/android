package top.role.android.feature.slide.qa

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.role.android.core.model.slide.MobileSlideQuestionItem
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlideAskSheet(
    slideTitle: String,
    pageIndex: Int,
    pageCount: Int,
    isOwner: Boolean,

    questions: List<MobileSlideQuestionItem>,
    questionsLoading: Boolean,
    questionsError: String?,

    submitting: Boolean,
    submitError: String?,

    questionActionId: String?,
    questionActionError: String?,

    onDismiss: () -> Unit,
    onRefresh: () -> Unit,
    onSubmit: (String) -> Unit,
    onMarkAnswered: (String) -> Unit,
    onHide: (String) -> Unit,
) {
    var text by
    remember {
        mutableStateOf("")
    }

    val trimmedText =
        text.trim()

    val canSubmit =
        !submitting &&
                trimmedText.isNotEmpty() &&
                trimmedText.length <= 500

    /*
     * 首次打开，以及当前 page 变化时，
     * 加载该页的问题。
     */
    LaunchedEffect(
        pageIndex
    ) {
        onRefresh()
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (!submitting) {
                onDismiss()
            }
        },
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom = 12.dp
                    ),
        ) {
            SlideAskSheetHeader(
                slideTitle =
                    slideTitle,

                pageIndex =
                    pageIndex,

                pageCount =
                    pageCount,

                isOwner =
                    isOwner,

                questionsLoading =
                    questionsLoading,

                onRefresh =
                    onRefresh,
            )

            HorizontalDivider()

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(
                            1f,
                            fill = false,
                        )
                        .heightIn(
                            max = 680.dp
                        )
                        .verticalScroll(
                            rememberScrollState()
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 16.dp,
                        ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        20.dp
                    ),
            ) {
                if (!isOwner) {
                    SlideQuestionInputSection(
                        text =
                            text,

                        onTextChange = {
                            text = it
                        },

                        submitting =
                            submitting,

                        submitError =
                            submitError,
                    )
                }

                SlideQuestionListSection(
                    isOwner =
                        isOwner,

                    questions =
                        questions,

                    questionsLoading =
                        questionsLoading,

                    questionsError =
                        questionsError,

                    questionActionId =
                        questionActionId,

                    questionActionError =
                        questionActionError,

                    onMarkAnswered =
                        onMarkAnswered,

                    onHide =
                        onHide,
                )
            }

            if (!isOwner) {
                HorizontalDivider()

                SlideAskSheetBottomBar(
                    submitting =
                        submitting,

                    canSubmit =
                        canSubmit,

                    onDismiss =
                        onDismiss,

                    onSubmit = {
                        onSubmit(
                            trimmedText
                        )
                    },
                )
            } else {
                HorizontalDivider()

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 12.dp,
                            ),

                    horizontalArrangement =
                        Arrangement.End,
                ) {
                    OutlinedButton(
                        onClick =
                            onDismiss,
                    ) {
                        Text(
                            "关闭"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlideAskSheetHeader(
    slideTitle: String,
    pageIndex: Int,
    pageCount: Int,
    isOwner: Boolean,
    questionsLoading: Boolean,
    onRefresh: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Column(
            modifier =
                Modifier.weight(
                    1f
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    4.dp
                ),
        ) {
            Text(
                text =
                    if (isOwner) {
                        "现场提问"
                    } else {
                        "在线提问"
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold,
            )

            Text(
                text =
                    slideTitle,

                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,

                maxLines =
                    2,

                overflow =
                    TextOverflow.Ellipsis,
            )

            Text(
                text =
                    "第 ${pageIndex + 1} 页 / 共 ${pageCount.coerceAtLeast(1)} 页",

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        }

        Spacer(
            modifier =
                Modifier.width(
                    8.dp
                )
        )

        IconButton(
            onClick =
                onRefresh,

            enabled =
                !questionsLoading,
        ) {
            if (questionsLoading) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.width(
                            20.dp
                        ),

                    strokeWidth =
                        2.dp,
                )
            } else {
                Icon(
                    imageVector =
                        Icons.Outlined.Refresh,

                    contentDescription =
                        "刷新问题",
                )
            }
        }
    }
}

@Composable
private fun SlideQuestionInputSection(
    text: String,
    onTextChange: (String) -> Unit,
    submitting: Boolean,
    submitError: String?,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),
    ) {
        Text(
            text =
                "提问",

            style =
                MaterialTheme
                    .typography
                    .titleSmall,

            fontWeight =
                FontWeight.SemiBold,
        )

        OutlinedTextField(
            value =
                text,

            onValueChange = {
                /*
                 * 允许临时超过 500，
                 * 这样用户还能正常编辑回来。
                 */
                onTextChange(
                    it
                )
            },

            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 140.dp
                    ),

            placeholder = {
                Text(
                    "输入你想问的问题，主讲人可以根据当前演示页理解上下文。"
                )
            },

            enabled =
                !submitting,

            minLines =
                5,

            maxLines =
                8,

            supportingText = {
                if (
                    text.trim()
                        .length >= 400
                ) {
                    val length =
                        text.trim()
                            .length

                    Text(
                        text =
                            "$length / 500",

                        color =
                            if (
                                length > 500
                            ) {
                                MaterialTheme
                                    .colorScheme
                                    .error
                            } else {
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant
                            },
                    )
                }
            },
        )

        if (
            !submitError.isNullOrBlank()
        ) {
            Text(
                text =
                    submitError,

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
    }
}

@Composable
private fun SlideQuestionListSection(
    isOwner: Boolean,
    questions: List<MobileSlideQuestionItem>,
    questionsLoading: Boolean,
    questionsError: String?,
    questionActionId: String?,
    questionActionError: String?,
    onMarkAnswered: (String) -> Unit,
    onHide: (String) -> Unit,
) {
    Column(
        modifier =
            Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                12.dp
            ),
    ) {
        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            Text(
                text =
                    if (isOwner) {
                        "本页提问"
                    } else {
                        "本页已有问题"
                    },

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.SemiBold,
            )

            if (questionsLoading) {
                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                CircularProgressIndicator(
                    modifier =
                        Modifier.width(
                            16.dp
                        ),

                    strokeWidth =
                        2.dp,
                )
            }

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            if (questions.isNotEmpty()) {
                Surface(
                    shape =
                        RoundedCornerShape(
                            999.dp
                        ),

                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant,
                ) {
                    Text(
                        text =
                            questions.size
                                .toString(),

                        modifier =
                            Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 3.dp,
                            ),

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        fontWeight =
                            FontWeight.SemiBold,
                    )
                }
            }
        }

        if (
            !questionActionError
                .isNullOrBlank()
        ) {
            Text(
                text =
                    questionActionError,

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

        when {
            !questionsError
                .isNullOrBlank() -> {
                Text(
                    text =
                        questionsError,

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }

            questions.isEmpty() &&
                    !questionsLoading -> {
                SlideQuestionEmptyState(
                    isOwner =
                        isOwner
                )
            }

            else -> {
                Column(
                    modifier =
                        Modifier.fillMaxWidth(),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        ),
                ) {
                    questions.forEach { item ->

                        SlideQuestionRow(
                            item =
                                item,

                            isOwner =
                                isOwner,

                            actionRunning =
                                questionActionId ==
                                        item.id,

                            actionsEnabled =
                                questionActionId ==
                                        null,

                            onMarkAnswered = {
                                onMarkAnswered(
                                    item.id
                                )
                            },

                            onHide = {
                                onHide(
                                    item.id
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SlideQuestionEmptyState(
    isOwner: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha = 0.55f
                            ),

                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),
                )
                .padding(
                    14.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                4.dp
            ),
    ) {
        Text(
            text =
                "还没有人提问。",

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )

        if (!isOwner) {
            Text(
                text =
                    "你可以先提出一个问题，避免大家重复问同一件事。",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SlideQuestionRow(
    item: MobileSlideQuestionItem,
    isOwner: Boolean,
    actionRunning: Boolean,
    actionsEnabled: Boolean,
    onMarkAnswered: () -> Unit,
    onHide: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    color =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant
                            .copy(
                                alpha = 0.55f
                            ),

                    shape =
                        RoundedCornerShape(
                            14.dp
                        ),
                )
                .padding(
                    12.dp
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                9.dp
            ),
    ) {
        Text(
            text =
                item.text,

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,
        )

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            SlideQuestionStatusBadge(
                status =
                    item.status
            )

            Spacer(
                modifier =
                    Modifier.width(
                        8.dp
                    )
            )

            Text(
                text =
                    questionRelativeTime(
                        item.createdAt
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelSmall,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )

            Spacer(
                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            if (actionRunning) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.width(
                            16.dp
                        ),

                    strokeWidth =
                        2.dp,
                )
            }
        }

        if (isOwner) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.End,

                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                if (
                    item.status == "open"
                ) {
                    TextButton(
                        onClick =
                            onMarkAnswered,

                        enabled =
                            actionsEnabled,
                    ) {
                        Text(
                            "标记已回答"
                        )
                    }
                }

                TextButton(
                    onClick =
                        onHide,

                    enabled =
                        actionsEnabled,
                ) {
                    Text(
                        "隐藏"
                    )
                }
            }
        }
    }
}

@Composable
private fun SlideQuestionStatusBadge(
    status: String,
) {
    val answered =
        status == "answered"

    val color =
        if (answered) {
            MaterialTheme
                .colorScheme
                .primary
        } else {
            MaterialTheme
                .colorScheme
                .tertiary
        }

    Surface(
        color =
            color.copy(
                alpha = 0.12f
            ),

        shape =
            RoundedCornerShape(
                999.dp
            ),
    ) {
        Text(
            text =
                if (answered) {
                    "已回应"
                } else {
                    "待回应"
                },

            modifier =
                Modifier.padding(
                    horizontal = 8.dp,
                    vertical = 3.dp,
                ),

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            fontWeight =
                FontWeight.SemiBold,

            color =
                color,
        )
    }
}

@Composable
private fun SlideAskSheetBottomBar(
    submitting: Boolean,
    canSubmit: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp,
                ),

        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            ),
    ) {
        OutlinedButton(
            onClick =
                onDismiss,

            enabled =
                !submitting,

            modifier =
                Modifier.weight(
                    1f
                ),
        ) {
            Text(
                "关闭"
            )
        }

        Button(
            onClick =
                onSubmit,

            enabled =
                canSubmit,

            modifier =
                Modifier.weight(
                    1f
                ),
        ) {
            if (submitting) {
                CircularProgressIndicator(
                    modifier =
                        Modifier.width(
                            16.dp
                        ),

                    strokeWidth =
                        2.dp,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onPrimary,
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            8.dp
                        )
                )

                Text(
                    "提交中..."
                )
            } else {
                Text(
                    "提交问题"
                )
            }
        }
    }
}

private fun questionRelativeTime(
    timestamp: Double,
): String {
    if (
        timestamp <= 0.0 ||
        !timestamp.isFinite()
    ) {
        return ""
    }

    val now =
        System.currentTimeMillis()

    val target =
        timestamp
            .toLong()

    val diff =
        now -
                target

    if (diff < 0L) {
        return "刚刚"
    }

    val seconds =
        diff /
                1_000L

    val minutes =
        seconds /
                60L

    val hours =
        minutes /
                60L

    val days =
        hours /
                24L

    return when {
        seconds < 60 ->
            "刚刚"

        minutes < 60 ->
            String.format(
                Locale.CHINA,
                "%d分钟前",
                minutes,
            )

        hours < 24 ->
            String.format(
                Locale.CHINA,
                "%d小时前",
                hours,
            )

        days < 7 ->
            String.format(
                Locale.CHINA,
                "%d天前",
                days,
            )

        else ->
            ""
    }
}