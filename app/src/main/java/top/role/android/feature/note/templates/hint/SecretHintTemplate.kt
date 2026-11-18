package top.role.android.feature.note.templates.hint

import java.util.UUID
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import top.role.android.core.model.note.content.NoteContentCodec

enum class HintCategory(val title: String) {
    WEB_APP("网页与 App"),
    DEVICE("设备与系统"),
    SERVICE("其他服务"),
    OTHER("其他"),
}

enum class HintDigits(val title: String) {
    FORWARD("顺序"),
    REVERSE("倒序"),
    RANDOM("随机"),
}

enum class HintPart(val title: String) {
    INPUT1("输入1"),
    DIGITS("数字"),
    INPUT2("输入2"),
}

data class SecretHintDraft(
    val category: HintCategory = HintCategory.WEB_APP,
    val name: String = "",
    val token: String = "",           // 账号/邮箱/用户名；UI 名称为「令牌」
    val input1: String = "",
    val input2: String = "",
    val digitLength: Int = 6,
    val digits: HintDigits = HintDigits.FORWARD,
    val randomPattern: String = "", // 仅含 1..N 的位置排列，不是真实数字
    val order: List<HintPart> = listOf(HintPart.INPUT1, HintPart.DIGITS, HintPart.INPUT2),
    val reminder: String = "",
    val cue6: String = "",          // 首次初始化 Note 开头时使用
    val cue8: String = "",
) {
    init {
        require(digitLength == 6 || digitLength == 8) { "数字模块只支持 6 位或 8 位" }
        require(order.size == 3 && order.toSet() == HintPart.entries.toSet()) {
            "组合顺序必须包含输入1、数字模块、输入2"
        }
    }

    val basePattern: String get() = if (digitLength == 6) "123456" else "12345678"

    fun digitsExample(): String = when (digits) {
        HintDigits.FORWARD -> basePattern
        HintDigits.REVERSE -> basePattern.reversed()
        HintDigits.RANDOM -> {
            require(randomPattern.length == digitLength &&
                    randomPattern.toSet() == basePattern.toSet()) {
                "随机位置尚未生成，请点击换一个"
            }
            randomPattern
        }
    }

    fun preview(): String = order.joinToString("") { part ->
        when (part) {
            HintPart.INPUT1 -> input1
            HintPart.DIGITS -> digitsExample()
            HintPart.INPUT2 -> input2
        }
    }
}

/**
 * 把小暗号输入写成普通 BlockContent v1。
 * 只插入新行，不改动旧行 / Run ID；不负责 Note 的持久化与同步。
 */
object SecretHintTemplate {
    const val DEFAULT_NOTE_TITLE = "我的小暗号"
    private const val INTRO_TITLE = "记忆点"

    fun hasIntro(existingContentJson: String?): Boolean {
        if (existingContentJson.isNullOrBlank()) return false
        val content = NoteContentCodec.parse(existingContentJson) as? JsonObject ?: return false
        val lines = content["lines"] as? JsonArray ?: return false
        return lines.any { value ->
            val line = value as? JsonObject ?: return@any false
            lineVariant(line) == "h2" && lineText(line).trim() == INTRO_TITLE
        }
    }

    /** includeIntro 为 Writer 一次性决定，不能每次保存都根据标题猜测。 */
    fun append(
        existingContentJson: String?,
        draft: SecretHintDraft,
        includeIntro: Boolean = false,
    ): String {
        require(draft.name.isNotBlank()) { "请输入名称" }

        val existing = if (existingContentJson.isNullOrBlank()) {
            null
        } else {
            NoteContentCodec.parse(existingContentJson) as? JsonObject
                ?: error("Note 内容无法解析，已取消追加")
        }
        if (existing != null && existing["v"]?.toString() != "1") {
            error("不支持当前 Note 的内容版本，已取消追加")
        }

        val oldLines = when (val value = existing?.get("lines")) {
            null -> if (existing == null) emptyList() else error("Note 内容缺少 lines")
            is JsonArray -> value.map { it as? JsonObject ?: error("Note 行格式不正确") }
            else -> error("Note 内容缺少有效的 lines")
        }

        val result = oldLines.toMutableList()
        if (includeIntro && !hasIntro(existingContentJson)) {
            result.addAll(0, buildIntro(draft))
        }

        val categoryIndex = result.indexOfFirst { line ->
            lineVariant(line) == "h2" && lineText(line).trim() == draft.category.title
        }
        val entry = buildEntry(draft)
        if (categoryIndex < 0) {
            result += makeLine("h2", draft.category.title)
            result.addAll(entry)
        } else {
            val nextCategory = (categoryIndex + 1 until result.size)
                .firstOrNull { lineVariant(result[it]) == "h2" }
                ?: result.size
            result.addAll(nextCategory, entry)
        }

        val merged = JsonObject(
            (existing?.toMap() ?: mapOf("v" to JsonPrimitive(1))) +
                    ("lines" to JsonArray(result))
        )
        return NoteContentCodec.stringify(merged)
    }

