package top.role.android.feature.slide

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.QuestionAnswer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import top.role.android.feature.slide.qa.SlideAskSheet
import top.role.android.feature.slide.render.SlidePageCanvas

@Composable
fun SlideDetailScreen(
    state: SlideDetailUiState,

    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,

    onLoadQuestions: () -> Unit,
    onSubmitQuestion: (
        text: String,
        onSuccess: () -> Unit,
    ) -> Unit,

    onMarkQuestionAnswered: (
        questionId: String,
    ) -> Unit,

    onHideQuestion: (
        questionId: String,
    ) -> Unit,

    onClearQuestionSubmitError: () -> Unit,
    onClearQuestionActionError: () -> Unit,

    onRetry: () -> Unit,
) {
    var showAskSheet by
    remember {
        mutableStateOf(
            false
        )
    }

    when {
        state.isLoading -> {
            SlideDetailLoading()
        }

        state.isUnavailable -> {
            SlideDetailUnavailable()
        }

        state.errorMessage != null -> {
            SlideDetailError(
                message =
                    state.errorMessage,

                onRetry =
                    onRetry,
            )
        }

        state.detail != null -> {
            SlideDetailContent(
                state =
                    state,

                onPreviousPage =
                    onPreviousPage,

                onNextPage =
                    onNextPage,

                onOpenQuestions = {
                    onClearQuestionSubmitError()
                    onClearQuestionActionError()

                    showAskSheet =
                        true
                },
            )
        }
    }

    if (
        showAskSheet &&
        state.detail != null
    ) {
        SlideAskSheet(
            slideTitle =
                state.detail.slide.title,

            pageIndex =
                state.currentPageIndex,

            pageCount =
                state.pageCount,

            isOwner =
                state.isOwner,

            questions =
                if (state.isOwner) {
                    state.presenterQuestions
                } else {
                    state.questions
                },

            questionsLoading =
                state.questionsLoading,

            questionsError =
                state.questionsError,

            submitting =
                state.submittingQuestion,

            submitError =
                state.questionSubmitError,

            questionActionId =
                state.questionActionId,

            questionActionError =
                state.questionActionError,

            onDismiss = {
                if (
                    !state.submittingQuestion
                ) {
                    showAskSheet =
                        false
                }
            },

            onRefresh = {
                onLoadQuestions()
            },

            onSubmit = { text ->
                onSubmitQuestion(
                    text,
                ) {
                    showAskSheet =
                        false
                }
            },

            onMarkAnswered = { questionId ->
                onMarkQuestionAnswered(
                    questionId
                )
            },

            onHide = { questionId ->
                onHideQuestion(
                    questionId
                )
            },
        )
    }
}

@Composable
private fun SlideDetailContent(
    state: SlideDetailUiState,

    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,

    onOpenQuestions: () -> Unit,
) {
    val detail =
        state.detail
            ?: return

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 16.dp,
                ),
    ) {
        Text(
            text =
                detail.slide.title,

            modifier =
                Modifier.padding(
                    top = 12.dp,
                ),

            style =
                MaterialTheme
                    .typography
                    .titleLarge,

            fontWeight =
                FontWeight.SemiBold,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis,
        )

        detail.slide.description
            ?.takeIf {
                it.isNotBlank()
            }
            ?.let { description ->

                Text(
                    text =
                        description,

                    modifier =
                        Modifier.padding(
                            top = 6.dp,
                        ),

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    maxLines =
                        2,

                    overflow =
                        TextOverflow.Ellipsis,
                )
            }

        Spacer(
            modifier =
                Modifier.height(
                    16.dp
                )
        )

        if (
            state.currentPage != null
        ) {
            SlidePageCanvas(
                slide =
                    detail.slide,

                page =
                    state.currentPage!!,

                modifier =
                    Modifier.fillMaxWidth(),
            )
        } else {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            220.dp
                        ),

                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text =
                        "暂无演示页面",

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,
                )
            }
        }

        SlidePageControls(
            state =
                state,

            onPreviousPage =
                onPreviousPage,

            onNextPage =
                onNextPage,
        )

        SlideInteractionBar(
            canAsk =
                state.canAsk,

            isOwner =
                state.isOwner,

            onOpenQuestions =
                onOpenQuestions,
        )
    }
}

