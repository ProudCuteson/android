package top.role.android.core.data.note

import java.util.UUID

object LocalNoteKeys {

    fun existing(
        localOwnerUserId: String,
        noteId: String,
    ): String {
        return "user:$localOwnerUserId:note:$noteId"
    }

    fun day(
        localOwnerUserId: String,
        dayKey: String,
    ): String {
        return "user:$localOwnerUserId:day:$dayKey"
    }

    fun new(
        localOwnerUserId: String,
    ): String {
        return "user:$localOwnerUserId:new:${UUID.randomUUID()}"
    }
}