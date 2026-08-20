package top.role.android.feature.survey.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import top.role.android.core.model.survey.MobileSurveyItem
import top.role.android.core.model.survey.MobileSurveyOption


@Composable
fun SurveyDetailScreen(
    state:
    SurveyDetailUiState,

    onAnswerChange:
        (
        fieldId: String,
        value: Any?,
    ) -> Unit,

    onPrevious:
        () -> Unit,

    onNext:
        () -> Unit,

    onSubmit:
        () -> Unit,
) {
    when {
        state.signedOut -> {
            SurveyDetailSignedOutContent()
        }

        state.loading &&
                state.detail == null -> {
            SurveyDetailLoadingContent()
        }

        state.submitted -> {
            SurveySubmittedContent(
                message =
                    state.submitMessage
                        ?: "提交成功",
            )
        }

        state.detail != null -> {
            SurveyDetailContent(
                state =
                    state,

                onAnswerChange =
                    onAnswerChange,

                onPrevious =
                    onPrevious,

                onNext =
                    onNext,

                onSubmit =
                    onSubmit,
            )
        }

        state.errorMessage != null -> {
            SurveyDetailErrorContent(
                message =
                    state.errorMessage,
            )
        }

        else -> {
            SurveyDetailErrorContent(
                message =
                    "调查不可用",
            )
        }
    }
}


