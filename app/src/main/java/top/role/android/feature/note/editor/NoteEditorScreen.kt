package top.role.android.feature.note.editor

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState

@Composable
fun NoteEditorRoute(
    ownerUserId: String,
    noteLocalRepository: NoteLocalRepository,

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
                    ownerUserId =
                        ownerUserId,

                    noteLocalRepository =
                        noteLocalRepository,

                    initialLocalKey =
                        initialLocalKey,

                    initialDayKey =
                        initialDayKey,

                    openAsDayOnly =
                        openAsDayOnly,

                    createAsNormal =
                        createAsNormal,

                    createAsDated =
                        createAsDated,
                )
        )

    val state by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    val lifecycleOwner =
        LocalLifecycleOwner.current

    /*
     * App 进入后台：
     *
     * Editing → 尽快落盘。
     * Reading / Conflict → ViewModel guard 不产生写入。
     */
    DisposableEffect(
        lifecycleOwner,
        viewModel,
    ) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (
                    event ==
                    Lifecycle.Event.ON_STOP
                ) {
                    viewModel.saveNow()
                }
            }

        lifecycleOwner.lifecycle
            .addObserver(
                observer,
            )

        onDispose {
            lifecycleOwner.lifecycle
                .removeObserver(
                    observer,
                )
        }
    }

    val scope =
        rememberCoroutineScope()

    NoteEditorScreen(
        state =
            state,

        onTitleChange =
            viewModel::updateTitle,

        onTextChange =
            viewModel::updateText,

        onPinnedChange =
            viewModel::setPinned,

        onStartEditing =
            viewModel::startEditing,

        onFinishEditing =
            viewModel::finishEditing,

        onUseRemoteConflict = {
            viewModel
                .resolveConflictUsingRemote(
                    onDeleted =
                        onBack,
                )
        },

        onKeepLocalConflict =
            viewModel::resolveConflictKeepingLocal,

        onBack = {
            scope.launch {
                viewModel
                    .flushAndCleanup()

                onBack()
            }
        },
    )
}

@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun NoteEditorScreen(
    state: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onPinnedChange: (Boolean) -> Unit,
    onStartEditing: () -> Unit,
    onFinishEditing: () -> Unit,
    onUseRemoteConflict: () -> Unit,
    onKeepLocalConflict: () -> Unit,
    onBack: () -> Unit,
) {
    var showConflictDialog by
    remember {
        mutableStateOf(
            false,
        )
    }

    val hasConflict =
        state.syncState ==
                NoteSyncState.Conflict

    val canResolveConflict =
        hasConflict &&
                state.conflict != null

    /*
     * Conflict 被外部解决或者状态发生变化后，
     * 已打开的 Dialog 不应继续悬挂。
     */
    LaunchedEffect(
        hasConflict,
        canResolveConflict,
    ) {
        if (
            !hasConflict ||
            !canResolveConflict
        ) {
            showConflictDialog =
                false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text =
                            when {
                                state.onlyDay ->
                                    state.dayKey
                                        ?: "日记"

                                state.dayKey != null ->
                                    state.dayKey

                                else ->
                                    "记录"
                            },
                    )
                },

                navigationIcon = {
                    IconButton(
                        onClick =
                            onBack,
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored
                                    .Filled.ArrowBack,

                            contentDescription =
                                "返回",
                        )
                    }
                },

                actions = {
                    /*
                     * Conflict 期间冻结一切本地 mutation。
                     */
                    if (
                        state.canEdit &&
                        !hasConflict
                    ) {
                        IconButton(
                            onClick = {
                                onPinnedChange(
                                    !state.pinned,
                                )
                            },
                        ) {
                            Icon(
                                imageVector =
                                    if (
                                        state.pinned
                                    ) {
                                        Icons.Filled.PushPin
                                    } else {
                                        Icons.Outlined.PushPin
                                    },

                                contentDescription =
                                    if (
                                        state.pinned
                                    ) {
                                        "取消置顶"
                                    } else {
                                        "置顶"
                                    },
                            )
                        }
                    }

                    /*
                     * Reading + Active + no Conflict：
                     * 显示编辑入口。
                     */
                    if (
                        state.canEdit &&
                        !state.isEditing &&
                        !hasConflict
                    ) {
                        IconButton(
                            onClick =
                                onStartEditing,
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.Edit,

                                contentDescription =
                                    "编辑",
                            )
                        }
                    }

                    /*
                     * Editing：
                     * 显示完成。
                     *
                     * Conflict 一旦出现，
                     * 这个入口也立即冻结。
                     */
                    if (
                        state.isEditing &&
                        !hasConflict
                    ) {
                        TextButton(
                            onClick =
                                onFinishEditing,
                        ) {
                            Text(
                                text =
                                    "完成",
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
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding,
                            ),

                    contentAlignment =
                        Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            /*
             * 真正 fatal load error。
             *
             * Archived / Conflict 都可能 canEdit=false，
             * 所以不能再用 canEdit 判断错误页。
             */
            state.errorMessage != null &&
                    state.localKey == null -> {

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding,
                            )
                            .padding(
                                24.dp,
                            ),

                    contentAlignment =
                        Alignment.Center,
                ) {
                    Text(
                        text =
                            state.errorMessage,

                        color =
                            MaterialTheme
                                .colorScheme
                                .error,
                    )
                }
            }

            /*
             * Conflict 优先级高于 Editing。
             *
             * 即使由于某种竞态 mode 尚未切回 Reading，
             * UI 也不会继续暴露输入框。
             */
            hasConflict -> {
                NoteReadingContent(
                    state =
                        state,

                    onOpenConflict = {
                        if (
                            canResolveConflict
                        ) {
                            showConflictDialog =
                                true
                        }
                    },

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding,
                            ),
                )
            }

            state.isEditing -> {
                NoteEditingContent(
                    state =
                        state,

                    onTitleChange =
                        onTitleChange,

                    onTextChange =
                        onTextChange,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding,
                            ),
                )
            }

            else -> {
                NoteReadingContent(
                    state =
                        state,

                    onOpenConflict = {
                        if (
                            canResolveConflict
                        ) {
                            showConflictDialog =
                                true
                        }
                    },

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(
                                innerPadding,
                            ),
                )
            }
        }
    }

    val conflict =
        state.conflict

    if (
        showConflictDialog &&
        conflict != null
    ) {
        NoteConflictDialog(
            localTitle =
                state.title,

            localText =
                state.text,

            remote =
                conflict,

            onDismiss = {
                showConflictDialog =
                    false
            },

            onUseRemote = {
                showConflictDialog =
                    false

                onUseRemoteConflict()
            },

            onKeepLocal = {
                showConflictDialog =
                    false

                onKeepLocalConflict()
            },
        )
    }
}

