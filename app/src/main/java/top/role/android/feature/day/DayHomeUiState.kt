package top.role.android.feature.day

import top.role.android.core.database.note.LocalNoteEntity
import top.role.android.core.model.note.DayKey

data class DayHomeUiState(
    val selectedDayKey: String = DayKey.today(),
    val monthStartDayKey: String = DayKey.monthRange(DayKey.today()).start,
    val monthEndDayKeyExclusive: String = DayKey.monthRange(DayKey.today()).endExclusive,
    val monthNotes: List<LocalNoteEntity> = emptyList(),
    val imageCounts: Map<String, Int> = emptyMap(),
    val loading: Boolean = false,
    val errorMessage: String? = null,
) {
    val dottedDayKeys: Set<String>
        get() = monthNotes.mapNotNull { it.dayKey }.toSet()

    val selectedDayDiary: LocalNoteEntity?
        get() = monthNotes.firstOrNull { note ->
                    note.dayKey == selectedDayKey && note.onlyDay
                }

    val selectedDayNotes: List<LocalNoteEntity>
        get() = monthNotes.filter { note ->
                    note.dayKey == selectedDayKey && !note.onlyDay
                }

    val selectedDayHasNote: Boolean
        get() = selectedDayKey in dottedDayKeys

    val selectedDayHasDiary: Boolean
        get() = selectedDayDiary != null

    val recentNotes: List<LocalNoteEntity>
        get() = monthNotes.take(20)

}