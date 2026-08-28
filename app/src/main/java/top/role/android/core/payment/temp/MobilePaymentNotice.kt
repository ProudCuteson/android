package top.role.android.core.payment.temp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun MobilePaymentNotice(
    webUrl:
    String,
    modifier:
    Modifier = Modifier,
) {
    val clipboardManager =
        LocalClipboardManager.current

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 2.dp,
                    vertical = 4.dp,
                ),

        shape =
            MaterialTheme
                .shapes
                .medium,

        color =
            MaterialTheme
                .colorScheme
                .surfaceVariant
                .copy(
                    alpha = 0.45f
                ),

        border =
            BorderStroke(
                width = 1.dp,

                color =
                    MaterialTheme
                        .colorScheme
                        .outlineVariant,
            ),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    horizontal = 14.dp,
                    vertical = 12.dp,
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    6.dp
                ),
        ) {
            Text(
                text =
                    "移动端支付尚未正式启用",

                style =
                    MaterialTheme
                        .typography
                        .labelLarge,

                fontWeight =
                    FontWeight.SemiBold,
            )

            Text(
                text =
                    "可参考页面信息前往网页版完成购买。",

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
                    "请勿在当前页面点击购买/支付",

                style =
                    MaterialTheme
                        .typography
                        .bodySmall,

                fontWeight =
                    FontWeight.SemiBold,

                color =
                    MaterialTheme
                        .colorScheme
                        .error,
            )

            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    text =
                        webUrl,

                    modifier =
                        Modifier.weight(1f),

                    style =
                        MaterialTheme
                            .typography
                            .bodySmall,

                    color =
                        MaterialTheme
                            .colorScheme
                            .primary,
                )

                TextButton(
                    onClick = {
                        clipboardManager
                            .setText(
                                AnnotatedString(
                                    webUrl
                                )
                            )
                    },
                ) {
                    Text(
                        text =
                            "复制网址"
                    )
                }
            }
        }
    }
}