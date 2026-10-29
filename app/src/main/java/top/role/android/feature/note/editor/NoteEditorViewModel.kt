package top.role.android.feature.note.editor

import java.io.File
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import top.role.android.core.data.note.NoteLocalRepository
import kotlinx.serialization.json.Json
import top.role.android.core.model.note.content.BlockContent
import top.role.android.core.model.note.content.NoteContentCodec
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.image.ImageRepository
import top.role.android.core.database.entity.LocalNoteImageEntity
import top.role.android.core.data.note.ai.NoteAiMode
import top.role.android.core.data.note.ai.NoteAiRemoteDataSource

class NoteEditorViewModel(
    private val ownerUserId: String,
    private val noteLocalRepository: NoteLocalRepository,
    private val noteAiRemoteDataSource: NoteAiRemoteDataSource,
    private val noteImageService: NoteImageService,
    private val imageRepository: ImageRepository,

    private val initialLocalKey: String? = null,
    private val initialDayKey: String? = null,
    private val openAsDayOnly: Boolean = false,
    private val createAsNormal: Boolean = false,
    private val createAsDated: Boolean = false,
) : ViewModel() {

    private val richContentJson = Json {
        encodeDefaults = true
        explicitNulls = false
    }

    private val _uiState =
        MutableStateFlow(
            NoteEditorUiState(),
        )

    val uiState:
            StateFlow<NoteEditorUiState> =
        _uiState.asStateFlow()

    private var currentNote:
            LocalNoteEntity? = null

    private var lastAiRunAt:
            Long =
        0L

    private var currentImageEntities:
            List<LocalNoteImageEntity> =
        emptyList()

    private val remotePreviewUrls =
        mutableMapOf<String, String>()

    private val resolvingPreviewImageIds =
        mutableSetOf<String>()

    private var saveJob:
            Job? = null

    private var observeNoteJob:
            Job? = null

    private var observedLocalKey:
            String? = null

    private var observeImagesJob:
            Job? = null

    private var observedImageNoteLocalKey:
            String? = null

    init {
        load()
    }

    // MARK: - Load

    private fun load() {
        viewModelScope.launch {
            try {
                val result =
                    loadInitialNote()

                val note =
                    result.note

                if (note == null) {
                    _uiState.value =
                        _uiState.value.copy(
                            loading =
                                false,

                            errorMessage =
                                "记录不存在",
                        )

                    return@launch
                }

                if (
                    note.ownerUserId !=
                    ownerUserId
                ) {
                    _uiState.value =
                        _uiState.value.copy(
                            loading =
                                false,

                            errorMessage =
                                "当前记录不属于登录用户",
                        )

                    return@launch
                }

                currentNote =
                    note

                val baseState =
                    NoteEditorUiState
                        .fromNote(
                            note,
                        )

                _uiState.value =
                    baseState.copy(
                        mode =
                            if (
                                result.startEditing &&
                                baseState.canEdit
                            ) {
                                NoteEditorMode.Editing
                            } else {
                                NoteEditorMode.Reading
                            },

                        conflict =
                            conflictUiModel(
                                note,
                            ),
                    )

                /*
                 * load 完成后立即进入 live Room observation。
                 */
                observeCurrentNote(
                    localKey =
                        note.localKey,
                )

                observeCurrentNoteImages(
                    noteLocalKey =
                        note.localKey,
                )

            } catch (
                error: Throwable
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        loading =
                            false,

                        errorMessage =
                            error.message
                                ?: "读取记录失败",
                    )
            }
        }
    }

    private suspend fun loadInitialNote():
            InitialNoteResult {

        /*
         * 明确 localKey：
         * 一定视为打开已有 Note。
         */
        if (
            initialLocalKey !=
            null
        ) {
            return InitialNoteResult(
                note =
                    noteLocalRepository
                        .findByLocalKey(
                            ownerUserId =
                                ownerUserId,

                            localKey =
                                initialLocalKey,
                        ),

                startEditing =
                    false,
            )
        }

        /*
         * DayOnly：
         *
         * 已存在
         * → Reading
         *
         * 第一次创建
         * → Editing
         */
        if (
            openAsDayOnly &&
            initialDayKey !=
            null
        ) {
            val existing =
                noteLocalRepository
                    .findDayOnlyNote(
                        ownerUserId =
                            ownerUserId,

                        dayKey =
                            initialDayKey,
                    )

            if (
                existing != null
            ) {
                return InitialNoteResult(
                    note =
                        existing,

                    startEditing =
                        false,
                )
            }

            return InitialNoteResult(
                note =
                    noteLocalRepository
                        .getOrCreateDayOnlyNote(
                            ownerUserId =
                                ownerUserId,

                            dayKey =
                                initialDayKey,
                        ),

                startEditing =
                    true,
            )
        }

        /*
         * 创建关联日期 Note。
         */
        if (
            createAsDated &&
            initialDayKey !=
            null
        ) {
            return InitialNoteResult(
                note =
                    noteLocalRepository
                        .createDatedNote(
                            ownerUserId =
                                ownerUserId,

                            dayKey =
                                initialDayKey,
                        ),

                startEditing =
                    true,
            )
        }

        /*
         * 创建普通 Note。
         */
        if (
            createAsNormal
        ) {
            return InitialNoteResult(
                note =
                    noteLocalRepository
                        .createNormalNote(
                            ownerUserId =
                                ownerUserId,
                        ),

                startEditing =
                    true,
            )
        }

        return InitialNoteResult(
            note =
                null,

            startEditing =
                false,
        )
    }

    // MARK: - Room observation

    private fun observeCurrentNote(
        localKey: String,
    ) {
        if (
            observedLocalKey ==
            localKey &&
            observeNoteJob?.isActive ==
            true
        ) {
            return
        }

        observeNoteJob
            ?.cancel()

        observedLocalKey =
            localKey

        observeNoteJob =
            viewModelScope.launch {
                noteLocalRepository
                    .observeByLocalKey(
                        ownerUserId =
                            ownerUserId,

                        localKey =
                            localKey,
                    )
                    .collectLatest { note ->

                        if (
                            note != null
                        ) {
                            applyObservedNote(
                                note =
                                    note,
                            )

                            return@collectLatest
                        }

                        /*
                         * localKey 有可能因为 identity 收敛发生改变：
                         *
                         * local temporary key
                         * → remote noteId key
                         *
                         * 或 Conflict:
                         *
                         * Remote Deleted
                         * → Keep Local
                         * → 新 localKey
                         *
                         * 此时旧 localKey 的 SQL observer 会得到 null。
                         *
                         * 使用稳定的 noteId / clientKey 尝试重新找到
                         * 当前 row，然后重新绑定 observer。
                         */
                        recoverObservationAfterKeyChange(
                            previousLocalKey =
                                localKey,
                        )
                    }
            }
    }

    private suspend fun recoverObservationAfterKeyChange(
        previousLocalKey: String,
    ) {
        val previous =
            currentNote
                ?: return

        /*
         * currentNote 已经被其他路径更新了 localKey，
         * 直接重新绑定即可。
         * 也需要观察图片，因为现在是独立的路线
         */
        if (
            previous.localKey !=
            previousLocalKey
        ) {
            noteImageService
                .rebindNote(
                    ownerUserId =
                        ownerUserId,

                    oldNoteLocalKey =
                        previousLocalKey,

                    newNoteLocalKey =
                        previous.localKey,
                )

            observeCurrentNote(
                localKey =
                    previous.localKey,
            )

            observeCurrentNoteImages(
                noteLocalKey =
                    previous.localKey,
            )

            return
        }

        val recovered =
            if (
                !previous.noteId.isNullOrBlank()
            ) {
                noteLocalRepository
                    .findByNoteId(
                        ownerUserId =
                            ownerUserId,

                        noteId =
                            previous.noteId,
                    )
            } else {
                noteLocalRepository
                    .findByClientKey(
                        ownerUserId =
                            ownerUserId,

                        clientKey =
                            previous.clientKey,
                    )
            }

        if (
            recovered == null ||
            recovered.localKey ==
            previousLocalKey
        ) {
            return
        }

        noteImageService
            .rebindNote(
                ownerUserId =
                    ownerUserId,

                oldNoteLocalKey =
                    previousLocalKey,

                newNoteLocalKey =
                    recovered.localKey,
            )

        applyObservedNote(
            note =
                recovered,
        )

        observeCurrentNote(
            localKey =
                recovered.localKey,
        )

        observeCurrentNoteImages(
            noteLocalKey =
                recovered.localKey,
        )

    }

    private fun applyObservedNote(
        note: LocalNoteEntity,
    ) {
        if (
            note.ownerUserId !=
            ownerUserId
        ) {
            return
        }

        val previousState =
            _uiState.value

        val conflict =
            conflictUiModel(
                note,
            )

        currentNote =
            note

        val baseState =
            NoteEditorUiState
                .fromNote(
                    note,
                )

        if (
            previousState.isEditing &&
            note.syncState ==
            NoteSyncState.Conflict
        ) {
            saveJob
                ?.cancel()

            saveJob =
                null

            _uiState.value =
                baseState.copy(
                    title =
                        previousState.title,

                    text =
                        previousState.text,

                    contentJson =
                        previousState.contentJson,

                    editorResetVersion =
                        previousState.editorResetVersion,

                    images =
                        previousState.images,

                    mode =
                        NoteEditorMode.Reading,

                    conflict =
                        conflict,

                    errorMessage =
                        note.lastSyncError
                            ?: previousState.errorMessage,
                )

            return
        }

        if (
            previousState.isEditing
        ) {
            _uiState.value =
                baseState.copy(
                    title =
                        previousState.title,

                    text =
                        previousState.text,

                    contentJson =
                        previousState.contentJson,

                    editorResetVersion =
                        previousState.editorResetVersion,

                    images =
                        previousState.images,

                    mode =
                        NoteEditorMode.Editing,

                    conflict =
                        conflict,

                    errorMessage =
                        previousState.errorMessage,
                )

            return
        }

        /*
         * Reading：
         * 完整接受 Room 最新状态。
         */
        _uiState.value =
            baseState.copy(

                images =
                    previousState.images,

                mode =
                    NoteEditorMode.Reading,

                conflict =
                    conflict,

                errorMessage =
                    note.lastSyncError,
            )
    }

    // MARK: - Mode

    fun startEditing() {
        val state =
            _uiState.value

        /*
         * Conflict 状态下禁止进入 Editing。
         */
        if (
            !state.canEdit ||
            state.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        if (
            state.mode ==
            NoteEditorMode.Editing
        ) {
            return
        }

        _uiState.value =
            state.copy(
                mode =
                    NoteEditorMode.Editing,

                errorMessage =
                    null,
            )
    }

    fun finishEditing() {
        val state =
            _uiState.value

        if (
            !state.isEditing ||
            state.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        /*
         * title-only / content-only 都合法，
         * 但两者同时为空不能进入 Reading。
         *
         * 空新 Note / 空已有 Note 的 cleanup
         * 仍然统一由“返回页面”流程处理。
         */
        if (
            state.title.isBlank() &&
            state.text.isBlank() &&
            state.images.isEmpty()
        ) {
            _uiState.value =
                state.copy(
                    errorMessage =
                        "标题、正文和图片不能同时为空，如需放弃这条记录请直接返回。",
                )

            return
        }

        saveJob
            ?.cancel()

        saveJob =
            null

        viewModelScope.launch {
            val note =
                saveCurrentContent()
                    ?: return@launch

            _uiState.value =
                _uiState.value.copy(
                    mode =
                        NoteEditorMode.Reading,

                    errorMessage =
                        null,
                )

            note.ownerUserId
                ?.let { owner ->
                    noteLocalRepository
                        .requestSync(
                            ownerUserId =
                                owner,
                        )
                }
        }
    }

    fun runAi(
        mode: NoteAiMode,
    ) {
        val current =
            _uiState.value

        /*
         * AI 只允许 Editing。
         */
        if (
            !current.isEditing ||
            !current.canEdit
        ) {
            return
        }

        if (
            current.aiRunning
        ) {
            return
        }

        /*
         * 简单防止连续点击。
         */
        val now =
            System.currentTimeMillis()

        if (
            now - lastAiRunAt <
            1_000L
        ) {
            return
        }

        val title =
            current.title
                .trim()

        val text =
            current.text
                .trim()

        /*
         * 与 iOS 保持相同 input contract：
         *
         * title
         *
         * text
         */
        val input =
            when {
                title.isEmpty() ->
                    text

                text.isEmpty() ->
                    title

                else ->
                    "$title\n\n$text"
            }

        if (
            input.isBlank()
        ) {
            _uiState.value =
                current.copy(
                    aiErrorMessage =
                        "请先输入一些内容",

                    aiSuggestion =
                        null,

                    aiSuggestionMode =
                        null,
                )

            return
        }

        lastAiRunAt =
            now

        _uiState.value =
            current.copy(
                aiRunning =
                    true,

                aiErrorMessage =
                    null,

                aiSuggestion =
                    null,

                aiSuggestionMode =
                    null,
            )

        viewModelScope.launch {
            try {
                val response =
                    noteAiRemoteDataSource
                        .generate(
                            input =
                                input,

                            mode =
                                mode,
                        )

                val output =
                    response.output
                        ?.trim()
                        .orEmpty()

                if (
                    output.isBlank()
                ) {
                    _uiState.value =
                        _uiState.value.copy(
                            aiRunning =
                                false,

                            aiErrorMessage =
                                "AI 没有返回内容",
                        )

                    return@launch
                }

                _uiState.value =
                    _uiState.value.copy(
                        aiRunning =
                            false,

                        aiSuggestion =
                            output,

                        aiSuggestionMode =
                            mode,

                        aiErrorMessage =
                            null,
                    )

            } catch (
                error: Throwable,
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        aiRunning =
                            false,

                        aiSuggestion =
                            null,

                        aiSuggestionMode =
                            null,

                        aiErrorMessage =
                            error.message
                                ?.takeIf {
                                    it.isNotBlank()
                                }
                                ?: "AI 处理失败",
                    )
            }
        }
    }

    fun replaceTextWithAiSuggestion() {
        val current =
            _uiState.value

        val suggestion =
            current.aiSuggestion
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return

        if (
            current.aiSuggestionMode ==
            NoteAiMode.Title
        ) {
            return
        }

        updateText(
            suggestion,
        )

        clearAiSuggestion()
    }

    fun appendAiSuggestionToText() {
        val current =
            _uiState.value

        val suggestion =
            current.aiSuggestion
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return

        if (
            current.aiSuggestionMode ==
            NoteAiMode.Title
        ) {
            return
        }

        val nextText =
            if (
                current.text.isBlank()
            ) {
                suggestion
            } else {
                current.text
                    .trimEnd() +
                        "\n\n" +
                        suggestion
            }

        updateText(
            nextText,
        )

        clearAiSuggestion()
    }

    fun useAiSuggestionAsTitle() {
        val current =
            _uiState.value

        val suggestion =
            current.aiSuggestion
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return

        if (
            current.aiSuggestionMode !=
            NoteAiMode.Title
        ) {
            return
        }

        updateTitle(
            suggestion,
        )

        clearAiSuggestion()
    }

    fun clearAiSuggestion() {
        _uiState.value =
            _uiState.value.copy(
                aiSuggestion =
                    null,

                aiSuggestionMode =
                    null,

                aiErrorMessage =
                    null,
            )
    }

    // MARK: - Conflict

    fun resolveConflictUsingRemote(
        onDeleted: () -> Unit,
    ) {
        viewModelScope.launch {
            val note =
                currentNote
                    ?: return@launch

            try {
                val resolved =
                    noteLocalRepository
                        .resolveConflictUsingRemote(
                            note =
                                note,
                        )

                /*
                 * Remote Deleted + 使用云端：
                 * Room row 已经被删除。
                 */
                if (
                    resolved == null
                ) {
                    observeNoteJob
                        ?.cancel()

                    observeNoteJob =
                        null

                    observedLocalKey =
                        null

                    currentNote =
                        null

                    onDeleted()

                    return@launch
                }

                adoptResolvedNote(
                    note =
                        resolved,
                )

            } catch (
                error: Throwable
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "使用云端版本失败",
                    )
            }
        }
    }

    fun resolveConflictKeepingLocal() {
        viewModelScope.launch {
            val note =
                currentNote
                    ?: return@launch

            val state =
                _uiState.value

            try {
                /*
                 * Conflict 可能是在 Editing debounce
                 * 尚未完成时出现。
                 *
                 * UI 中的 title/text 可能比 currentNote
                 * 多最后几百毫秒的输入。
                 *
                 * “保留本机版本”必须保护这些输入。
                 */
                val localVersion =
                    note.copy(
                        title =
                            state.title,

                        text =
                            state.text,

                        pinned =
                            state.pinned,
                    )

                val resolved =
                    noteLocalRepository
                        .resolveConflictKeepingLocal(
                            note =
                                localVersion,
                        )
                        ?: return@launch

                adoptResolvedNote(
                    note =
                        resolved,
                )

            } catch (
                error: Throwable
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "保留本地版本失败",
                    )
            }
        }
    }

    private suspend fun adoptResolvedNote(
        note: LocalNoteEntity,
    ) {
        val previousLocalKey =
            currentNote
                ?.localKey

        if (
            previousLocalKey != null &&
            previousLocalKey != note.localKey
        ) {
            noteImageService
                .rebindNote(
                    ownerUserId =
                        ownerUserId,

                    oldNoteLocalKey =
                        previousLocalKey,

                    newNoteLocalKey =
                        note.localKey,
                )
        }

        currentNote =
            note

        val baseState =
            NoteEditorUiState
                .fromNote(
                    note,
                )

        _uiState.value =
            baseState.copy(
                images =
                    _uiState.value.images,

                mode =
                    NoteEditorMode.Reading,

                conflict =
                    conflictUiModel(
                        note,
                    ),

                errorMessage =
                    null,
            )

        observeCurrentNote(
            localKey =
                note.localKey,
        )

        observeCurrentNoteImages(
            noteLocalKey =
                note.localKey,
        )
    }

    private fun conflictUiModel(
        note: LocalNoteEntity,
    ): NoteConflictUiModel? {
        if (
            note.syncState !=
            NoteSyncState.Conflict
        ) {
            return null
        }

        /*
         * NOTE_VERSION_CONFLICT 刚发生时：
         *
         * syncState 已经是 Conflict，
         * 但 Pull 可能还没有拿到完整 remote snapshot。
         *
         * 这时返回 null 是正常状态，
         * Screen 会显示“正在获取最新云端版本”。
         */
        val snapshot =
            runCatching {
                noteLocalRepository
                    .decodeConflictSnapshot(
                        note.conflictSnapshotJson,
                    )
            }.getOrNull()
                ?: return null

        return NoteConflictUiModel(
            title =
                snapshot.title,

            text =
                snapshot.text,

            dayKey =
                snapshot.dayKey,

            onlyDay =
                snapshot.onlyDay,

            pinned =
                snapshot.pinned,

            remoteStatus =
                snapshot.parsedRemoteStatus,

            version =
                snapshot.version,

            updatedAt =
                snapshot.updatedAt,
        )
    }

    // MARK: - Content

    fun updateTitle(
        value: String,
    ) {
        if (
            !isEditingCurrentNote()
        ) {
            return
        }

        if (
            _uiState.value.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        if (
            _uiState.value.title ==
            value
        ) {
            return
        }

        _uiState.value =
            _uiState.value.copy(
                title =
                    value,

                errorMessage =
                    null,
            )

        scheduleSave()
    }

    /**
     * Legacy plain-text path (currently used by AI replace/append).
     * It deliberately creates a plain BlockContent and refreshes the rich editor.
     */
    fun updateText(value: String) {
        if (!isEditingCurrentNote()) return
        if (_uiState.value.syncState == NoteSyncState.Conflict) return

        val current = _uiState.value
        if (current.text == value) return

        val nextContentJson = NoteContentCodec.stringify(
            NoteContentCodec.fromPlainText(value)
        )
        _uiState.value = current.copy(
            text = value,
            contentJson = nextContentJson,
            editorResetVersion = current.editorResetVersion + 1,
            errorMessage = null,
        )
        scheduleSave()
    }

    /**
     * Native rich-editor path. Text is always a projection of Content.
     * The editor handles IME and selection; VM only receives committed snapshots.
     */
    fun updateRichContent(content: BlockContent) {
        if (!isEditingCurrentNote()) return
        if (_uiState.value.syncState == NoteSyncState.Conflict) return
        require(content.v == 1) { "Unsupported BlockContent version" }

        val element = richContentJson.encodeToJsonElement(
            BlockContent.serializer(), content
        )
        val encoded = NoteContentCodec.stringify(element)
        val plainText = NoteContentCodec.extractText(element)
        val current = _uiState.value
        if (current.contentJson == encoded && current.text == plainText) return

        _uiState.value = current.copy(
            text = plainText,
            contentJson = encoded,
            errorMessage = null,
        )
        scheduleSave()
    }

    // MARK: - Pin

    fun setPinned(
        value: Boolean,
    ) {
        val state = _uiState.value

        if (
            !state.canEdit ||
            state.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        if (state.pinned == value) {
            return
        }

        _uiState.value = state.copy(
            pinned = value,
            errorMessage = null,
        )

        viewModelScope.launch {
            try {
                val note = currentNote ?: return@launch
                saveJob ?.cancel()
                saveJob = null
                val latestNote = saveCurrentContent() ?: note
                val updated = noteLocalRepository
                    .setPinned(
                        note = latestNote,
                        pinned = value,
                    )
                currentNote = updated

                _uiState.value = _uiState.value.copy(
                    pinned = updated.pinned,
                    syncState = updated.syncState,
                    errorMessage = null,
                )
            } catch (
                error: Throwable
            ) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "更新置顶状态失败",
                )
            }
        }
    }

    // MARK: - Delete

    fun deleteNote(
        onDeleted: () -> Unit,
    ) {
        viewModelScope.launch {
            saveJob
                ?.cancel()

            saveJob =
                null

            val note =
                currentNote
                    ?: return@launch

            try {
                val marked =
                    noteLocalRepository
                        .markForDelete(
                            note =
                                note,
                        )

                currentNote =
                    marked

                marked
                    ?.ownerUserId
                    ?.let { owner ->
                        noteLocalRepository
                            .requestSync(
                                ownerUserId =
                                    owner,
                            )
                    }

                onDeleted()

            } catch (
                error: Throwable
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "删除记录失败",
                    )
            }
        }
    }

    // MARK: - Flush

    suspend fun flush() {
        saveJob
            ?.cancel()

        saveJob =
            null

        if (
            !isEditingCurrentNote() ||
            _uiState.value.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        saveCurrentContent()
    }

    suspend fun flushAndCleanup() {
        saveJob
            ?.cancel()

        saveJob =
            null

        if (
            _uiState.value.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        if (
            !isEditingCurrentNote()
        ) {
            return
        }

        val note =
            saveCurrentContent()
                ?: return

        if (
            isEmptyNote(
                note,
            )
        ) {
            if (
                note.noteId == null &&
                note.remoteVersion == null
            ) {
                noteImageService
                    .removeAllImagesForNote(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            note.localKey,
                    )

                noteLocalRepository
                    .deleteLocal(
                        note =
                            note,
                    )

                currentNote =
                    null

                return
            }

            val marked =
                noteLocalRepository
                    .markForDelete(
                        note =
                            note,
                    )

            currentNote =
                marked

            marked
                ?.ownerUserId
                ?.let { owner ->
                    noteLocalRepository
                        .requestSync(
                            ownerUserId =
                                owner,
                        )
                }

            return
        }

        note.ownerUserId
            ?.let { owner ->
                noteLocalRepository
                    .requestSync(
                        ownerUserId =
                            owner,
                    )
            }
    }

    fun saveNow() {
        if (
            !isEditingCurrentNote() ||
            _uiState.value.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        saveJob
            ?.cancel()

        saveJob =
            null

        viewModelScope.launch {
            saveCurrentContent()
        }
    }

    // MARK: - Autosave

    private fun scheduleSave() {
        if (
            !isEditingCurrentNote() ||
            _uiState.value.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        saveJob
            ?.cancel()

        saveJob =
            viewModelScope.launch {
                delay(
                    500.milliseconds,
                )

                saveCurrentContent()
            }
    }

    private suspend fun saveCurrentContent():
            LocalNoteEntity? {

        val note =
            currentNote
                ?: return null

        if (
            !isEditingCurrentNote()
        ) {
            return note
        }

        if (
            _uiState.value.syncState ==
            NoteSyncState.Conflict
        ) {
            return note
        }

        val state =
            _uiState.value

        if (
            note.title ==
            state.title &&
            note.text ==
            state.text &&
            note.contentJson ==
            state.contentJson
        ) {
            return note
        }

        return try {
            val updated = if (state.contentJson != null) {
                noteLocalRepository.updateRichContent(
                    note = note,
                    title = state.title,
                    text = state.text,
                    contentJson = state.contentJson,
                )
            } else {
                noteLocalRepository.updateContent(
                    note = note,
                    title = state.title,
                    text = state.text,
                )
            }

            currentNote =
                updated

            _uiState.value =
                _uiState.value.copy(
                    syncState =
                        updated.syncState,

                    errorMessage =
                        null,
                )

            updated

        } catch (
            error: Throwable
        ) {
            _uiState.value =
                _uiState.value.copy(
                    errorMessage =
                        error.message
                            ?: "保存记录失败",
                )

            null
        }
    }

    // MARK: - Guards

    private fun canEditCurrentNote():
            Boolean {

        val state =
            _uiState.value

        return state.canEdit &&
                state.syncState !=
                NoteSyncState.Conflict
    }

    private fun isEditingCurrentNote():
            Boolean {

        val state =
            _uiState.value

        return state.isEditing &&
                state.syncState !=
                NoteSyncState.Conflict
    }

    private suspend fun isEmptyNote(
        note: LocalNoteEntity,
    ): Boolean {
        if (
            note.title.isNotBlank() ||
            note.text.isNotBlank()
        ) {
            return false
        }

        val images =
            noteImageService
                .listImages(
                    ownerUserId =
                        ownerUserId,

                    noteLocalKey =
                        note.localKey,
                )

        return images.isEmpty()
    }

    override fun onCleared() {
        saveJob
            ?.cancel()

        observeNoteJob
            ?.cancel()

        observeImagesJob
            ?.cancel()

        super.onCleared()
    }

    private data class InitialNoteResult(
        val note: LocalNoteEntity?,
        val startEditing: Boolean,
    )

    // MARK: - Note Image

    private fun LocalNoteImageEntity.toEditorImageUi():
            NoteEditorImageUi {

        val localUrl =
            localPath
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { path ->
                    File(
                        path,
                    )
                        .toUri()
                        .toString()
                }

        val remoteUrl =
            imageId
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { remoteImageId ->
                    remotePreviewUrls[
                        remoteImageId
                    ]
                }

        return NoteEditorImageUi(
            localImageKey =
                localImageKey,

            imageId =
                imageId,

            displayUrl =
                localUrl
                    ?: remoteUrl,

            uploadState =
                uploadState,

            uploadError =
                uploadError,
        )
    }

    private fun resolveMissingRemotePreviews(
        images: List<LocalNoteImageEntity>,
    ) {
        val imageIds =
            images
                .asSequence()

                /*
                 * 本地文件存在时，
                 * Editor 直接走 file://，
                 * 不需要 remote preview。
                 */
                .filter { image ->
                    image.localPath
                        .isNullOrBlank()
                }

                .mapNotNull { image ->
                    image.imageId
                        ?.takeIf {
                            it.isNotBlank()
                        }
                }

                .distinct()

                /*
                 * 已有缓存的不重复请求。
                 */
                .filter { imageId ->
                    imageId !in
                            remotePreviewUrls
                }

                /*
                 * 当前已经正在获取的也不重复请求。
                 */
                .filter { imageId ->
                    imageId !in
                            resolvingPreviewImageIds
                }

                .toList()

        imageIds.forEach { imageId ->
            resolvingPreviewImageIds +=
                imageId

            viewModelScope.launch {
                try {
                    val preview =
                        imageRepository
                            .getPreviewUrl(
                                imageId =
                                    imageId,
                            )

                    val previewUrl =
                        preview.url
                            .takeIf {
                                it.isNotBlank()
                            }

                    if (
                        previewUrl != null
                    ) {
                        remotePreviewUrls[
                            imageId
                        ] =
                            previewUrl
                    }

                } catch (
                    _: Throwable,
                ) {
                    /*
                     * Preview 获取失败不影响 Note。
                     *
                     * 不缓存失败状态。
                     *
                     * 后续：
                     * - Room 再次 emission
                     * - 重新打开 Editor
                     *
                     * 都还能继续尝试。
                     */
                } finally {
                    resolvingPreviewImageIds -=
                        imageId

                    /*
                     * URL cache 变化以后重新映射。
                     */
                    updateImageUiState()
                }
            }
        }
    }

    private fun updateImageUiState() {
        val imageUiItems =
            currentImageEntities
                .map { image ->
                    image.toEditorImageUi()
                }

        _uiState.value =
            _uiState.value.copy(
                images =
                    imageUiItems,
            )
    }

    private fun applyObservedImages(
        images: List<LocalNoteImageEntity>,
    ) {
        currentImageEntities =
            images

        updateImageUiState()

        resolveMissingRemotePreviews(
            images =
                images,
        )
    }

    private fun observeCurrentNoteImages(
        noteLocalKey: String,
    ) {
        if (
            observedImageNoteLocalKey ==
            noteLocalKey &&
            observeImagesJob?.isActive ==
            true
        ) {
            return
        }

        observeImagesJob
            ?.cancel()

        observedImageNoteLocalKey =
            noteLocalKey

        observeImagesJob =
            viewModelScope.launch {
                noteImageService
                    .observeImages(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            noteLocalKey,
                    )
                    .collectLatest { images ->
                        applyObservedImages(
                            images =
                                images,
                        )
                    }
            }
    }

    fun addImage(
        sourceUri: Uri,
    ) {
        val state =
            _uiState.value

        if (
            !state.canEdit ||
            state.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        val note =
            currentNote
                ?: return

        viewModelScope.launch {
            try {
                noteImageService
                    .addImage(
                        ownerUserId =
                            ownerUserId,

                        noteLocalKey =
                            note.localKey,

                        sourceUri =
                            sourceUri,
                    )

                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            null,
                    )

            } catch (
                error: Throwable,
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "添加图片失败",
                    )
            }
        }
    }

    fun retryImageUpload(
        localImageKey: String,
    ) {
        viewModelScope.launch {
            try {
                noteImageService
                    .retryUpload(
                        ownerUserId =
                            ownerUserId,

                        localImageKey =
                            localImageKey,
                    )

            } catch (
                error: Throwable,
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "图片上传失败",
                    )
            }
        }
    }

    fun removeImage(
        localImageKey: String,
    ) {
        val state =
            _uiState.value

        if (
            !state.canEdit ||
            state.syncState ==
            NoteSyncState.Conflict
        ) {
            return
        }

        viewModelScope.launch {
            try {
                noteImageService
                    .removeImage(
                        ownerUserId =
                            ownerUserId,

                        localImageKey =
                            localImageKey,
                    )

                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            null,
                    )

            } catch (
                error: Throwable,
            ) {
                _uiState.value =
                    _uiState.value.copy(
                        errorMessage =
                            error.message
                                ?: "删除图片失败",
                    )
            }
        }
    }

}