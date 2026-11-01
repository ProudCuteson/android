package top.role.android.feature.note.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.database.note.LocalNoteEntity

class NoteLibraryViewModel(
    private val localOwnerUserId: String,
    private val noteLocalRepository:
    NoteLocalRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            NoteLibraryUiState(),
        )

    val uiState:
            StateFlow<NoteLibraryUiState> =
        _uiState.asStateFlow()

    private var observeJob:
            Job? = null

    init {
        observeCurrent()
    }

    // MARK: - Section

    fun selectSection(
        section: NoteLibrarySection,
    ) {
        if (
            _uiState.value.selectedSection ==
            section
        ) {
            return
        }

        _uiState.value =
            _uiState.value.copy(
                selectedSection =
                    section,
            )

        observeCurrent()
    }

    // MARK: - Search

    fun updateSearchQuery(
        value: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                searchQuery =
                    value,
            )

        observeCurrent()
    }

    // MARK: - Observe

    private fun observeCurrent() {
        observeJob?.cancel()

        observeJob =
            viewModelScope.launch {
                val state =
                    _uiState.value

                val query =
                    state.searchQuery
                        .trim()

                val flow =
                    if (
                        query.isNotEmpty()
                    ) {
                        /*
                         * 第一版搜索只查 Active Library。
                         *
                         * Archived 搜索以后如果需要，
                         * 可以再增加独立 scope。
                         */
                        noteLocalRepository
                            .searchNotes(
                                localOwnerUserId = localOwnerUserId,
                                query = query,
                            )
                    } else {
                        when (
                            state.selectedSection
                        ) {
                            NoteLibrarySection.Recent ->
                                noteLocalRepository
                                    .observeRecentNotes(
                                        localOwnerUserId = localOwnerUserId,
                                    )

                            NoteLibrarySection.Pinned ->
                                noteLocalRepository
                                    .observePinnedNotes(
                                        localOwnerUserId = localOwnerUserId,
                                    )

                            NoteLibrarySection.Diary ->
                                noteLocalRepository
                                    .observeDiaryNotes(
                                        localOwnerUserId = localOwnerUserId,
                                    )

                            NoteLibrarySection.Archived ->
                                noteLocalRepository
                                    .observeArchivedNotes(
                                        localOwnerUserId = localOwnerUserId,
                                    )
                        }
                    }

                flow.collect { notes ->
                    _uiState.value =
                        _uiState.value.copy(
                            notes = notes,
                            loading = false,
                            errorMessage = null,
                        )
                }
            }
    }

    // MARK: - Pin

    fun togglePinned(
        note: LocalNoteEntity,
    ) {
        viewModelScope.launch {
            try {
                noteLocalRepository
                    .setPinned(
                        note =
                            note,

                        pinned =
                            !note.pinned,
                    )

                /*
                 * setPinned() Repository 内已经 requestSync，
                 * 这里不需要重复唤醒。
                 */
            } catch (
                error: Throwable
            ) {
                setError(
                    error.message
                        ?: "更新置顶状态失败",
                )
            }
        }
    }

    // MARK: - Archive

    fun archiveNote(
        note: LocalNoteEntity,
    ) {
        viewModelScope.launch {
            try {
                val marked =
                    noteLocalRepository
                        .markForArchive(
                            note =
                                note,
                        )

                marked
                    .localOwnerUserId
                    .let { owner ->
                        noteLocalRepository
                            .requestSync(
                                localOwnerUserId = owner,
                            )
                    }
            } catch (
                error: Throwable
            ) {
                setError(
                    error.message
                        ?: "归档记录失败",
                )
            }
        }
    }

    // MARK: - Restore

    fun restoreNote(
        note: LocalNoteEntity,
    ) {
        viewModelScope.launch {
            try {
                val marked = noteLocalRepository
                    .markForRestore(
                        note = note,
                    )

                marked
                    .localOwnerUserId
                    .let { owner ->
                        noteLocalRepository
                            .requestSync(
                                localOwnerUserId = owner,
                            )
                    }
            } catch (
                error: Throwable
            ) {
                setError(
                    error.message
                        ?: "取消归档失败",
                )
            }
        }
    }

    // MARK: - Delete

    fun deleteNote(
        note: LocalNoteEntity,
    ) {
        viewModelScope.launch {
            try {
                val marked =
                    noteLocalRepository
                        .markForDelete(
                            note =
                                note,
                        )

                /*
                 * null：
                 * 从未上传的本地 Note
                 * 已经直接从 Room 删除。
                 *
                 * non-null：
                 * 等待 lifecycle soft delete。
                 */
                marked
                    ?.localOwnerUserId
                    ?.let { owner ->
                        noteLocalRepository
                            .requestSync(
                                localOwnerUserId = owner,
                            )
                    }
            } catch (
                error: Throwable
            ) {
                setError(
                    error.message
                        ?: "删除记录失败",
                )
            }
        }
    }

    private fun setError(
        message: String,
    ) {
        _uiState.value =
            _uiState.value.copy(
                errorMessage =
                    message,
            )
    }
}