@Composable
private fun SurveyDetailContent(
    state:
    SurveyDetailUiState,

    onAnswerChange:
        (
        fieldId: String,
        value: Any?,
    ) -> Unit,

    onPrevious:
        () -> Unit,

    onNext:
        () -> Unit,

    onSubmit:
        () -> Unit,
) {
    val detail =
        state.detail
            ?: return

    val groups =
        state.groups

    val groupIndex =
        state.currentGroupIndex
            .coerceIn(
                minimumValue = 0,
                maximumValue =
                    groups
                        .lastIndex
                        .coerceAtLeast(0),
            )

    val currentGroup =
        groups.getOrNull(
            groupIndex
        )

    val isLastGroup =
        groups.isEmpty() ||
                groupIndex >=
                groups.lastIndex

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .padding(
                    horizontal = 16.dp,
                    vertical = 16.dp,
                ),

        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            ),
    ) {

        Text(
            text =
                detail.title,

            style =
                MaterialTheme
                    .typography
                    .headlineSmall,

            fontWeight =
                FontWeight.SemiBold,
        )

        if (
            detail.description
                .isNotBlank()
        ) {
            Text(
                text =
                    detail.description,

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

        if (
            groups.size > 1 &&
            currentGroup != null
        ) {
            Text(
                text =
                    "第 ${groupIndex + 1} / ${groups.size} 组",

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                color =
                    MaterialTheme
                        .colorScheme
                        .primary,
            )

            if (
                currentGroup.title
                    .isNotBlank()
            ) {
                Text(
                    text =
                        currentGroup.title,

                    style =
                        MaterialTheme
                            .typography
                            .titleSmall,
                )
            }
        }

        HorizontalDivider()

        val items =
            currentGroup
                ?.items
                ?: emptyList()

        items.forEach { item ->

            SurveyItemContent(
                item =
                    item,

                answer =
                    item.fieldId
                        ?.let {
                            state.answers[it]
                        },

                onAnswerChange =
                    onAnswerChange,
            )
        }

        if (
            !state.errorMessage
                .isNullOrBlank()
        ) {
            Text(
                text =
                    state.errorMessage,

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

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.SpaceBetween,

            verticalAlignment =
                Alignment.CenterVertically,
        ) {

            if (
                groupIndex > 0
            ) {
                TextButton(
                    onClick =
                        onPrevious,
                ) {
                    Text(
                        text =
                            "上一组"
                    )
                }
            } else {
                Text(
                    text =
                        ""
                )
            }

            if (
                !isLastGroup
            ) {
                Button(
                    onClick =
                        onNext,
                ) {
                    Text(
                        text =
                            "下一组"
                    )
                }
            } else {
                Button(
                    enabled =
                        !state.submitting,

                    onClick =
                        onSubmit,
                ) {
                    if (
                        state.submitting
                    ) {
                        Text(
                            text =
                                "提交中..."
                        )
                    } else {
                        Text(
                            text =
                                "提交"
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun SurveyItemContent(
    item:
    MobileSurveyItem,

    answer:
    Any?,

    onAnswerChange:
        (
        fieldId: String,
        value: Any?,
    ) -> Unit,
) {
    when (
        item.kind
    ) {
        "content" -> {
            SurveyContentItem(
                item =
                    item,
            )
        }

        "question" -> {
            SurveyQuestionItem(
                item =
                    item,

                answer =
                    answer,

                onAnswerChange =
                    onAnswerChange,
            )
        }

        else -> {
            SurveyUnsupportedItem(
                item =
                    item,
            )
        }
    }
}


@Composable
private fun SurveyContentItem(
    item:
    MobileSurveyItem,
) {
    when (
        item.type
    ) {
        "heading" -> {
            Text(
                text =
                    item.title
                        ?: item.content
                        ?: "",

                style =
                    MaterialTheme
                        .typography
                        .titleMedium,

                fontWeight =
                    FontWeight.SemiBold,

                modifier =
                    Modifier.padding(
                        top = 8.dp
                    ),
            )
        }

        "paragraph" -> {
            Text(
                text =
                    item.content
                        ?: item.title
                        ?: "",

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

        "divider" -> {
            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 8.dp
                    )
            )
        }

        else -> {
            if (
                !item.content
                    .isNullOrBlank()
            ) {
                Text(
                    text =
                        item.content,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,
                )
            }
        }
    }
}


@Composable
private fun SurveyQuestionItem(
    item:
    MobileSurveyItem,

    answer:
    Any?,

    onAnswerChange:
        (
        fieldId: String,
        value: Any?,
    ) -> Unit,
) {
    val fieldId =
        item.fieldId
            ?: return

    Column(
        modifier =
            Modifier.fillMaxWidth(),

        verticalArrangement =
            Arrangement.spacedBy(
                10.dp
            ),
    ) {

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    4.dp
                ),
        ) {
            Text(
                text =
                    item.title
                        ?: "未命名题目",

                modifier =
                    Modifier.weight(1f),

                style =
                    MaterialTheme
                        .typography
                        .titleSmall,

                fontWeight =
                    FontWeight.Medium,
            )

            if (
                item.required
            ) {
                Text(
                    text =
                        "*",

                    color =
                        MaterialTheme
                            .colorScheme
                            .error,
                )
            }
        }

        if (
            !item.description
                .isNullOrBlank()
        ) {
            Text(
                text =
                    item.description,

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

        when (
            item.type
        ) {
            "singleChoice" -> {
                SurveySingleChoiceQuestion(
                    options =
                        item.options,

                    answer =
                        answer as? String,

                    onChange = {
                        onAnswerChange(
                            fieldId,
                            it,
                        )
                    },
                )
            }

            "multipleChoice" -> {
                SurveyMultipleChoiceQuestion(
                    options =
                        item.options,

                    answer =
                        answer,

                    onChange = {
                        onAnswerChange(
                            fieldId,
                            it,
                        )
                    },
                )
            }

            "shortText" -> {
                SurveyTextQuestion(
                    value =
                        answer as? String
                            ?: "",

                    placeholder =
                        item.placeholder
                            ?: "",

                    singleLine =
                        true,

                    onChange = {
                        onAnswerChange(
                            fieldId,
                            it,
                        )
                    },
                )
            }

            "longText" -> {
                SurveyTextQuestion(
                    value =
                        answer as? String
                            ?: "",

                    placeholder =
                        item.placeholder
                            ?: "",

                    singleLine =
                        false,

                    onChange = {
                        onAnswerChange(
                            fieldId,
                            it,
                        )
                    },
                )
            }

            "rating" -> {
                SurveyRatingQuestion(
                    item =
                        item,

                    answer =
                        answer,

                    onChange = {
                        onAnswerChange(
                            fieldId,
                            it,
                        )
                    },
                )
            }

            "number" -> {
                SurveyNumberQuestion(
                    item =
                        item,

                    answer =
                        answer,

                    onChange = {
                        onAnswerChange(
                            fieldId,
                            it,
                        )
                    },
                )
            }

            "boolean" -> {
                SurveyBooleanQuestion(
                    item =
                        item,

                    answer =
                        answer as? Boolean,

                    onChange = {
                        onAnswerChange(
                            fieldId,
                            it,
                        )
                    },
                )
            }

            else -> {
                Text(
                    text =
                        "暂不支持此题型：${item.type}",

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
}


@Composable
private fun SurveySingleChoiceQuestion(
    options:
    List<MobileSurveyOption>,

    answer:
    String?,

    onChange:
        (String) -> Unit,
) {
    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
    ) {
        options.forEach { option ->

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onChange(
                                option.value
                            )
                        },

                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected =
                        answer ==
                                option.value,

                    onClick = {
                        onChange(
                            option.value
                        )
                    },
                )

                Text(
                    text =
                        option.label,

                    modifier =
                        Modifier.padding(
                            start = 6.dp
                        ),
                )
            }
        }
    }
}


@Composable
private fun SurveyMultipleChoiceQuestion(
    options:
    List<MobileSurveyOption>,

    answer:
    Any?,

    onChange:
        (List<String>) -> Unit,
) {
    val selected =
        when (
            answer
        ) {
            is List<*> ->
                answer
                    .filterIsInstance<String>()
                    .toSet()

            else ->
                emptySet()
        }

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
    ) {
        options.forEach { option ->

            val checked =
                option.value in selected

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {

                            val next =
                                selected
                                    .toMutableSet()

                            if (
                                checked
                            ) {
                                next.remove(
                                    option.value
                                )
                            } else {
                                next.add(
                                    option.value
                                )
                            }

                            onChange(
                                next.toList()
                            )
                        },

                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked =
                        checked,

                    onCheckedChange = {

                        val next =
                            selected
                                .toMutableSet()

                        if (
                            checked
                        ) {
                            next.remove(
                                option.value
                            )
                        } else {
                            next.add(
                                option.value
                            )
                        }

                        onChange(
                            next.toList()
                        )
                    },
                )

                Text(
                    text =
                        option.label,

                    modifier =
                        Modifier.padding(
                            start = 6.dp
                        ),
                )
            }
        }
    }
}


@Composable
private fun SurveyTextQuestion(
    value:
    String,

    placeholder:
    String,

    singleLine:
    Boolean,

    onChange:
        (String) -> Unit,
) {
    OutlinedTextField(
        value =
            value,

        onValueChange =
            onChange,

        modifier =
            Modifier.fillMaxWidth(),

        singleLine =
            singleLine,

        minLines =
            if (
                singleLine
            ) {
                1
            } else {
                4
            },

        placeholder = {
            if (
                placeholder
                    .isNotBlank()
            ) {
                Text(
                    text =
                        placeholder
                )
            }
        },
    )
}


@Composable
private fun SurveyRatingQuestion(
    item:
    MobileSurveyItem,

    answer:
    Any?,

    onChange:
        (Double) -> Unit,
) {
    val min =
        item.rating
            ?.min
            ?: 1.0

    val max =
        item.rating
            ?.max
            ?: item.ratingScale
            ?: 5.0

    val current =
        when (
            answer
        ) {
            is Double ->
                answer

            is Int ->
                answer.toDouble()

            else ->
                null
        }

    val minInt =
        min.toInt()

    val maxInt =
        max.toInt()

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            ),
    ) {

        Row(
            modifier =
                Modifier.fillMaxWidth(),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                ),
        ) {
            for (
            value in
            minInt..maxInt
            ) {
                Text(
                    text =
                        value.toString(),

                    modifier =
                        Modifier
                            .clickable {
                                onChange(
                                    value.toDouble()
                                )
                            }
                            .padding(
                                horizontal = 10.dp,
                                vertical = 8.dp,
                            ),

                    fontWeight =
                        if (
                            current ==
                            value.toDouble()
                        ) {
                            FontWeight.Bold
                        } else {
                            FontWeight.Normal
                        },

                    color =
                        if (
                            current ==
                            value.toDouble()
                        ) {
                            MaterialTheme
                                .colorScheme
                                .primary
                        } else {
                            MaterialTheme
                                .colorScheme
                                .onSurface
                        },
                )
            }
        }

        val minLabel =
            item.rating
                ?.minLabel

        val maxLabel =
            item.rating
                ?.maxLabel

        if (
            !minLabel.isNullOrBlank() ||
            !maxLabel.isNullOrBlank()
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,
            ) {
                Text(
                    text =
                        minLabel ?: "",

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
                    text =
                        maxLabel ?: "",

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
}


@Composable
private fun SurveyNumberQuestion(
    item:
    MobileSurveyItem,

    answer:
    Any?,

    onChange:
        (Double?) -> Unit,
) {
    val current =
        when (
            answer
        ) {
            is Double ->
                answer.toString()

            is Int ->
                answer.toString()

            else ->
                ""
        }

    OutlinedTextField(
        value =
            current,

        onValueChange = {
                raw ->

            if (
                raw.isBlank()
            ) {
                onChange(
                    null
                )
            } else {
                raw
                    .toDoubleOrNull()
                    ?.let {
                            value ->

                        val min =
                            item.min

                        val max =
                            item.max

                        if (
                            min != null &&
                            value < min
                        ) {
                            return@let
                        }

                        if (
                            max != null &&
                            value > max
                        ) {
                            return@let
                        }

                        onChange(
                            value
                        )
                    }
            }
        },

        modifier =
            Modifier.fillMaxWidth(),

        singleLine =
            true,

        supportingText = {
            val texts =
                buildList {
                    item.min
                        ?.let {
                            add(
                                "最小 $it"
                            )
                        }

                    item.max
                        ?.let {
                            add(
                                "最大 $it"
                            )
                        }
                }

            if (
                texts.isNotEmpty()
            ) {
                Text(
                    text =
                        texts.joinToString(
                            " · "
                        )
                )
            }
        },
    )
}


@Composable
private fun SurveyBooleanQuestion(
    item:
    MobileSurveyItem,

    answer:
    Boolean?,

    onChange:
        (Boolean) -> Unit,
) {
    val trueLabel =
        item.booleanLabels
            ?.trueLabel
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "是"

    val falseLabel =
        item.booleanLabels
            ?.falseLabel
            ?.takeIf {
                it.isNotBlank()
            }
            ?: "否"

    Column(
        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            ),
    ) {

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        onChange(
                            true
                        )
                    },

            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            RadioButton(
                selected =
                    answer == true,

                onClick = {
                    onChange(
                        true
                    )
                },
            )

            Text(
                text =
                    trueLabel
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        onChange(
                            false
                        )
                    },

            verticalAlignment =
                Alignment.CenterVertically,
        ) {
            RadioButton(
                selected =
                    answer == false,

                onClick = {
                    onChange(
                        false
                    )
                },
            )

            Text(
                text =
                    falseLabel
            )
        }
    }
}


@Composable
private fun SurveyUnsupportedItem(
    item:
    MobileSurveyItem,
) {
    Text(
        text =
            item.reason
                ?: "暂不支持此内容",

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


@Composable
private fun SurveyDetailLoadingContent() {
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
}


@Composable
private fun SurveyDetailSignedOutContent() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text =
                "登录后参与调查"
        )
    }
}


@Composable
private fun SurveyDetailErrorContent(
    message:
    String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text =
                message,

            color =
                MaterialTheme
                    .colorScheme
                    .error,
        )
    }
}


@Composable
private fun SurveySubmittedContent(
    message:
    String,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {
        Text(
            text =
                "提交完成",

            style =
                MaterialTheme
                    .typography
                    .headlineSmall,

            fontWeight =
                FontWeight.SemiBold,
        )

        Text(
            text =
                message,

            modifier =
                Modifier.padding(
                    top = 12.dp
                ),

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
}