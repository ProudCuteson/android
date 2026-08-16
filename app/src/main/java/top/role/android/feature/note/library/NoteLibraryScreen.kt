package top.role.android.feature.note.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.database.note.effectiveRemoteStatus
import top.role.android.core.model.note.NoteRemoteStatus

@Composable
fun NoteLibraryScreen(
    ownerUserId: String,
    noteLocalRepository: NoteLocalRepository,
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    onCreateNote: () -> Unit,
) {
    val viewModel: NoteLibraryViewModel =
        viewModel(
            factory =
                NoteLibraryViewModelFactory(
                    ownerUserId =
                        ownerUserId,

                    noteLocalRepository =
                        noteLocalRepository,
                )
        )

    val state by
    viewModel.uiState
        .collectAsStateWithLifecycle()

    var pendingDeleteNote by
    remember {
        mutableStateOf<
                LocalNoteEntity?
                >(
            null,
        )
    }

    Column(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        LibraryHeader(
            onBack =
                onBack,

            onCreateNote =
                onCreateNote,
        )

        OutlinedTextField(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            16.dp,
                    ),

            value =
                state.searchQuery,

            onValueChange =
                viewModel::updateSearchQuery,

            singleLine =
                true,

            placeholder = {
                Text(
                    text =
                        "搜索记录",
                )
            },
        )

        Spacer(
            modifier =
                Modifier.height(
                    12.dp,
                )
        )

        LibrarySections(
            selectedSection =
                state.selectedSection,

            onSelect =
                viewModel::selectSection,
        )

        Spacer(
            modifier =
                Modifier.height(
                    8.dp,
                )
        )

        HorizontalDivider()

        if (
            state.notes.isEmpty()
        ) {
            EmptyLibraryContent(
                searching =
                    state.searchQuery
                        .isNotBlank(),

                section =
                    state.selectedSection,
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    androidx.compose.foundation.layout.PaddingValues(
                        vertical =
                            8.dp,
                    ),
            ) {
                items(
                    items =
                        state.notes,

                    key = { note ->
                        note.localKey
                    },
                ) { note ->
                    NoteLibraryRow(
                        note =
                            note,

                        onClick = {
                            onOpenNote(
                                note.localKey,
                            )
                        },

                        onTogglePinned = {
                            viewModel.togglePinned(
                                note,
                            )
                        },

                        onArchive = {
                            viewModel.archiveNote(
                                note,
                            )
                        },

                        onRestore = {
                            viewModel.restoreNote(
                                note,
                            )
                        },

                        onRequestDelete = {
                            pendingDeleteNote =
                                note
                        },
                    )
                }
            }
        }
    }

    pendingDeleteNote
        ?.let { note ->
            AlertDialog(
                onDismissRequest = {
                    pendingDeleteNote =
                        null
                },

                title = {
                    Text(
                        text =
                            "删除记录？",
                    )
                },

                text = {
                    Text(
                        text =
                            "删除后，这条记录将从记录列表中移除。",
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            pendingDeleteNote =
                                null

                            viewModel.deleteNote(
                                note,
                            )
                        },
                    ) {
                        Text(
                            text =
                                "删除",
                        )
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = {
                            pendingDeleteNote =
                                null
                        },
                    ) {
                        Text(
                            text =
                                "取消",
                        )
                    }
                },
            )
        }
}

@Composable
private fun LibraryHeader(
    onBack: () -> Unit,
    onCreateNote: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        8.dp,

                    vertical =
                        8.dp,
                ),

        verticalAlignment =
            Alignment.CenterVertically,
    ) {
        IconButton(
            onClick =
                onBack,
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.ArrowBack,

                contentDescription =
                    "返回",
            )
        }

        Text(
            modifier =
                Modifier.weight(
                    1f,
                ),

            text =
                "记录",

            style =
                MaterialTheme
                    .typography
                    .headlineSmall,

            fontWeight =
                FontWeight.SemiBold,
        )

        IconButton(
            onClick =
                onCreateNote,
        ) {
            Icon(
                imageVector =
                    Icons.Default.Add,

                contentDescription =
                    "新建记录",
            )
        }
    }
}

@Composable
private fun LibrarySections(
    selectedSection: NoteLibrarySection,
    onSelect: (NoteLibrarySection) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        16.dp,
                ),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp,
            ),
    ) {
        LibrarySectionChip(
            text =
                "最近",

            selected =
                selectedSection ==
                        NoteLibrarySection.Recent,

            onClick = {
                onSelect(
                    NoteLibrarySection.Recent,
                )
            },
        )

        LibrarySectionChip(
            text =
                "置顶",

            selected =
                selectedSection ==
                        NoteLibrarySection.Pinned,

            onClick = {
                onSelect(
                    NoteLibrarySection.Pinned,
                )
            },
        )

        LibrarySectionChip(
            text =
                "日记",

            selected =
                selectedSection ==
                        NoteLibrarySection.Diary,

            onClick = {
                onSelect(
                    NoteLibrarySection.Diary,
                )
            },
        )

        LibrarySectionChip(
            text =
                "归档",

            selected =
                selectedSection ==
                        NoteLibrarySection.Archived,

            onClick = {
                onSelect(
                    NoteLibrarySection.Archived,
                )
            },
        )
    }
}

