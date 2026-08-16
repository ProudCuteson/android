package top.role.android.core.data.note

import java.util.UUID

object LocalNoteKeys {

    fun existing(
        ownerUserId: String,
        noteId: String,
    ): String {
        return "user:$ownerUserId:note:$noteId"
    }

    fun day(
        ownerUserId: String,
        dayKey: String,
    ): String {
        return "user:$ownerUserId:day:$dayKey"
    }

    fun new(
        ownerUserId: String,
    ): String {
        return "user:$ownerUserId:new:${UUID.randomUUID()}"
    }
}