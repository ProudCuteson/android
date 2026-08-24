package top.role.android.feature.account.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

import top.role.android.core.model.account.MobileAccountProfile


@Composable
fun AccountProfileSection(
    profile: MobileAccountProfile,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {

        Surface(
            modifier =
                Modifier.size(56.dp),

            shape =
                MaterialTheme.shapes.extraLarge,

            color =
                MaterialTheme.colorScheme.surfaceVariant,
        ) {

            if (
                profile.avatarUrl
                    .isNotBlank()
            ) {

                AsyncImage(
                    model =
                        profile.avatarUrl,

                    contentDescription =
                        profile.name,

                    modifier =
                        Modifier.fillMaxSize(),
                )

            } else {

                Column(
                    modifier =
                        Modifier.fillMaxSize(),

                    verticalArrangement =
                        Arrangement.Center,

                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                ) {

                    Text(
                        text =
                            profileInitial(
                                profile.name
                            ),

                        style =
                            MaterialTheme.typography.titleLarge,

                        fontWeight =
                            FontWeight.SemiBold,

                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }


        Spacer(
            modifier =
                Modifier.size(16.dp)
        )


        Column(
            modifier =
                Modifier.weight(1f),
        ) {

            Text(
                text =
                    displayAccountValue(
                        profile.name
                    ),

                style =
                    MaterialTheme.typography.titleLarge,

                fontWeight =
                    FontWeight.SemiBold,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis,
            )
        }
    }
}


private fun profileInitial(
    name: String,
): String {

    return name
        .trim()
        .firstOrNull()
        ?.uppercase()
        ?: "U"
}


private fun displayAccountValue(
    value: String,
): String {

    return value
        .trim()
        .ifEmpty {
            "—"
        }
}