@Composable
private fun LibrarySectionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected =
            selected,

        onClick =
            onClick,

        label = {
            Text(
                text =
                    text,
            )
        },
    )
}

@Composable
private fun NoteLibraryRow(
    note: LocalNoteEntity,
    onClick: () -> Unit,
    onTogglePinned: () -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    var moreExpanded by
    remember {
        mutableStateOf(
            false,
        )
    }

    Surface(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClick =
                        onClick,
                ),
    ) {
        Column(
            modifier =
                Modifier.padding(
                    start =
                        16.dp,

                    end =
                        8.dp,

                    top =
                        14.dp,

                    bottom =
                        14.dp,
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    5.dp,
                ),
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),

                verticalAlignment =
                    Alignment.CenterVertically,
            ) {
                Text(
                    modifier =
                        Modifier.weight(
                            1f,
                        ),

                    text =
                        note.title
                            .takeIf {
                                it.isNotBlank()
                            }
                            ?: note.text
                                .takeIf {
                                    it.isNotBlank()
                                }
                                ?.take(
                                    40,
                                )
                            ?: "无标题",

                    style =
                        MaterialTheme
                            .typography
                            .titleMedium,

                    fontWeight =
                        FontWeight.Medium,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,
                )

                if (
                    note.pinned
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.PushPin,

                        contentDescription =
                            "已置顶",
                    )
                }

                Box {
                    IconButton(
                        onClick = {
                            moreExpanded =
                                true
                        },
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.MoreVert,

                            contentDescription =
                                "更多操作",
                        )
                    }

                    DropdownMenu(
                        expanded =
                            moreExpanded,

                        onDismissRequest = {
                            moreExpanded =
                                false
                        },
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text =
                                        if (
                                            note.pinned
                                        ) {
                                            "取消置顶"
                                        } else {
                                            "置顶"
                                        },
                                )
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.PushPin,

                                    contentDescription =
                                        null,
                                )
                            },

                            onClick = {
                                moreExpanded =
                                    false

                                onTogglePinned()
                            },
                        )

                        if (
                            note.effectiveRemoteStatus ==
                            NoteRemoteStatus.Archived
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text =
                                            "取消归档",
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.Unarchive,

                                        contentDescription =
                                            null,
                                    )
                                },

                                onClick = {
                                    moreExpanded =
                                        false

                                    onRestore()
                                },
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text =
                                            "归档",
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        imageVector =
                                            Icons.Outlined.Archive,

                                        contentDescription =
                                            null,
                                    )
                                },

                                onClick = {
                                    moreExpanded =
                                        false

                                    onArchive()
                                },
                            )
                        }

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text =
                                        "删除",
                                )
                            },

                            leadingIcon = {
                                Icon(
                                    imageVector =
                                        Icons.Outlined.Delete,

                                    contentDescription =
                                        null,
                                )
                            },

                            onClick = {
                                moreExpanded =
                                    false

                                onRequestDelete()
                            },
                        )
                    }
                }
            }

            if (
                note.title.isNotBlank() &&
                note.text.isNotBlank()
            ) {
                Text(
                    text =
                        note.text,

                    style =
                        MaterialTheme
                            .typography
                            .bodyMedium,

                    color =
                        MaterialTheme
                            .colorScheme
                            .onSurfaceVariant,

                    maxLines =
                        2,

                    overflow =
                        TextOverflow.Ellipsis,
                )
            }

            Row(
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp,
                    ),
            ) {
                note.dayKey
                    ?.let { dayKey ->
                        Text(
                            text =
                                dayKey,

                            style =
                                MaterialTheme
                                    .typography
                                    .labelSmall,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant,
                        )
                    }

                if (
                    note.onlyDay
                ) {
                    Text(
                        text =
                            "日记",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }

                if (
                    note.effectiveRemoteStatus ==
                    NoteRemoteStatus.Archived
                ) {
                    Text(
                        text =
                            "已归档",

                        style =
                            MaterialTheme
                                .typography
                                .labelSmall,

                        color =
                            MaterialTheme
                                .colorScheme
                                .onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryContent(
    searching: Boolean,
    section: NoteLibrarySection,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    32.dp,
                ),

        verticalArrangement =
            Arrangement.Center,

        horizontalAlignment =
            Alignment.CenterHorizontally,
    ) {
        Text(
            text =
                when {
                    searching ->
                        "没有找到记录"

                    section ==
                            NoteLibrarySection.Archived ->
                        "这里还没有归档记录"

                    else ->
                        "这里还没有记录"
                },

            color =
                MaterialTheme
                    .colorScheme
                    .onSurfaceVariant,
        )
    }
}