@Composable
private fun SlidePageControls(
    state: SlideDetailUiState,

    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 12.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.SpaceBetween,
    ) {
        IconButton(
            enabled =
                state.hasPreviousPage,

            onClick =
                onPreviousPage,
        ) {
            Icon(
                imageVector =
                    Icons
                        .AutoMirrored
                        .Filled
                        .ArrowBack,

                contentDescription =
                    "上一页",
            )
        }

        Text(
            text =
                if (
                    state.pageCount > 0
                ) {
                    "${state.currentPageIndex + 1} / ${state.pageCount}"
                } else {
                    "0 / 0"
                },

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )

        IconButton(
            enabled =
                state.hasNextPage,

            onClick =
                onNextPage,
        ) {
            Icon(
                imageVector =
                    Icons
                        .AutoMirrored
                        .Filled
                        .ArrowForward,

                contentDescription =
                    "下一页",
            )
        }
    }
}

@Composable
private fun SlideInteractionBar(
    canAsk: Boolean,
    isOwner: Boolean,

    onOpenQuestions: () -> Unit,
) {
    /*
     * 普通用户：
     * canAsk=true 时显示“提问”。
     *
     * Owner / Presenter：
     * 即使 canAsk=false，也必须能进入现场问题面板。
     */
    if (
        !canAsk &&
        !isOwner
    ) {
        return
    }

    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            ),
    ) {
        SlideInteractionAction(
            text =
                if (isOwner) {
                    "现场提问"
                } else {
                    "提问"
                },

            icon = {
                Icon(
                    imageVector =
                        if (isOwner) {
                            Icons
                                .Outlined
                                .QuestionAnswer
                        } else {
                            Icons
                                .Outlined
                                .ChatBubbleOutline
                        },

                    contentDescription =
                        null,
                )
            },

            modifier =
                Modifier.weight(
                    1f
                ),

            onClick =
                onOpenQuestions,
        )
    }
}

@Composable
private fun SlideInteractionAction(
    text: String,

    icon:
    @Composable
        () -> Unit,

    modifier: Modifier = Modifier,

    onClick: () -> Unit,
) {
    Card(
        modifier =
            modifier
                .clickable(
                    onClick =
                        onClick,
                ),

        shape =
            RoundedCornerShape(
                14.dp
            ),

        colors =
            CardDefaults
                .cardColors(
                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surfaceContainerLow,
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 14.dp,
                        vertical = 12.dp,
                    ),

            verticalAlignment =
                Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.Center,
        ) {
            icon()

            Text(
                text =
                    text,

                modifier =
                    Modifier.padding(
                        start = 8.dp,
                    ),

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,
            )
        }
    }
}

@Composable
private fun SlideDetailLoading() {
    Box(
        modifier =
            Modifier.fillMaxSize(),

        contentAlignment =
            Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SlideDetailUnavailable() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    24.dp
                ),

        contentAlignment =
            Alignment.Center,
    ) {
        Text(
            text =
                "演示暂不可查看",

            textAlign =
                TextAlign.Center,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )
    }
}

@Composable
private fun SlideDetailError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    24.dp
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text =
                "演示加载失败",

            style =
                MaterialTheme
                    .typography
                    .titleMedium,
        )

        Text(
            text =
                message,

            modifier =
                Modifier.padding(
                    top = 8.dp,
                ),

            style =
                MaterialTheme
                    .typography
                    .bodyMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,

            textAlign =
                TextAlign.Center,
        )

        Text(
            text =
                "点击重试",

            modifier =
                Modifier
                    .padding(
                        top = 16.dp,
                    )
                    .clickable(
                        onClick =
                            onRetry,
                    ),

            style =
                MaterialTheme
                    .typography
                    .labelLarge,

            color =
                MaterialTheme
                    .colorScheme
                    .primary,
        )
    }
}