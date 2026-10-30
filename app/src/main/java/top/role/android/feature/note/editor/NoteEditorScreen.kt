package top.role.android.feature.note.editor

import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import top.role.android.core.data.image.ImageRepository
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.data.note.ai.NoteAiMode
import top.role.android.core.data.note.ai.NoteAiRemoteDataSource
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.model.note.content.BlockContent
import top.role.android.core.model.note.content.NoteContentCodec
import top.role.android.feature.note.editor.richtext.RichTextEditorField
import top.role.android.feature.note.editor.richtext.RichTextEditorState
import top.role.android.feature.note.editor.richtext.RichTextToolbar
import top.role.android.feature.note.editor.richtext.RichTextReadingView

@Composable
fun NoteEditorRoute(
    ownerUserId: String,
    noteLocalRepository: NoteLocalRepository,
    noteAiRemoteDataSource: NoteAiRemoteDataSource,
    noteImageService: NoteImageService,
    imageRepository: ImageRepository,
    initialLocalKey: String? = null,
    initialDayKey: String? = null,
    openAsDayOnly: Boolean = false,
    createAsNormal: Boolean = false,
    createAsDated: Boolean = false,
    onBack: () -> Unit,
) {
    val viewModel: NoteEditorViewModel =
        viewModel(
            factory =
                NoteEditorViewModelFactory(
                    ownerUserId = ownerUserId,
                    noteLocalRepository = noteLocalRepository,
                    noteAiRemoteDataSource = noteAiRemoteDataSource,
                    noteImageService = noteImageService,
                    imageRepository = imageRepository,
                    initialLocalKey = initialLocalKey,
                    initialDayKey = initialDayKey,
                    openAsDayOnly = openAsDayOnly,
                    createAsNormal = createAsNormal,
                    createAsDated = createAsDated,
                ),
        )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.saveNow()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val scope = rememberCoroutineScope()

    NoteEditorScreen(
        state = state,
        onTitleChange = viewModel::updateTitle,
        onRichContentChange = viewModel::updateRichContent,
        onChecklistChange = viewModel::setChecklistChecked,
        onPinnedChange = viewModel::setPinned,
        onStartEditing = viewModel::startEditing,
        onFinishEditing = viewModel::finishEditing,
        onRunAi = viewModel::runAi,
        onReplaceTextWithAi = viewModel::replaceTextWithAiSuggestion,
        onAppendAiToText = viewModel::appendAiSuggestionToText,
        onUseAiAsTitle = viewModel::useAiSuggestionAsTitle,
        onDismissAiSuggestion = viewModel::clearAiSuggestion,
        onAddImage = viewModel::addImage,
        onRetryImageUpload = viewModel::retryImageUpload,
        onRemoveImage = viewModel::removeImage,
        onUseRemoteConflict = { viewModel.resolveConflictUsingRemote(onDeleted = onBack) },
        onKeepLocalConflict = viewModel::resolveConflictKeepingLocal,
        onSetDayKey = viewModel::setDayKey,
        onBack = {
            scope.launch {
                viewModel.flushAndCleanup()
                onBack()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    onRichContentChange: (BlockContent) -> Unit,
    onChecklistChange: (String, Boolean) -> Unit,
    onPinnedChange: (Boolean) -> Unit,
    onStartEditing: () -> Unit,
    onFinishEditing: () -> Unit,

    onRunAi: (NoteAiMode) -> Unit,
    onReplaceTextWithAi: () -> Unit,
    onAppendAiToText: () -> Unit,
    onUseAiAsTitle: () -> Unit,
    onDismissAiSuggestion: () -> Unit,

    onAddImage: (Uri) -> Unit,
    onRetryImageUpload: (String) -> Unit,
    onRemoveImage: (String) -> Unit,

    onUseRemoteConflict: () -> Unit,
    onKeepLocalConflict: () -> Unit,
    onBack: () -> Unit,
    onSetDayKey: (String?) -> Unit,
) {
    var showConflictDialog by remember { mutableStateOf(false) }
    val hasConflict = state.syncState == NoteSyncState.Conflict
    val canResolveConflict = hasConflict && state.conflict != null

    LaunchedEffect(
        hasConflict,
        canResolveConflict,
    ) {
        if (!hasConflict || !canResolveConflict) {
            showConflictDialog = false
        }
    }

    // One editor session per note/editing entry or explicit AI text replacement.
    // Room auto-save changes do not recreate TextFieldState and disturb IME/selection.
    val richEditor = remember(
        state.localKey,
        state.editorResetVersion,
        state.isEditing,
    ) {
        RichTextEditorState(
            localEditorContent(state.contentJson, state.text)
        )
    }

    LaunchedEffect(richEditor, state.isEditing) {
        if (!state.isEditing) return@LaunchedEffect
        snapshotFlow {
            Triple(
                richEditor.field.text.toString(),
                richEditor.field.selection,
                richEditor.field.composition,
            )
        }.collect { richEditor.onNativeStateChanged() }
    }

    LaunchedEffect(richEditor, state.isEditing) {
        if (!state.isEditing) return@LaunchedEffect
        var lastDelivered = richEditor.export()
        snapshotFlow {
            // An IME composing range is not committed note content.
            if (richEditor.field.composition == null) richEditor.export() else null
        }.filterNotNull().collect { current ->
            if (current != lastDelivered) {
                lastDelivered = current
                onRichContentChange(current)
            }
        }
    }

    val commitEditor: () -> Unit = {
        if (state.isEditing && !hasConflict) {
            onRichContentChange(richEditor.export())
        }
    }

    var showAiPanel by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),

        bottomBar = {
            if (state.isEditing && !hasConflict) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    Column {
                        HorizontalDivider()

                        RichTextToolbar(
                            editor = richEditor,
                            onAddImage = onAddImage,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = 8.dp,
                                ),
                        )

                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            state.onlyDay -> state.dayKey ?: "日记"
                            state.dayKey != null -> state.dayKey
                            else -> "记录"
                        },
                    )
                },
                navigationIcon = {
                    IconButton( onClick = { commitEditor(); onBack() } ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    // Conflict 期间冻结一切本地 mutation。
                    if (state.canEdit && !hasConflict) {
                        IconButton(onClick = { onPinnedChange(!state.pinned)},
                        ) {
                            Icon(
                                imageVector = if (state.pinned) {
                                    Icons.Filled.PushPin
                                } else {
                                    Icons.Outlined.PushPin
                                },
                                contentDescription = if (state.pinned) {"取消置顶"} else {"置顶"},
                            )
                        }
                    }
                    // Reading + Active + no Conflict：显示编辑入口。
                    if (
                        state.canEdit &&
                        !state.isEditing &&
                        !hasConflict
                    ) {
                        IconButton(onClick = onStartEditing) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "编辑",
                            )
                        }
                    }
                    // Editing：显示完成。* Conflict 一旦出现，这个入口也立即冻结。
                    if (state.isEditing && !hasConflict) {
                        TextButton(
                            onClick = { commitEditor(); onFinishEditing() },
                        ) {
                            Text(
                                text = "完成",
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        when {
            state.loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            // fatal load error。 Archived / Conflict 都可能 canEdit=false，不用 canEdit 判断错误页。
            state.errorMessage != null && state.localKey == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
            // Conflict 优先级高于 Editing。 即使由于某种竞态 mode 尚未切回 Reading，也不会继续暴露输入框。
            hasConflict -> {
                NoteReadingContent(
                    state = state,
                    onOpenConflict = {
                        if ( canResolveConflict ) {
                            showConflictDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            state.isEditing -> {
                NoteEditingContent(
                    state = state,
                    onTitleChange = onTitleChange,
                    richEditor = richEditor,
                    onRunAi = onRunAi,
                    onReplaceTextWithAi = onReplaceTextWithAi,
                    onAppendAiToText = onAppendAiToText,
                    onUseAiAsTitle = onUseAiAsTitle,
                    onDismissAiSuggestion = onDismissAiSuggestion,
                    onAddImage = onAddImage,
                    onRetryImageUpload = onRetryImageUpload,
                    onRemoveImage = onRemoveImage,
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onSetDayKey = onSetDayKey,
                )
            }

            else -> {
                NoteReadingContent(
                    state = state,
                    onOpenConflict = {
                        if (canResolveConflict) {
                            showConflictDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onChecklistChange = onChecklistChange,
                )
            }
        }
    }

    if (showAiPanel && state.isEditing && !hasConflict) {
        ModalBottomSheet(
            onDismissRequest = { showAiPanel = false },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                NoteAiToolbar(
                    running = state.aiRunning,
                    suggestion = state.aiSuggestion,
                    suggestionMode = state.aiSuggestionMode,
                    errorMessage = state.aiErrorMessage,
                    onRunAi = onRunAi,
                    onReplaceText = onReplaceTextWithAi,
                    onAppendText = onAppendAiToText,
                    onUseAsTitle = onUseAiAsTitle,
                    onDismissSuggestion = onDismissAiSuggestion,
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    val conflict = state.conflict

    if (showConflictDialog && conflict != null) {
        NoteConflictDialog(
            localTitle = state.title,
            localText = state.text,
            remote = conflict,
            onDismiss = { showConflictDialog = false },
            onUseRemote = {
                showConflictDialog = false
                onUseRemoteConflict()
            },
            onKeepLocal = {
                showConflictDialog = false
                onKeepLocalConflict()
            },
        )
    }
}

/**
 * Reading
 */
@Composable
private fun NoteReadingContent(
    state: NoteEditorUiState,
    onOpenConflict: () -> Unit,
    modifier: Modifier = Modifier,
    onChecklistChange: ((String, Boolean) -> Unit)? = null,
) {
    val hasConflict = state.syncState == NoteSyncState.Conflict

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        EditorMetaRow(state = state)

        if (!state.errorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp,))
            EditorInlineError(message = state.errorMessage)
        }

        if (hasConflict) {
            Spacer(modifier = Modifier.height(12.dp))
            ConflictBanner(
                canResolve = state.conflict != null,
                onClick = onOpenConflict,
            )
        }

        if (state.remoteStatus == NoteRemoteStatus.Archived) {
            Spacer(modifier = Modifier.height(12.dp,),)
            Text(
                text = "已归档 · 取消归档后可继续编辑",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = state.title.takeIf { it.isNotBlank() } ?: "无标题",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        if (state.images.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            NoteImageReading(
                images = state.images,
            )
        }

        if (!state.contentJson.isNullOrBlank() || state.text.isNotBlank()) {
            Spacer(Modifier.height(18.dp))

            val content = remember(
                state.localKey,
                state.contentJson,
                state.text,
            ) {
                localEditorContent(
                    state.contentJson,
                    state.text,
                )
            }

            RichTextReadingView(
                content = content,
                onChecklistChange = if (
                    state.canEdit &&
                    !hasConflict &&
                    state.remoteStatus == NoteRemoteStatus.Active
                ) onChecklistChange else null,
            )
        } else {
            Text(
                text = "暂无正文",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

    }
}

@Composable
private fun NoteEditingContent(
    state: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    richEditor: RichTextEditorState,

    onRunAi: (NoteAiMode) -> Unit,
    onReplaceTextWithAi: () -> Unit,
    onAppendAiToText: () -> Unit,
    onUseAiAsTitle: () -> Unit,
    onDismissAiSuggestion: () -> Unit,

    onAddImage: (Uri) -> Unit,
    onRetryImageUpload: (String) -> Unit,
    onRemoveImage: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSetDayKey: (String?) -> Unit,
) {
    val titleState = rememberTextFieldState(
        initialText = state.title,
    )

    LaunchedEffect(
        titleState,
    ) {
        snapshotFlow { titleState.text.toString() }.collectLatest { value ->
            if (
                value != state.title
            ) {
                onTitleChange(value)
            }
        }
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding( horizontal = 20.dp, vertical = 12.dp ),
        verticalArrangement = Arrangement.Top,
    ) {
        EditorMetaRow(
            state = state,
            onSetDayKey = onSetDayKey,
        )
        if (
            !state.errorMessage.isNullOrBlank()
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            EditorInlineError( message = state.errorMessage)
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(18.dp))
        BasicTextField(
            state = titleState,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme
                .typography
                .titleLarge
                .copy(color = MaterialTheme .colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            lineLimits = TextFieldLineLimits.SingleLine,
            decorator = { innerTextField ->
                Box {
                    if (titleState.text.isEmpty()) {
                        Text(
                            text = "标题",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    innerTextField()
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.images.isNotEmpty()) {
            NoteImageEditor(
                images = state.images,
                onAddImage = onAddImage,
                onRetryUpload = onRetryImageUpload,
                onRemoveImage = onRemoveImage,
                showAddButton = false,
            )

            Spacer(Modifier.height(16.dp))
        }

        RichTextEditorField(editor = richEditor)

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.035f),
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 14.dp, vertical = 14.dp),
        ) {
            NoteAiToolbar(
                running = state.aiRunning,
                suggestion = state.aiSuggestion,
                suggestionMode = state.aiSuggestionMode,
                errorMessage = state.aiErrorMessage,
                onRunAi = onRunAi,
                onReplaceText = onReplaceTextWithAi,
                onAppendText = onAppendAiToText,
                onUseAsTitle = onUseAiAsTitle,
                onDismissSuggestion = onDismissAiSuggestion,
            )
        }

    }
}

@Composable
private fun EditorMetaRow(
    state: NoteEditorUiState,
    onSetDayKey: ((String?) -> Unit)? = null,
) {
    val typeLabel = if (state.onlyDay) "私人日记" else "普通记录"

    val syncLabel = when (state.syncState) {
        NoteSyncState.Synced -> "已同步"
        NoteSyncState.Pending -> "已保存在本机"
        NoteSyncState.Syncing -> "同步中"
        NoteSyncState.Waiting -> "等待同步"
        NoteSyncState.Conflict -> "同步冲突"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "$typeLabel ·",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = syncLabel,
                style = MaterialTheme.typography.labelSmall,
                color = if (state.syncState == NoteSyncState.Conflict) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
            )
        }

        if (!state.onlyDay) {
            if (state.isEditing && onSetDayKey != null) {
                NotePlacementPicker(
                    dayKey = state.dayKey,
                    onChange = onSetDayKey,
                )
            } else if (state.dayKey != null) {
                Text(
                    text = state.dayKey,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun EditorInlineError(
    message: String,
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    ) {
        Text(
            text = message,
            modifier = Modifier.padding( horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun ConflictBanner(
    canResolve: Boolean,
    onClick: () -> Unit,
) {
    if (canResolve) {
        Surface(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ConflictBannerContent(
                message = "云端内容已经发生变化，点击查看并选择要保留的版本。",
            )
        }
    } else {

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ) {
            ConflictBannerContent(
                message = "检测到同步冲突，正在获取最新云端版本……",
            )
        }
    }
}

@Composable
private fun ConflictBannerContent(
    message: String,
) {
    Column(
        modifier = Modifier.padding( horizontal = 16.dp, vertical = 12.dp ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "发现同步冲突",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun NoteConflictDialog(
    localTitle: String,
    localText: String,
    remote: NoteConflictUiModel,
    onDismiss: () -> Unit,
    onUseRemote: () -> Unit,
    onKeepLocal: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "同步冲突") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "这条记录在其他设备或云端发生了变化。请选择要保留的版本。",
                )
                ConflictVersionPreview(
                    label = "本机版本",
                    title = localTitle,
                    text = localText,
                )
                ConflictVersionPreview(
                    label = remoteStatusLabel(remote.remoteStatus),
                    title = remote.title,
                    text = remote.text,
                )
            }
        },

        confirmButton = {
            TextButton(onClick = onKeepLocal ) {
                Text(
                    text = "保留本机版本",
                )
            }
        },

        dismissButton = {
            TextButton(onClick = onUseRemote) {
                Text(
                    text = if (remote.remoteStatus == NoteRemoteStatus.Deleted) {
                        "接受云端删除"
                    } else {
                        "使用云端版本"
                    },
                )
            }
        },
    )
}

@Composable
private fun ConflictVersionPreview(
    label: String,
    title: String,
    text: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = title.takeIf { it.isNotBlank() } ?: "无标题",
            style = MaterialTheme.typography.titleSmall,
            maxLines = 2,
        )

        Text(
            text = text.takeIf { it.isNotBlank() } ?: "暂无正文",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 5,
        )
    }
}

private fun remoteStatusLabel(
    status: NoteRemoteStatus,
): String {
    return when (
        status
    ) {
        NoteRemoteStatus.Active -> "云端版本"
        NoteRemoteStatus.Archived -> "云端版本 · 已归档"
        NoteRemoteStatus.Deleted -> "云端状态 · 已删除"
    }
}

/** Legacy Note without contentJson can still be opened using its plain-text projection. */
private fun localEditorContent(
    contentJson: String?,
    fallbackText: String): BlockContent {
    val parsed = contentJson?.takeIf { it.isNotBlank() }?.let { raw ->
        runCatching {
            val element = requireNotNull(NoteContentCodec.parse(raw))
            NoteContentCodec.readModel(element)
        }.getOrNull()
    }
    return parsed
        ?: NoteContentCodec.readModel(
            NoteContentCodec.fromPlainText(fallbackText)
        )
}