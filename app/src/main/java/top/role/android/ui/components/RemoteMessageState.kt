package top.role.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RemoteMessageState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    horizontal = 32.dp,
                    vertical = 24.dp,
                ),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center,
    ) {

        Text(
            text =
                title,

            style =
                MaterialTheme
                    .typography
                    .titleMedium,

            textAlign =
                TextAlign.Center,
        )

        Text(
            text =
                description,

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

        if (
            actionLabel != null &&
            onAction != null
        ) {
            Button(
                modifier =
                    Modifier.padding(
                        top = 16.dp,
                    ),

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