/**
 * Reading
 *
 * Active existing Note 默认进入。
 * Archived 永远进入。
 * Conflict 也统一进入这里。
 */
@Composable
private fun NoteReadingContent(
    state: NoteEditorUiState,
    onOpenConflict: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasConflict =
        state.syncState ==
                NoteSyncState.Conflict

    Column(
        modifier =
            modifier
                .padding(
                    horizontal =
                        20.dp,

                    vertical =
                        12.dp,
                ),

        verticalArrangement =
            Arrangement.Top,
    ) {
        EditorMetaRow(
            state =
                state,
        )

        /*
         * 非 fatal 错误也需要有可见出口。
         */
        if (
            !state.errorMessage.isNullOrBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        10.dp,
                    ),
            )

            EditorInlineError(
                message =
                    state.errorMessage,
            )
        }

        if (
            hasConflict
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        12.dp,
                    ),
            )

            ConflictBanner(
                canResolve =
                    state.conflict != null,

                onClick =
                    onOpenConflict,
            )
        }

        /*
         * Conflict remote Archived/Deleted
         * 保存在 conflict snapshot，
         * 并不会提前改变 current remoteStatus。
         *
         * 所以这里显示的是本地当前真实 lifecycle。
         */
        if (
            state.remoteStatus ==
            NoteRemoteStatus.Archived
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        12.dp,
                    ),
            )

            Text(
                text =
                    "已归档 · 取消归档后可继续编辑",

                style =
                    MaterialTheme
                        .typography
                        .labelMedium,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    20.dp,
                ),
        )

        Text(
            text =
                state.title
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?: "无标题",

            style =
                MaterialTheme
                    .typography
                    .headlineSmall,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurface,
        )

        Spacer(
            modifier =
                Modifier.height(
                    18.dp,
                ),
        )

        if (
            state.text.isNotBlank()
        ) {
            Text(
                text =
                    state.text,

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurface,
            )
        } else {
            Text(
                text =
                    "暂无正文",

                style =
                    MaterialTheme
                        .typography
                        .bodyLarge,

                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant,
            )
        }
    }
}

