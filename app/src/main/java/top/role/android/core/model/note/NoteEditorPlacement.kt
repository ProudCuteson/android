package top.role.android.core.model.note

enum class NoteEditorPlacement {
    Normal,
    Dated,
    DayOnly;

    val onlyDay: Boolean
        get() =
            this == DayOnly

    val shouldSendDayKey: Boolean
        get() =
            this == Dated ||
                    this == DayOnly
}