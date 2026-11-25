package top.role.android.feature.entity.service

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import top.role.android.feature.content.picker.*
import top.role.android.core.asset.AssetUploadService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityServiceCreateScreen(
    pickerRepository: ContentPickerRepository,
    assetUploadService: AssetUploadService,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSubmit: (
        String,
        String,
        String,
        String,
        String,
        List<EntityServiceReference>,
    ) -> Unit,
    onBack: () -> Unit,
) {
    var requestType by rememberSaveable {
        mutableStateOf("advice")
    }

    var title by rememberSaveable {
        mutableStateOf("")
    }

    var contextSummary by rememberSaveable {
        mutableStateOf("")
    }

    var coreQuestion by rememberSaveable {
        mutableStateOf("")
    }

    var expectedHelp by rememberSaveable {
        mutableStateOf("")
    }

    var typeExpanded by remember {
        mutableStateOf(false)
    }

    val types = listOf(
        "advice",
        "feedback",
        "support",
        "collaboration",
        "other",
    )

    val selectedReferences = remember {
        mutableStateListOf<ContentSelection>()
    }

    var showReferencePicker by remember {
        mutableStateOf(false)
    }

    val canSubmit =
        title.isNotBlank() &&
                contextSummary.isNotBlank() &&
                coreQuestion.isNotBlank() &&
                expectedHelp.isNotBlank() &&
                !isSubmitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("发起请求")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !isSubmitting,
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 20.dp,
                    vertical = 12.dp,
                ),
            verticalArrangement =
                Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "请清楚描述你的问题与期望获得的帮助。",
                style = MaterialTheme.typography.bodyMedium,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // 请求类型
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "请求类型",
                    style = MaterialTheme.typography.labelLarge,
                )

                Box {
                    OutlinedButton(
                        onClick = {
                            typeExpanded = true
                        },
                        enabled = !isSubmitting,
                    ) {
                        Text(
                            EntityServiceTypes.label(requestType)
                        )
                    }

                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = {
                            typeExpanded = false
                        },
                    ) {
                        types.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        EntityServiceTypes.label(type)
                                    )
                                },
                                onClick = {
                                    requestType = type
                                    typeExpanded = false
                                },
                            )
                        }
                    }
                }
            }

            // 标题
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                },
                label = {
                    Text("标题")
                },
                placeholder = {
                    Text("简单概括这次请求")
                },
                singleLine = true,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )

            // 背景说明
            OutlinedTextField(
                value = contextSummary,
                onValueChange = {
                    contextSummary = it
                },
                label = {
                    Text("背景说明")
                },
                placeholder = {
                    Text("说明当前背景、已有信息或上下文")
                },
                minLines = 3,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )

            // 核心问题
            OutlinedTextField(
                value = coreQuestion,
                onValueChange = {
                    coreQuestion = it
                },
                label = {
                    Text("核心问题")
                },
                placeholder = {
                    Text("你最希望解决的问题是什么？")
                },
                minLines = 3,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )

            // 期望帮助
            OutlinedTextField(
                value = expectedHelp,
                onValueChange = {
                    expectedHelp = it
                },
                label = {
                    Text("期望帮助")
                },
                placeholder = {
                    Text("你希望空间提供什么形式的帮助或结果？")
                },
                minLines = 3,
                enabled = !isSubmitting,
                modifier = Modifier.fillMaxWidth(),
            )

            HorizontalDivider()

            Text(
                text = "参考内容（可选）",
                style = MaterialTheme.typography.titleSmall,
            )

            OutlinedButton(
                onClick = { showReferencePicker = true },
                enabled = !isSubmitting,
            ) {
                Text("添加参考内容")
            }

            selectedReferences.forEach { reference ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = reference.title.ifBlank { "无标题" },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )

                    TextButton(
                        onClick = {
                            selectedReferences.remove(reference)
                        },
                        enabled = !isSubmitting,
                    ) {
                        Text("移除")
                    }
                }
            }

            // 提交错误
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            // 提交按钮
            Button(
                onClick = {
                    onSubmit(
                        requestType,
                        title,
                        contextSummary,
                        coreQuestion,
                        expectedHelp,
                        selectedReferences.map {
                            it.toServiceRef("context")
                        },
                    )
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("提交请求")
                }
            }

            Spacer(Modifier.height(24.dp))


            if (showReferencePicker) {
                ContentPickerDialog(
                    repository = pickerRepository,
                    assetUploadService = assetUploadService,
                    multiple = true,
                    initialSelected = selectedReferences.toList(),
                    onDismiss = {
                        showReferencePicker = false
                    },
                    onConfirm = { selected ->
                        selectedReferences.clear()
                        selectedReferences.addAll(selected)
                        showReferencePicker = false
                    },
                )
            }

        }
    }
}