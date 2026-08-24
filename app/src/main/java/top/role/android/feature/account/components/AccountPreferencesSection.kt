package top.role.android.feature.account.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.core.preferences.AppAppearance
import top.role.android.core.preferences.WeekStart
import top.role.android.core.preferences.displayLabel


@Composable
fun AccountPreferencesSection(
    appearance: AppAppearance,
    weekStart: WeekStart,
    onAppearanceChange: (AppAppearance) -> Unit,
    onWeekStartChange: (WeekStart) -> Unit,
) {
    var showAppearanceDialog by
    remember {
        mutableStateOf(false)
    }

    var showWeekStartDialog by
    remember {
        mutableStateOf(false)
    }

    AccountSection(
        title =
            "偏好",
    ) {

        AccountValueNavigationRow(
            icon =
                Icons.Outlined.DarkMode,

            title =
                "外观",

            value =
                appearance.displayLabel(),

            onClick = {
                showAppearanceDialog =
                    true
            },
        )


        SectionDivider()


        AccountValueNavigationRow(
            icon =
                Icons.Outlined.CalendarMonth,

            title =
                "每周开始于",

            value =
                weekStart.displayLabel(),

            onClick = {
                showWeekStartDialog =
                    true
            },
        )
    }


    if (
        showAppearanceDialog
    ) {
        AppearanceDialog(
            current =
                appearance,

            onSelect = { value ->

                onAppearanceChange(
                    value
                )

                showAppearanceDialog =
                    false
            },

            onDismiss = {
                showAppearanceDialog =
                    false
            },
        )
    }


    if (
        showWeekStartDialog
    ) {
        WeekStartDialog(
            current =
                weekStart,

            onSelect = { value ->

                onWeekStartChange(
                    value
                )

                showWeekStartDialog =
                    false
            },

            onDismiss = {
                showWeekStartDialog =
                    false
            },
        )
    }
}


@Composable
private fun AppearanceDialog(
    current: AppAppearance,
    onSelect: (AppAppearance) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text(
                text =
                    "外观",
            )
        },

        text = {
            Column {

                AppAppearance.entries
                    .forEach { appearance ->

                        PreferenceChoiceRow(
                            label =
                                appearance.displayLabel(),

                            selected =
                                appearance == current,

                            onClick = {
                                onSelect(
                                    appearance
                                )
                            },
                        )
                    }
            }
        },

        confirmButton = {
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
    )
}


@Composable
private fun WeekStartDialog(
    current: WeekStart,
    onSelect: (WeekStart) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest =
            onDismiss,

        title = {
            Text(
                text =
                    "每周开始于",
            )
        },

        text = {
            Column {

                WeekStart.entries
                    .forEach { weekStart ->

                        PreferenceChoiceRow(
                            label =
                                weekStart.displayLabel(),

                            selected =
                                weekStart == current,

                            onClick = {
                                onSelect(
                                    weekStart
                                )
                            },
                        )
                    }
            }
        },

        confirmButton = {
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
    )
}


@Composable
private fun PreferenceChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick,
                )
                .padding(
                    vertical = 8.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        RadioButton(
            selected =
                selected,

            onClick =
                onClick,
        )


        Text(
            text =
                label,

            modifier =
                Modifier.padding(
                    start = 8.dp,
                ),
        )
    }
}