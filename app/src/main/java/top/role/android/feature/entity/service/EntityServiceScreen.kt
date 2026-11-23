
package top.role.android.feature.entity.service

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.Composable
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.feature.content.picker.ContentPickerRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityServiceScreen(
    state: EntityServiceUiState,
    pickerRepository: ContentPickerRepository,
    onOpenReference: (
        EntityServiceReference,
        EntityServiceReferenceSource,
    ) -> Unit,
    onRetry: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onCloseDetail: () -> Unit,
    onRetryDetail: () -> Unit,
    onWithdraw: () -> Unit,
    onSubmit: (
        String, String, String, String, String,
        List<EntityServiceReference>,
    ) -> Unit,
    onConsumeCreated: () -> Unit,
    onClearSubmitError: () -> Unit,
    onMarkInReview: () -> Unit,
    onAccept: () -> Unit,
    onDecline: (String) -> Unit,
    onClose: (EntityServiceReference?, String) -> Unit,
    onBack: () -> Unit,
) {
    val isManager = state.mode == "managed"

    var showCreate by rememberSaveable {
        mutableStateOf(false)
    }

    val closeCreate: () -> Unit = {
        if (!state.isSubmitting) {
            showCreate = false
            onClearSubmitError()
        }
    }

    LaunchedEffect(state.createdServiceId) {
        if (state.createdServiceId != null) {
            showCreate = false
            onConsumeCreated()
        }
    }

    // 同一路由内部切换页面时，处理系统返回键
    BackHandler(
        enabled = showCreate,
    ) {
        if (!state.isSubmitting) {
            closeCreate()
        }
    }

    if (showCreate) {
        EntityServiceCreateScreen(
            pickerRepository = pickerRepository,
            isSubmitting = state.isSubmitting,
            errorMessage = state.submitError,
            onSubmit = onSubmit,
            onBack = closeCreate,
        )
        return
    }

    // 请求详情：在当前路由内部打开
    BackHandler(
        enabled = state.selectedServiceId != null,
    ) {
        if (!state.isActionRunning) {
            onCloseDetail()
        }
    }

    if (state.selectedServiceId != null) {
        EntityServiceDetailScreen(
            state = state,
            pickerRepository = pickerRepository,
            onOpenReference = onOpenReference,
            onRetry = onRetryDetail,
            onWithdraw = onWithdraw,
            onMarkInReview = onMarkInReview,
            onAccept = onAccept,
            onDecline = onDecline,
            onClose = onClose,
            onBack = onCloseDetail,
        )
        return
    }

    Scaffold(

        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isManager) "管理请求"
                        else "服务请求"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    if (!isManager) {
                        TextButton(
                            onClick = {
                                onClearSubmitError()
                                showCreate = true
                            },
                        ) {
                            Text("新建")
                        }
                    }
                },
            )
        },

        ) { innerPadding ->

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                    )

                    Spacer(Modifier.height(12.dp))

                    OutlinedButton(onClick = onRetry) {
                        Text("重试")
                    }
                }
            }


            state.services.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (isManager) {
                            "暂无收到的服务请求"
                        } else {
                            "暂无服务请求"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (!isManager) {
                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = "可以向空间管理者提出咨询、反馈或协作请求",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Spacer(Modifier.height(20.dp))

                        OutlinedButton(
                            onClick = {
                                onClearSubmitError()
                                showCreate = true
                            },
                        ) {
                            Text("发起请求")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 12.dp,
                    ),
                    verticalArrangement =
                        Arrangement.spacedBy(10.dp),
                ) {
                    item {
                        Text(
                            text = if (isManager) {
                                "收到的请求"
                            } else {
                                "我的请求"
                            },
                            style =
                                MaterialTheme.typography.titleMedium,
                        )

                        Spacer(Modifier.height(6.dp))
                    }

                    items(
                        items = state.services,
                        key = { it.id },
                    ) { service ->

                        EntityServiceListItem(
                            service = service,
                            onClick = {
                                onOpenDetail(service.id)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EntityServiceListItem(
    service: EntityServiceItem,
    onClick: () -> Unit,
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = service.title,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = service.coreQuestion,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = EntityServiceTypes.label(service.requestType),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )

                Text(
                    text = EntityServiceStatuses.label(service.status),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            service.responseNote
                ?.takeIf { it.isNotBlank() }
                ?.let { note ->

                    HorizontalDivider()

                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
        }
    }
}