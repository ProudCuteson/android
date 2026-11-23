package top.role.android.feature.entity.service
import top.role.android.feature.content.picker.ContentSelection

fun ContentSelection.toServiceRef(
    purpose: String,
) = EntityServiceReference(
    targetType = type,
    targetId = id,
    label = title.takeIf { it.isNotBlank() },
    purpose = purpose,
)

/**
 * 只将业务需要的字段传给 Convex。
 * 不直接将 Kotlin data class 放进 Mutation args。
 */
fun EntityServiceReference.toConvexArg():
        Map<String, Any> = buildMap {

    put("targetType", targetType)

    targetId
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { put("targetId", it) }

    label
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { put("label", it) }

    purpose
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { put("purpose", it) }

    url
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.let { put("url", it) }
}