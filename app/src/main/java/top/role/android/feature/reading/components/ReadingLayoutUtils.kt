package top.role.android.feature.reading.components

import androidx.compose.ui.text.style.TextAlign

fun resolveMobileTextAlign(
    align: String?,
): TextAlign {
    return when (align) {
        "left" -> TextAlign.Left
        "center" -> TextAlign.Center
        "right" -> TextAlign.Right
        "justify" -> TextAlign.Justify
        else -> TextAlign.Left
    }
}