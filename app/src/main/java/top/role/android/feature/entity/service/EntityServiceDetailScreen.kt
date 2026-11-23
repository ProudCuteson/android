
package top.role.android.feature.entity.service

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import top.role.android.feature.content.picker.ContentPickerRepository
import java.text.DateFormat
import java.util.Date
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Person
import coil3.compose.AsyncImage
import androidx.compose.runtime.saveable.rememberSaveable
import top.role.android.feature.content.picker.ContentSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityServiceDetailScreen(
    state: EntityServiceUiState,
    pickerRepository: ContentPickerRepository,
    onWithdraw: () -> Unit,
    onMarkInReview: () -> Unit,
    onAccept: () -> Unit,
    onDecline: (String) -> Unit,
    onClose: (EntityServiceReference?, String) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit,
) {
    var showWithdrawConfirm by remember {
        mutableStateOf(false)
    }

    val service = state.serviceDetail

    var replyNote by rememberSaveable(service?.id) {
        mutableStateOf("")
    }

    var selectedResult by remember(service?.id) {
        mutableStateOf<ContentSelection?>(null)
    }

    val canManage = state.mode == "managed" &&
            service != null

    val canReplyAndClose = canManage &&
            EntityServiceStatuses.canClose(service.status)

    val hasReply = replyNote.isNotBlank() ||
            selectedResult != null

    val canWithdraw = state.mode == "mine" &&
        service?.isRequester == true &&
        EntityServiceStatuses.canWithdraw(service.status)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("请求详情") },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !state.isActionRunning,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },

        bottomBar = {
            if (canWithdraw || canReplyAndClose) {
                Surface(
                    tonalElevation = 3.dp,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(
                                horizontal = 20.dp,
                                vertical = 12.dp,
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        state.actionErrorMessage
                            ?.takeIf { it.isNotBlank() }
                            ?.let { message ->
                                Text(
                                    text = message,
                                    style = MaterialTheme
                                        .typography.bodySmall,
                                    color = MaterialTheme
                                        .colorScheme.error,
                                )
                            }

                        // 会员端：保留撤回请求
                        if (canWithdraw) {
                            OutlinedButton(
                                onClick = {
                                    showWithdrawConfirm = true
                                },
                                enabled = !state.isActionRunning,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    if (state.isActionRunning) {
                                        "处理中..."
                                    } else {
                                        "撤回请求"
                                    }
                                )
                            }
                        }

                        // 管理端：仅保留正式回复按钮
                        if (canReplyAndClose) {
                            Button(
                                onClick = {
                                    onClose(
                                        selectedResult?.toServiceRef(
                                            "deliverable"
                                        ),
                                        replyNote.trim(),
                                    )
                                },
                                enabled =
                                    !state.isActionRunning && hasReply,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(
                                    if (state.isActionRunning) {
                                        "提交中..."
                                    } else {
                                        "回复并关闭"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },

        ) { innerPadding ->

        when {
            state.detailErrorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = state.detailErrorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(onClick = onRetry) {
                        Text("重试")
                    }
                }
            }

            state.isDetailLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.serviceDetail == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("暂无请求详情")
                }
            }


            else -> {
                EntityServiceDetailContent(
                    service = service,
                    isManager = canManage,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    footer = {
                        // 原管理操作移入滚动正文
                        if (canManage) {
                            HorizontalDivider()

                            EntityServiceManageActions(
                                service = service,
                                isRunning = state.isActionRunning,
                                onMarkInReview = onMarkInReview,
                                onAccept = onAccept,
                                onDecline = onDecline,
                            )
                        }

                        // 管理员的正式回复工作区
                        if (canReplyAndClose) {
                            HorizontalDivider()

                            EntityServiceReplyPanel(
                                repository = pickerRepository,
                                responseNote = replyNote,
                                onResponseNoteChange = {
                                    replyNote = it
                                },
                                selectedResult = selectedResult,
                                onSelectedResultChange = {
                                    selectedResult = it
                                },
                                enabled = !state.isActionRunning,
                            )
                        }
                    },
                )
            }

        }

        if (showWithdrawConfirm) {
            AlertDialog(
                onDismissRequest = {
                    if (!state.isActionRunning) {
                        showWithdrawConfirm = false
                    }
                },
                title = {
                    Text("撤回请求")
                },
                text = {
                    Text("确定撤回这条服务请求吗？")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showWithdrawConfirm = false
                            onWithdraw()
                        },
                    ) {
                        Text("确认撤回")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showWithdrawConfirm = false
                        },
                    ) {
                        Text("取消")
                    }
                },
            )
        }

    }
}