/**
 * Editing
 *
 * TextFieldState 继续负责：
 *
 * - selection
 * - IME composition
 * - cursor
 *
 * ViewModel 只接 String。
 */
@Composable
private fun NoteEditingContent(
    state: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val titleState =
        rememberTextFieldState(
            initialText =
                state.title,
        )

    val bodyState =
        rememberTextFieldState(
            initialText =
                state.text,
        )

    /*
     * TextFieldState → ViewModel。
     */
    LaunchedEffect(
        titleState,
    ) {
        snapshotFlow {
            titleState
                .text
                .toString()
        }.collectLatest { value ->
            if (
                value !=
                state.title
            ) {
                onTitleChange(
                    value,
                )
            }
        }
    }

    LaunchedEffect(
        bodyState,
    ) {
        snapshotFlow {
            bodyState
                .text
                .toString()
        }.collectLatest { value ->
            if (
                value !=
                state.text
            ) {
                onTextChange(
                    value,
                )
            }
        }
    }

    Column(
        modifier =
            modifier
                .padding(
                    horizontal =
                        20.dp,

                    vertical =
                        12.dp,
                ),

        verticalArrangement =
            Arrangement.Top,
    ) {
        EditorMetaRow(
            state =
                state,
        )

        if (
            !state.errorMessage.isNullOrBlank()
        ) {
            Spacer(
                modifier =
                    Modifier.height(
                        10.dp,
                    ),
            )

            EditorInlineError(
                message =
                    state.errorMessage,
            )
        }

        Spacer(
            modifier =
                Modifier.height(
                    12.dp,
                ),
        )

        BasicTextField(
            state =
                titleState,

            modifier =
                Modifier.fillMaxWidth(),

            textStyle =
                MaterialTheme
                    .typography
                    .titleLarge
                    .copy(
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface,
                    ),

            cursorBrush =
                SolidColor(
                    MaterialTheme
                        .colorScheme
                        .primary,
                ),

            lineLimits =
                TextFieldLineLimits.SingleLine,

            decorator = { innerTextField ->
                Box {
                    if (
                        titleState.text
                            .isEmpty()
                    ) {
                        Text(
                            text =
                                "标题",

                            style =
                                MaterialTheme
                                    .typography
                                    .titleLarge,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                        )
                    }

                    innerTextField()
                }
            },
        )

        Spacer(
            modifier =
                Modifier.height(
                    16.dp,
                ),
        )

        BasicTextField(
            state =
                bodyState,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(
                        1f,
                    ),

            textStyle =
                MaterialTheme
                    .typography
                    .bodyLarge
                    .copy(
                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurface,
                    ),

            cursorBrush =
                SolidColor(
                    MaterialTheme
                        .colorScheme
                        .primary,
                ),

            lineLimits =
                TextFieldLineLimits.MultiLine(),

            decorator = { innerTextField ->
                Box(
                    modifier =
                        Modifier.fillMaxSize(),
                ) {
                    if (
                        bodyState.text
                            .isEmpty()
                    ) {
                        Text(
                            text =
                                "开始记录……",

                            style =
                                MaterialTheme
                                    .typography
                                    .bodyLarge,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                        )
                    }

                    innerTextField()
                }
            },
        )

        /*
         * 后续 Editing 工具统一放这里：
         *
         * - Image
         * - AI Polish
         * - AI Rewrite
         *
         * 本轮只保留结构。
         */
    }
}

@Composable
private fun EditorMetaRow(
    state: NoteEditorUiState,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        Text(
            text =
                when {
                    state.onlyDay ->
                        "私人日记"

                    state.dayKey != null ->
                        "关联日期"

                    else ->
                        "普通记录"
                },

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )

        Text(
            text =
                when (
                    state.syncState
                ) {
                    NoteSyncState.Synced ->
                        "已同步"

                    NoteSyncState.Pending ->
                        "已保存在本机"

                    NoteSyncState.Syncing ->
                        "同步中"

                    NoteSyncState.Waiting ->
                        "等待同步"

                    NoteSyncState.Conflict ->
                        "同步冲突"
                },

            style =
                MaterialTheme
                    .typography
                    .labelSmall,

            color =
                if (
                    state.syncState ==
                    NoteSyncState.Conflict
                ) {
                    MaterialTheme
                        .colorScheme
                        .error
                } else {
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
                },
        )
    }
}

