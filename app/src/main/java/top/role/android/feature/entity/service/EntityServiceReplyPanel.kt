package top.role.android.feature.entity.service

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import top.role.android.core.asset.AssetUploadService
import top.role.android.feature.content.picker.ContentPickerPanel
import top.role.android.feature.content.picker.ContentPickerRepository
import top.role.android.feature.content.picker.ContentSelection

@Composable
fun EntityServiceReplyPanel(
    repository: ContentPickerRepository,
    assetUploadService: AssetUploadService,
    responseNote: String,
    onResponseNoteChange: (String) -> Unit,
    selectedResult: ContentSelection?,
    onSelectedResultChange: (ContentSelection?) -> Unit,
    enabled: Boolean,
    onUploadBusyChange: (Boolean) -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = "回复请求",
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = responseNote,
            onValueChange = onResponseNoteChange,
            label = {
                Text("回复说明（可选）")
            },
            placeholder = {
                Text("说明处理意见、交付结果或需要注意的事项")
            },
            minLines = 4,
            maxLines = 8,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        Text(
            text = "交付内容（可选）",
            style = MaterialTheme.typography.titleSmall,
        )

        ContentPickerPanel(
            repository = repository,
            assetUploadService = assetUploadService,
            multiple = false,
            selected = listOfNotNull(selectedResult),
            onSelectionChange = { selected ->
                onSelectedResultChange(
                    selected.firstOrNull()
                )
            },
            enabled = enabled,
            onUploadBusyChange = onUploadBusyChange,
        )

        Text(
            text = "回复说明和交付内容至少提供一项。提交后将完成并关闭本次服务请求。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}