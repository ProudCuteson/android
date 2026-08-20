package top.role.android.feature.bounty

fun bountyKindLabel(
    kind: String,
): String {
    return when (kind) {
        "task" -> "任务"
        "person" -> "招募"
        "article" -> "作品"
        else -> kind
    }
}