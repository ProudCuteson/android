package top.role.android.feature.note.editor.richtext

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.core.model.note.content.BlockContent
import top.role.android.core.model.note.content.BlockMark
import top.role.android.core.model.note.content.BlockRun

/** Checks a real export → new editor import → export cycle. Nothing is persisted. */
data class RichTextRoundTripResult(
    val textMatches: Boolean,
    val blockMatches: Boolean,
    val markMatches: Boolean,
    val lineIdsMatch: Boolean,
    val runIdsMatch: Boolean,
    val idsAreUnique: Boolean,
) {
    val passed: Boolean
        get() = textMatches && blockMatches && markMatches &&
                lineIdsMatch && runIdsMatch && idsAreUnique
}

/** Compare semantics separately from IDs so a failed result is easy to diagnose. */
fun compareRichTextRoundTrip(
    before: BlockContent,
    after: BlockContent,
): RichTextRoundTripResult {
    val textMatches =
        RichTextContentMapper.plainText(before) == RichTextContentMapper.plainText(after)

    val blockMatches = before.v == after.v &&
            before.lines.size == after.lines.size &&
            before.lines.zip(after.lines).all { (a, b) ->
                a.variant == b.variant &&
                        a.list == b.list &&
                        a.align == b.align &&
                        a.lineHeight == b.lineHeight
            }

    val markMatches = before.lines.size == after.lines.size &&
            before.lines.zip(after.lines).all { (a, b) ->
                normalizedRuns(a.runs) == normalizedRuns(b.runs)
            }

    val beforeLines = before.lines.map { it.id }
    val afterLines = after.lines.map { it.id }
    val lineIdsMatch = beforeLines == afterLines

    val runIdsMatch = before.lines.size == after.lines.size &&
            before.lines.zip(after.lines).all { (a, b) ->
                a.runs.map { it.id } == b.runs.map { it.id }
            }

    val runIds = after.lines.flatMap { line -> line.runs.map { it.id } }
    val idsAreUnique = afterLines.size == afterLines.toSet().size &&
            runIds.size == runIds.toSet().size

    return RichTextRoundTripResult(
        textMatches = textMatches,
        blockMatches = blockMatches,
        markMatches = markMatches,
        lineIdsMatch = lineIdsMatch,
        runIdsMatch = runIdsMatch,
        idsAreUnique = idsAreUnique,
    )
}

/** Adjacent runs with identical marks represent the same visible formatting. */
private fun normalizedRuns(runs: List<BlockRun>): List<Pair<String, BlockMark?>> {
    val result = mutableListOf<Pair<String, BlockMark?>>()
    runs.forEach { run ->
        val mark = run.mark?.takeUnless { it == BlockMark() }
        if (result.isNotEmpty() && result.last().second == mark) {
            val previous = result.removeAt(result.lastIndex)
            result.add((previous.first + run.text) to mark)
        } else {
            result.add(run.text to mark)
        }
    }
    return result
}

@Composable
fun RichTextRoundTripPanel(
    result: RichTextRoundTripResult?,
    onRun: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        OutlinedButton(onClick = onRun) {
            Text("导出 → 重新载入 → 再导出")
        }

        if (result != null) {
            Text(
                if (result.passed) "上次往返检查：全部通过" else "上次往返检查：发现差异",
                style = MaterialTheme.typography.labelMedium,
                color = if (result.passed) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error,
            )
            val details = listOf(
                "正文" to result.textMatches,
                "段落属性" to result.blockMatches,
                "文字样式" to result.markMatches,
                "Line ID" to result.lineIdsMatch,
                "Run ID" to result.runIdsMatch,
                "ID 唯一性" to result.idsAreUnique,
            )
            Text(
                details.joinToString(" · ") { (label, ok) -> "$label:${if (ok) "✓" else "✗"}" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "已经将重新载入的内容显示在上方编辑器中；继续编辑仍仅存在内存。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}