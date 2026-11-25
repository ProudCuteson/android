package top.role.android.feature.content.picker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import top.role.android.core.asset.AssetAccessMode
import top.role.android.core.asset.AssetUploadOptions
import top.role.android.core.asset.AssetUploadSession
import top.role.android.core.asset.AssetUploadStatus
import top.role.android.core.asset.displayLabel

@Composable
fun ContentPickerUploadSection(
    session: AssetUploadSession,
    onUploaded: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val state by session.state.collectAsState()

    val currentOnUploaded by rememberUpdatedState(
        onUploaded
    )

    val currentEnabled by rememberUpdatedState(
        enabled
    )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->

        if (
            uri != null &&
            currentEnabled &&
            !session.state.value.busy
        ) {
            scope.launch {
                try {

                    session.upload(
                        uri = uri,
                        options = AssetUploadOptions(
                            accessMode = AssetAccessMode.Private,
                            allowedMimes = setOf(
                                "application/pdf"
                            ),
                        ),
                    )

                    currentOnUploaded()

                } catch (cancel: CancellationException) {

                    throw cancel

                } catch (_: Exception) {

                    // 错误由 AssetUploadSession 统一管理。
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.spacedBy(12.dp),
        ) {

            OutlinedButton(
                onClick = {
                    launcher.launch(
                        arrayOf("application/pdf")
                    )
                },
                enabled = enabled &&
                        !state.busy &&
                        !state.canRetryFinalize,
            ) {
                Text("上传 PDF")
            }

            if (state.busy) {

                Text(
                    text = state.status.displayLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme
                        .colorScheme.onSurfaceVariant,
                )
            }
        }

        if (state.busy) {

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (state.status == AssetUploadStatus.Done) {

            Text(
                text = "上传成功，可在下方列表中选择",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        if (state.status == AssetUploadStatus.Error) {

            Text(
                text = state.error ?: "文件上传失败",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )

            if (state.canRetryFinalize) {

                Row(
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp),
                ) {

                    TextButton(
                        enabled = enabled && !state.busy,
                        onClick = {
                            scope.launch {
                                try {

                                    session.retryFinalize()

                                    currentOnUploaded()

                                } catch (
                                    cancel: CancellationException
                                ) {

                                    throw cancel

                                } catch (_: Exception) {

                                    // Session 负责错误状态。
                                }
                            }
                        },
                    ) {
                        Text("重试确认")
                    }

                    TextButton(
                        enabled = enabled && !state.busy,
                        onClick = {
                            session.reset()
                        },
                    ) {
                        Text("放弃确认")
                    }
                }

            } else {

                Text(
                    text = "可以重新选择文件上传。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme
                        .colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}