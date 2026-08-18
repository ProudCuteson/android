package top.role.android.core.data.note.ai

enum class NoteAiMode(
    val rawValue: String,
) {
    Polish(
        rawValue =
            "polish",
    ),

    Expand(
        rawValue =
            "expand",
    ),

    Shorten(
        rawValue =
            "shorten",
    ),

    Title(
        rawValue =
            "title",
    ),

    Free(
        rawValue =
            "free",
    ),
}