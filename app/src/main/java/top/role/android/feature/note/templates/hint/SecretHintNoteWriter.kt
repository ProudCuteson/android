package top.role.android.feature.note.templates.hint

import android.content.Context
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.model.note.NoteRemoteStatus
import top.role.android.core.model.note.NoteSyncState
import top.role.android.core.model.note.content.NoteContentCodec

/**
 * 只维护场景入口到普通 Note 的本地引用；内容写入、版本与同步使用现有 Repository。
 */
class SecretHintNoteWriter(
    context: Context,
    private val notes: NoteLocalRepository,
) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "note_scene_targets", Context.MODE_PRIVATE
    )
    private val mutex = Mutex()

    suspend fun append(localOwnerUserId: String, draft: SecretHintDraft): LocalNoteEntity =
        mutex.withLock {
            require(localOwnerUserId.isNotBlank()) { "本地 Note 身份为空" }
            require(draft.name.isNotBlank()) { "请输入名称" }

            val savedClientKey = preferences.getString(linkKey(localOwnerUserId), null)
            val existing = savedClientKey?.let { notes.findByClientKey(localOwnerUserId, it) }
            if (savedClientKey != null && existing == null) {
                error("之前关联的 Note 暂未找到，请先恢复或重新选择目标 Note")
            }

            val created = existing == null
            val note = existing ?: notes.createNormalNote(localOwnerUserId)
            check(note.remoteStatus == NoteRemoteStatus.Active &&
                    note.pendingRemoteStatus == null &&
                    note.syncState != NoteSyncState.Conflict
            ) { "当前 Note 无法追加，请先解决冲突或更换目标 Note" }

            val introKey = introKey(localOwnerUserId, note)
            val initialized = preferences.getBoolean(introKey, false)
            // 兼容已用旧 V1 创建的测试 Note：缺开头时补一次。
            // 记录初始化标志后，用户手动删除或修改开头都不会被下次保存恢复。
            val includeIntro = !initialized && !SecretHintTemplate.hasIntro(note.contentJson)

            val contentJson = SecretHintTemplate.append(
                existingContentJson = note.contentJson,
                draft = draft,
                includeIntro = includeIntro,
            )
            val content = requireNotNull(NoteContentCodec.parse(contentJson))
            val updated = notes.updateRichContent(
                note = note,
                title = note.title.ifBlank { SecretHintTemplate.DEFAULT_NOTE_TITLE },
                text = NoteContentCodec.extractText(content),
                contentJson = contentJson,
            )
            val finalNote = if (created) notes.setPinned(updated, true) else updated

            preferences.edit()
                .putString(linkKey(localOwnerUserId), finalNote.clientKey)
                .putBoolean(introKey, true)
                .apply()
            notes.requestSync(localOwnerUserId)
            finalNote
        }

    suspend fun bindExistingNote(localOwnerUserId: String, clientKey: String) = mutex.withLock {
        val note = notes.findByClientKey(localOwnerUserId, clientKey)
            ?: error("目标 Note 不存在")
        check(note.remoteStatus == NoteRemoteStatus.Active &&
                note.pendingRemoteStatus == null &&
                note.syncState != NoteSyncState.Conflict
        ) { "目标 Note 当前不可编辑" }
        preferences.edit().putString(linkKey(localOwnerUserId), note.clientKey).apply()
    }

    /** 清理关联，不删除 Note 或原有的一次性开头标志。 */
    fun clearLink(localOwnerUserId: String) {
        preferences.edit().remove(linkKey(localOwnerUserId)).apply()
    }

    private fun linkKey(owner: String) = "secret_hint.target.$owner"

    private fun introKey(owner: String, note: LocalNoteEntity) =
        "secret_hint.intro.$owner.${note.clientKey ?: note.localKey}"
}
