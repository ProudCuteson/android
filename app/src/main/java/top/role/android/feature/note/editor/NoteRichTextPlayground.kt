package top.role.android.feature.note.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.feature.note.editor.richtext.RichTextContentMapper
import top.role.android.feature.note.editor.richtext.RichTextDebugPanel
import top.role.android.feature.note.editor.richtext.RichTextEditorField
import top.role.android.feature.note.editor.richtext.RichTextEditorState
import top.role.android.feature.note.editor.richtext.RichTextRoundTripPanel
import top.role.android.feature.note.editor.richtext.RichTextRoundTripResult
import top.role.android.feature.note.editor.richtext.RichTextToolbar
import top.role.android.feature.note.editor.richtext.compareRichTextRoundTrip

@Composable
fun NoteRichTextPlayground() {
    var editor by remember {
        mutableStateOf(RichTextEditorState(RichTextContentMapper.sample()))
    }
    var roundTripResult by remember { mutableStateOf<RichTextRoundTripResult?>(null) }

    // Swapping the editor on reload also restarts this observer for its new TextFieldState.
    LaunchedEffect(editor) {
        snapshotFlow {
            Triple(
                editor.field.text.toString(),
                editor.field.selection,
                editor.field.composition,
            )
        }.collect { editor.onNativeStateChanged() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RichTextEditorField(editor)

            RichTextRoundTripPanel(
                result = roundTripResult,
                onRun = {
                    val before = editor.export()
                    val restoredEditor = RichTextEditorState(before)
                    val after = restoredEditor.export()
                    roundTripResult = compareRichTextRoundTrip(before, after)
                    editor = restoredEditor
                },
            )

            RichTextDebugPanel(editor)
        }

        HorizontalDivider()
        Surface(color = MaterialTheme.colorScheme.surface) {
            RichTextToolbar(
                editor = editor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
