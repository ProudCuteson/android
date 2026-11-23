package top.role.android.feature.entity.service

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EntityServiceManageActions(
    service: EntityServiceItem,
    isRunning: Boolean,
    onMarkInReview: () -> Unit,
    onAccept: () -> Unit,
    onDecline: (String) -> Unit,
) {
    var dialog by rememberSaveable(service.id) {
        mutableStateOf<String?>(null)
    }

    var declineNote by rememberSaveable(service.id) {
        mutableStateOf("")
    }

    val status = service.status

    val canReview = status == EntityServiceStatuses.OPEN
    val canAccept = EntityServiceStatuses.canAccept(status)
    val canDecline = EntityServiceStatuses.canDecline(status)
    val hasIncentive = (service.incentiveC ?: 0.0) > 0.0

    if (canReview || canAccept || canDecline) {
        Column(
            verticalArrangement =
                Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "管理操作",
                style = MaterialTheme.typography.titleSmall,
            )

            if (canAccept && hasIncentive) {
                Text(
                    text = "此请求包含 Credits 激励，暂请通过 Web 管理端处理受理。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme
                        .colorScheme.onSurfaceVariant,
                )
            }

            if (canReview || canAccept) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp),
                ) {
                    if (canReview) {
                        OutlinedButton(
                            onClick = onMarkInReview,
                            enabled = !isRunning,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("标记处理中")
                        }
                    }

                    if (canAccept) {
                        Button(
                            onClick = { dialog = "accept" },
                            enabled = !isRunning && !hasIncentive,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("接受请求")
                        }
                    }
                }
            }

            if (canDecline) {
                OutlinedButton(
                    onClick = {
                        declineNote = ""
                        dialog = "decline"
                    },
                    enabled = !isRunning,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("拒绝请求")
                }
            }
        }
    }

    when (dialog) {
        "accept" -> {
            AlertDialog(
                onDismissRequest = {
                    if (!isRunning) dialog = null
                },
                title = { Text("接受请求") },
                text = {
                    Text("确认接受这条服务请求吗？")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialog = null
                            onAccept()
                        },
                        enabled = !isRunning,
                    ) {
                        Text("确认接受")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { dialog = null },
                        enabled = !isRunning,
                    ) {
                        Text("取消")
                    }
                },
            )
        }

        "decline" -> {
            AlertDialog(
                onDismissRequest = {
                    if (!isRunning) dialog = null
                },
                title = { Text("拒绝请求") },
                text = {
                    OutlinedTextField(
                        value = declineNote,
                        onValueChange = {
                            declineNote = it
                        },
                        label = {
                            Text("处理说明（可选）")
                        },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dialog = null
                            onDecline(declineNote)
                        },
                        enabled = !isRunning,
                    ) {
                        Text("确认拒绝")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { dialog = null },
                        enabled = !isRunning,
                    ) {
                        Text("取消")
                    }
                },
            )
        }
    }
}