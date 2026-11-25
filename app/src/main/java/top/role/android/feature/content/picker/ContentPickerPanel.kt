package top.role.android.feature.content.picker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import top.role.android.core.asset.AssetUploadService

private val contentTypes = listOf(
    "note" to "Note",
    "doc" to "Doc",
    "slide" to "Slide",
    "fileAsset" to "PDF",
)

@Composable
fun ContentPickerPanel(
    repository: ContentPickerRepository,
    assetUploadService: AssetUploadService,
    multiple: Boolean,
    selected: List<ContentSelection>,
    onSelectionChange: (List<ContentSelection>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onUploadBusyChange: (Boolean) -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val uploadSession = remember(assetUploadService) {
        assetUploadService.createSession()
    }

    val uploadState by uploadSession.state.collectAsState()

    val uploadBusy = uploadState.busy

    val interactionEnabled = enabled && !uploadBusy

    val currentOnUploadBusyChange by rememberUpdatedState(
        onUploadBusyChange
    )

    LaunchedEffect(uploadBusy) {
        currentOnUploadBusyChange(uploadBusy)
    }

    DisposableEffect(Unit) {
        onDispose {
            currentOnUploadBusyChange(false)
        }
    }

    var type by rememberSaveable {
        mutableStateOf("note")
    }

    var search by rememberSaveable {
        mutableStateOf("")
    }

    var rows by remember {
        mutableStateOf<List<ContentPickerItem>>(emptyList())
    }

    var cursor by remember {
        mutableStateOf<String?>(null)
    }

    var isDone by remember {
        mutableStateOf(true)
    }

    var firstLoading by remember {
        mutableStateOf(false)
    }

    var moreLoading by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    var reloadKey by remember {
        mutableIntStateOf(0)
    }

    var activeKey by remember {
        mutableStateOf("")
    }

    val requestKey = "$type:${search.trim()}:$reloadKey"

    /**
     * 切换类型、搜索或重试时重新加载第一页。
     */
    LaunchedEffect(repository, requestKey) {
        val key = requestKey
        activeKey = key

        rows = emptyList()
        cursor = null
        isDone = true
        firstLoading = true
        moreLoading = false
        error = null

        if (search.isNotBlank()) {
            delay(300)
        }

        try {
            val page = repository.loadPage(
                type = type,
                query = search.trim(),
                cursor = null,
                pageSize = 20,
            )

            if (activeKey == key) {
                if (page.ok) {
                    rows = page.page
                    cursor = page.continueCursor
                    isDone = page.isDone
                } else {
                    error = page.code ?: "加载失败"
                }
            }
        } catch (cancel: CancellationException) {
            throw cancel
        } catch (e: Exception) {
            if (activeKey == key) {
                error = e.message ?: "加载失败"
            }
        } finally {
            if (activeKey == key) {
                firstLoading = false
            }
        }
    }

    fun toggle(item: ContentPickerItem) {
        if (!interactionEnabled) return

        // 正式交付的 Note 必须有标题
        if (
            !multiple &&
            item.type == "note" &&
            item.title.isBlank()
        ) {
            return
        }

        val selection = item.asSelection()

        if (!multiple) {
            onSelectionChange(listOf(selection))
            return
        }

        val exists = selected.any {
            it.type == selection.type &&
                    it.id == selection.id
        }

        val next = if (exists) {
            selected.filterNot {
                it.type == selection.type &&
                        it.id == selection.id
            }
        } else {
            if (selected.size >= 20) return
            selected + selection
        }

        onSelectionChange(next)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 四种资源分类
        Row(
            modifier = Modifier.horizontalScroll(
                rememberScrollState()
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            contentTypes.forEach { (value, label) ->
                FilterChip(
                    selected = type == value,
                    onClick = { type = value },
                    enabled = interactionEnabled,
                    label = { Text(label) },
                )
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = {
                search = it.take(100)
            },
            label = {
                Text(
                    if (type == "fileAsset") {
                        "搜索文件名"
                    } else {
                        "搜索标题"
                    }
                )
            },
            singleLine = true,
            enabled = interactionEnabled,
            modifier = Modifier.fillMaxWidth(),
        )

        if (type == "fileAsset") {

            ContentPickerUploadSection(
                session = uploadSession,
                enabled = enabled,
                onUploaded = {

                    search = ""

                    reloadKey++
                },
            )
        }

        // 已选内容独立显示，切换分类不丢失
        if (selected.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = if (multiple) {
                        "已选择 ${selected.size} / 20"
                    } else {
                        "当前交付内容"
                    },
                    style = MaterialTheme.typography.labelMedium,
                )

                selected.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = item.title.ifBlank { "无标题" },
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            modifier = Modifier.weight(1f),
                        )

                        TextButton(
                            onClick = {
                                onSelectionChange(
                                    selected.filterNot {
                                        it.type == item.type &&
                                                it.id == item.id
                                    }
                                )
                            },
                            enabled = interactionEnabled,
                        ) {
                            Text("移除")
                        }
                    }
                }
            }
        }

        if (!multiple && type == "note") {
            Text(
                text = "无标题 Note 不能作为正式交付内容",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (firstLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        error?.let { message ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f),
                )

                TextButton(
                    onClick = { reloadKey++ },
                    enabled = interactionEnabled,
                ) {
                    Text("重试")
                }
            }
        }

        if (!firstLoading && rows.isEmpty() && error == null) {
            Text(
                text = "暂无可选择的内容",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        rows.forEach { item ->
            val checked = selected.any {
                it.type == item.type && it.id == item.id
            }

            val selectable = interactionEnabled && (
                    multiple ||
                            item.type != "note" ||
                            item.title.isNotBlank()
                    )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        enabled = selectable,
                        onClick = { toggle(item) },
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement =
                    Arrangement.spacedBy(10.dp),
            ) {
                if (multiple) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                        enabled = selectable,
                    )
                } else {
                    RadioButton(
                        selected = checked,
                        onClick = null,
                        enabled = selectable,
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement =
                        Arrangement.spacedBy(3.dp),
                ) {
                    Text(
                        text = item.title.ifBlank { "无标题" },
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    item.description
                        ?.takeIf { it.isNotBlank() }
                        ?.let { description ->
                            Text(
                                text = description,
                                maxLines = 2,
                                style = MaterialTheme
                                    .typography.bodySmall,
                                color = MaterialTheme
                                    .colorScheme.onSurfaceVariant,
                            )
                        }
                }
            }

            HorizontalDivider()
        }

        if (!firstLoading && !isDone && cursor != null) {
            TextButton(
                enabled = interactionEnabled && !moreLoading,
                onClick = {
                    if (moreLoading) return@TextButton

                    val key = activeKey
                    val nextCursor =
                        cursor ?: return@TextButton

                    moreLoading = true

                    scope.launch {
                        try {
                            val page = repository.loadPage(
                                type = type,
                                query = search.trim(),
                                cursor = nextCursor,
                                pageSize = 20,
                            )

                            if (activeKey == key) {
                                if (page.ok) {
                                    rows = (
                                            rows + page.page
                                            ).distinctBy {
                                            "${it.type}:${it.id}"
                                        }

                                    cursor = page.continueCursor
                                    isDone = page.isDone
                                    error = null
                                } else {
                                    error = page.code
                                        ?: "加载更多失败"
                                }
                            }
                        } catch (cancel: CancellationException) {
                            throw cancel
                        } catch (e: Exception) {
                            if (activeKey == key) {
                                error = e.message
                                    ?: "加载更多失败"
                            }
                        } finally {
                            if (activeKey == key) {
                                moreLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (moreLoading) "加载中…"
                    else "加载更多"
                )
            }
        }
    }
}