    private fun buildIntro(draft: SecretHintDraft): List<JsonObject> = listOf(
        makeLine("h2", INTRO_TITLE),
        makeLine("paragraph", "6 位：123456（记忆点：${draft.cue6.trim().ifBlank { "____" }}）"),
        makeLine("paragraph", "8 位：12345678（记忆点：${draft.cue8.trim().ifBlank { "____" }}）"),
        makeLine("small", "上面的数字只代表位置。真实数字留在自己的记忆里。"),
        makeLine("small", "提示：新增建议从「场景 → 小暗号」进入"),
        makeLine("small", "仅供个人助记，请勿在这里填写真实密码或密钥。"),
    )

    private fun buildEntry(draft: SecretHintDraft): List<JsonObject> = buildList {
        add(makeLine("h3", draft.name.trim()))
        if (draft.token.isNotBlank()) add(
            fieldLine("令牌",
            draft.token.trim())
        )
        add(passwordLine(draft))
        if (draft.reminder.isNotBlank()) add(
            makeLine("small",
            "提示：${draft.reminder.trim()}")
        )
    }

    private fun makeLine(variant: String, text: String): JsonObject = JsonObject(
        mapOf(
            "id" to JsonPrimitive(UUID.randomUUID().toString()),
            "variant" to JsonPrimitive(variant),
            "runs" to JsonArray(listOf(makeRun(text))),
        )
    )

    private fun fieldLine(label: String, value: String): JsonObject = JsonObject(
        mapOf(
            "id" to JsonPrimitive(UUID.randomUUID().toString()),
            "variant" to JsonPrimitive("paragraph"),
            "runs" to JsonArray(listOf(
                makeRun("$label："),
                makeRun(value))
            ),
        )
    )

    private fun passwordLine(
        draft: SecretHintDraft,
    ): JsonObject {
        val runs = mutableListOf<JsonObject>()

        runs += makeRun("口令：")

        draft.order.forEach { part ->
            when (part) {
                HintPart.INPUT1 -> {
                    runs += makeRun(draft.input1)
                }

                HintPart.INPUT2 -> {
                    runs += makeRun(draft.input2)
                }

                HintPart.DIGITS -> {
                    runs += makeRun(
                        text = draft.digitsExample(),
                        bold = true,
                        color = "#16803C",
                    )
                }
            }
        }

        return JsonObject(
            mapOf(
                "id" to JsonPrimitive(
                    UUID.randomUUID().toString()
                ),
                "variant" to JsonPrimitive("paragraph"),
                "runs" to JsonArray(runs),
            )
        )
    }

    private fun makeRun(
        text: String,
        bold: Boolean = false,
        color: String? = null,
    ): JsonObject {
        val fields = mutableMapOf<String, JsonElement>(
            "id" to JsonPrimitive(
                UUID.randomUUID().toString()
            ),
            "text" to JsonPrimitive(text),
        )

        val marks = mutableMapOf<String, JsonElement>()

        if (bold) {
            marks["bold"] = JsonPrimitive(true)
        }

        if (color != null) {
            marks["color"] = JsonPrimitive(color)
        }

        if (marks.isNotEmpty()) {
            fields["mark"] = JsonObject(marks)
        }

        return JsonObject(fields)
    }

    private fun lineVariant(line: JsonObject): String =
        (line["variant"] as? JsonPrimitive)?.contentOrNull.orEmpty()

    private fun lineText(line: JsonObject): String =
        (line["runs"] as? JsonArray).orEmpty().joinToString("") { value ->
            ((value as? JsonObject)?.get("text") as? JsonPrimitive)?.contentOrNull.orEmpty()
        }
}