@Composable
private fun EntityServiceDetailContent(
    service: EntityServiceItem,
    isManager: Boolean,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp,
            )
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = service.title,
                style = MaterialTheme.typography.titleLarge,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = EntityServiceTypes.label(
                        service.requestType
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )

                Text(
                    text = EntityServiceStatuses.label(
                        service.status
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Text(
                text = formatServiceTime(service.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isManager) {
                ServiceRequesterInfo(
                    requester = service.requester,
                )
            }

        }

        HorizontalDivider()

        ServiceDetailSection(
            title = "背景说明",
            content = service.contextSummary,
        )

        ServiceDetailSection(
            title = "核心问题",
            content = service.coreQuestion,
        )

        ServiceDetailSection(
            title = "期望帮助",
            content = service.expectedHelp,
        )

        service.targetReplyAt?.let { timestamp ->
            ServiceDetailSection(
                title = "预计回复时间",
                content = formatServiceTime(timestamp),
            )
        }

        service.resultRef?.let { ref ->
            HorizontalDivider()

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "交付内容",
                    style = MaterialTheme.typography.titleSmall,
                )

                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = ref.label
                                ?.takeIf { it.isNotBlank() }
                                ?: "未命名内容",
                            style =
                                MaterialTheme.typography.bodyMedium,
                        )

                        Text(
                            text = referenceTypeLabel(
                                ref.targetType
                            ),
                            style =
                                MaterialTheme.typography.bodySmall,
                            color = MaterialTheme
                                .colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        service.responseNote
            ?.takeIf { it.isNotBlank() }
            ?.let { note ->
                HorizontalDivider()

                ServiceDetailSection(
                    title = "空间处理说明",
                    content = note,
                )
            }

        service.resultLink
            ?.takeIf { it.isNotBlank() }
            ?.let { link ->
                ServiceDetailSection(
                    title = service.resultLinkLabel
                        ?.takeIf { it.isNotBlank() }
                        ?: "处理结果链接",
                    content = link,
                )
            }

        val references = service.references.orEmpty()

        if (references.isNotEmpty()) {
            HorizontalDivider()

            Column(
                verticalArrangement =
                    Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "关联内容",
                    style = MaterialTheme.typography.titleSmall,
                )

                references.forEach { reference ->
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text = reference.label
                                    ?.takeIf { it.isNotBlank() }
                                    ?: referenceTypeLabel(
                                        reference.targetType
                                    ),
                                style =
                                    MaterialTheme.typography.bodyMedium,
                            )

                            Text(
                                text = referenceTypeLabel(
                                    reference.targetType
                                ),
                                style =
                                    MaterialTheme.typography.bodySmall,
                                color = MaterialTheme
                                    .colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        service.incentiveC
            ?.takeIf { it > 0.0 }
            ?.let { amount ->
                ServiceDetailSection(
                    title = "激励 Credits",
                    content = "${amount.toLong()} Credits",
                )
            }

        service.resolvedAt?.let { timestamp ->
            HorizontalDivider()

            Text(
                text = "处理时间：${formatServiceTime(timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        footer()

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ServiceRequesterInfo(
    requester: EntityServiceRequester?,
) {
    val displayName = requester
        ?.displayName
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: "未设置昵称"

    val avatarUrl = requester
        ?.avatarUrl
        ?.trim()
        ?.takeIf { it.isNotEmpty() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center,
        ) {
            // 无头像或图片加载失败时的默认图标
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp),
            )

            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "请求人",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ServiceDetailSection(
    title: String,
    content: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
        )

        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun referenceTypeLabel(type: String): String =
    when (type) {
        "doc" -> "文档"
        "slide" -> "演示文稿"
        "note" -> "记录"
        "fileAsset" -> "文件"
        else -> "关联内容"
    }

private fun formatServiceTime(timestamp: Double): String {
    return DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT,
    ).format(Date(timestamp.toLong()))
}
