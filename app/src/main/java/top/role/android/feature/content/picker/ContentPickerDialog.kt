
package top.role.android.feature.content.picker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ContentPickerDialog(
    repository: ContentPickerRepository,
    multiple: Boolean,
    initialSelected: List<ContentSelection> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (List<ContentSelection>) -> Unit,
) {
    // 弹窗内只修改草稿，点击确定才提交给外层
    var draft by remember {
        mutableStateOf(initialSelected)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.82f)
                .imePadding(),
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement =
                    Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = if (multiple) {
                        "添加参考内容"
                    } else {
                        "选择内容"
                    },
                    style = MaterialTheme.typography.titleLarge,
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    ContentPickerPanel(
                        repository = repository,
                        multiple = multiple,
                        selected = draft,
                        onSelectionChange = { draft = it },
                    )
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }

                    Button(
                        enabled = multiple || draft.size == 1,
                        onClick = {
                            onConfirm(draft)
                        },
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}