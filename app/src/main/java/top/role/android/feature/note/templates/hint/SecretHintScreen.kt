package top.role.android.feature.note.templates.hint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private fun shuffledPattern(length: Int): String =
    (1..length).map(Int::toString).shuffled().joinToString("")

/** 结构化助记输入；只产生 Draft，真正写入普通 Note 由 onSave 完成。 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretHintScreen(
    onBack: () -> Unit,
    onSave: suspend (SecretHintDraft) -> Unit,
) {
    var category by rememberSaveable { mutableStateOf(HintCategory.WEB_APP) }
    var name by rememberSaveable { mutableStateOf("") }
    var token by rememberSaveable { mutableStateOf("") }
    var input1 by rememberSaveable { mutableStateOf("") }
    var input2 by rememberSaveable { mutableStateOf("") }
    var digitLength by rememberSaveable { mutableStateOf(6) }
    var digits by rememberSaveable { mutableStateOf(HintDigits.FORWARD) }
    var random6 by rememberSaveable { mutableStateOf("") }
    var random8 by rememberSaveable { mutableStateOf("") }
    var orderCsv by rememberSaveable { mutableStateOf("INPUT1,DIGITS,INPUT2") }
    var reminder by rememberSaveable { mutableStateOf("") }
    var cue6 by rememberSaveable { mutableStateOf("") }
    var cue8 by rememberSaveable { mutableStateOf("") }
    var showCues by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun ensureRandom(length: Int) {
        if (length == 6 && random6.isBlank()) random6 = shuffledPattern(6)
        if (length == 8 && random8.isBlank()) random8 = shuffledPattern(8)
    }

    val order = orderCsv.split(",").map(HintPart::valueOf)
    val draft = SecretHintDraft(
        category = category,
        name = name,
        token = token,
        input1 = input1,
        input2 = input2,
        digitLength = digitLength,
        digits = digits,
        randomPattern = if (digitLength == 6) random6 else random8,
        order = order,
        reminder = reminder,
        cue6 = cue6,
        cue8 = cue8,
    )

    fun swapAdjacent(index: Int) {
        val changed = order.toMutableList()
        val old = changed[index]
        changed[index] = changed[index + 1]
        changed[index + 1] = old
        orderCsv = changed.joinToString(",") { it.name }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("小暗号") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "给未来的自己留一点线索，真实数字存在脑海。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HintCategory.entries.toList()) { option ->
                        FilterChip(
                            selected = category == option,
                            onClick = { category = option },
                            label = { Text(option.title) },
                        )
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称（例如：网易）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedTextField(
                    value = token,
                    onValueChange = { token = it },
                    label = { Text("令牌（用户名 / 邮箱，可选）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = input1,
                        onValueChange = { input1 = it },
                        label = { Text("输入1") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = input2,
                        onValueChange = { input2 = it },
                        label = { Text("输入2") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            item {
                Text("脑海数字", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(6, 8).forEach { length ->
                        FilterChip(
                            selected = digitLength == length,
                            onClick = {
                                digitLength = length
                                if (digits == HintDigits.RANDOM) ensureRandom(length)
                            },
                            label = { Text("$length 位") },
                        )
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(HintDigits.entries.toList()) { option ->
                        FilterChip(
                            selected = digits == option,
                            onClick = {
                                digits = option
                                if (option == HintDigits.RANDOM) ensureRandom(digitLength)
                            },
                            label = { Text(option.title) },
                        )
                    }
                }
            }
            if (digits == HintDigits.RANDOM) {
                item {
                    TextButton(onClick = {
                        if (digitLength == 6) random6 = shuffledPattern(6)
                        else random8 = shuffledPattern(8)
                    }) {
                        Text("随机排列：${draft.digitsExample()}   ·   换一个")
                    }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    order.forEachIndexed { index, part ->
                        item {
                            Text(
                                part.title,
                                modifier = Modifier.padding(vertical = 12.dp),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        if (index < order.lastIndex) {
                            item {
                                TextButton(
                                    onClick = { swapAdjacent(index) },
                                    contentPadding = PaddingValues(horizontal = 4.dp),
                                ) { Text("⇄") }
                            }
                        }
                    }
                }
            }
            item {
                TextButton(
                    onClick = { showCues = !showCues },
                    contentPadding = PaddingValues(
                        horizontal = 0.dp,
                        vertical = 0.dp,
                    ),
                    modifier = Modifier.height(32.dp),
                ) {
                    Text(
                        if (showCues) "收起数字记忆点"
                        else "给数字加记忆点（可选）"
                    )
                }
            }
            if (showCues) {
                item {
                    Text(
                        "首次建立固定开头写入。不要填写真实数字。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item {
                    OutlinedTextField(
                        value = cue6,
                        onValueChange = { cue6 = it },
                        label = { Text("123456 的记忆点") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    OutlinedTextField(
                        value = cue8,
                        onValueChange = { cue8 = it },
                        label = { Text("12345678 的记忆点") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            item {
                OutlinedTextField(
                    value = reminder,
                    onValueChange = { reminder = it },
                    label = { Text("记忆点（选填）") },
                    minLines = 1,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item { HorizontalDivider() }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("示意", style = MaterialTheme.typography.labelMedium)
                    Text(
                        draft.preview(),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        "123456 / 12345678 自动添加格式写入记录。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (errorMessage != null) {
                item {
                    Text(
                        errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            item {
                Button(
                    enabled = name.isNotBlank() && !saving,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            saving = true
                            errorMessage = null
                            try {
                                onSave(draft)
                                onBack()
                            } catch (error: CancellationException) {
                                throw error
                            } catch (error: Exception) {
                                errorMessage = error.message ?: "保存失败，请再试一次"
                            } finally {
                                saving = false
                            }
                        }
                    },
                ) {
                    if (saving) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            strokeWidth = 2.dp,
                        )
                    }
                    Text("追加到我的小暗号")
                }
            }
        }
    }
}