@Composable
private fun EditorInlineError(
    message: String,
) {
    Surface(
        shape =
            MaterialTheme
                .shapes
                .small,

        color =
            MaterialTheme
                .colorScheme
                .errorContainer,

        contentColor =
            MaterialTheme
                .colorScheme
                .onErrorContainer,
    ) {
        Text(
            text =
                message,

            modifier =
                Modifier.padding(
                    horizontal =
                        12.dp,

                    vertical =
                        8.dp,
                ),

            style =
                MaterialTheme
                    .typography
                    .bodySmall,
        )
    }
}

@Composable
private fun ConflictBanner(
    canResolve: Boolean,
    onClick: () -> Unit,
) {
    if (
        canResolve
    ) {
        Surface(
            onClick =
                onClick,

            shape =
                MaterialTheme
                    .shapes
                    .medium,

            color =
                MaterialTheme
                    .colorScheme
                    .errorContainer,

            contentColor =
                MaterialTheme
                    .colorScheme
                    .onErrorContainer,
        ) {
            ConflictBannerContent(
                message =
                    "云端内容已经发生变化，点击查看并选择要保留的版本。",
            )
        }
    } else {
        /*
         * VERSION_CONFLICT 已经发生，
         * 但完整 Remote snapshot 尚未 Pull 回来。
         *
         * 此时必须冻结编辑，
         * 但不能提供不可执行的 resolver。
         */
        Surface(
            shape =
                MaterialTheme
                    .shapes
                    .medium,

            color =
                MaterialTheme
                    .colorScheme
                    .errorContainer,

            contentColor =
                MaterialTheme
                    .colorScheme
                    .onErrorContainer,
        ) {
            ConflictBannerContent(
                message =
                    "检测到同步冲突，正在获取最新云端版本……",
            )
        }
    }
}

@Composable
private fun ConflictBannerContent(
    message: String,
) {
    Column(
        modifier =
            Modifier.padding(
                horizontal =
                    16.dp,

                vertical =
                    12.dp,
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                4.dp,
            ),
    ) {
        Text(
            text =
                "发现同步冲突",

            style =
                MaterialTheme
                    .typography
                    .titleSmall,

            fontWeight =
                FontWeight.SemiBold,
        )

        Text(
            text =
                message,

            style =
                MaterialTheme
                    .typography
                    .bodySmall,
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
        onDismissRequest =
            onDismiss,

        title = {
            Text(
                text =
                    "同步冲突",
            )
        },

        text = {
            Column(
                verticalArrangement =
                    Arrangement.spacedBy(
                        16.dp,
                    ),
            ) {
                Text(
                    text =
                        "这条记录在其他设备或云端发生了变化。请选择要保留的版本。",
                )

                ConflictVersionPreview(
                    label =
                        "本机版本",

                    title =
                        localTitle,

                    text =
                        localText,
                )

                ConflictVersionPreview(
                    label =
                        remoteStatusLabel(
                            remote.remoteStatus,
                        ),

                    title =
                        remote.title,

                    text =
                        remote.text,
                )
            }
        },

        /*
         * 默认主操作保护本机用户输入。
         */
        confirmButton = {
            TextButton(
                onClick =
                    onKeepLocal,
            ) {
                Text(
                    text =
                        "保留本机版本",
                )
            }
        },

        dismissButton = {
            TextButton(
                onClick =
                    onUseRemote,
            ) {
                Text(
                    text =
                        if (
                            remote.remoteStatus ==
                            NoteRemoteStatus.Deleted
                        ) {
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
        verticalArrangement =
            Arrangement.spacedBy(
                4.dp,
            ),
    ) {
        Text(
            text =
                label,

            style =
                MaterialTheme
                    .typography
                    .labelMedium,

            fontWeight =
                FontWeight.SemiBold,

            color =
                MaterialTheme
                    .colorScheme
                    .primary,
        )

        Text(
            text =
                title
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?: "无标题",

            style =
                MaterialTheme
                    .typography
                    .titleSmall,

            maxLines =
                2,
        )

        Text(
            text =
                text
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?: "暂无正文",

            style =
                MaterialTheme
                    .typography
                    .bodySmall,

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,

            maxLines =
                5,
        )
    }
}

private fun remoteStatusLabel(
    status: NoteRemoteStatus,
): String {
    return when (
        status
    ) {
        NoteRemoteStatus.Active ->
            "云端版本"

        NoteRemoteStatus.Archived ->
            "云端版本 · 已归档"

        NoteRemoteStatus.Deleted ->
            "云端状态 · 已删除"
    }
}