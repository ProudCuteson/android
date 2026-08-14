package top.role.android.feature.reading.components

import androidx.compose.ui.text.style.TextAlign

fun resolveMobileTextAlign(
    align: String?,
): TextAlign {
    return when (align) {
        "center" ->
            TextAlign.Center

        "right" ->
            TextAlign.Right

        else ->
            TextAlign.Left
    }
}