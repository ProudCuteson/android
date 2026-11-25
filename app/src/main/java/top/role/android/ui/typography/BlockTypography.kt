package top.role.android.ui.typography

import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.math.roundToInt

/**
 * BlockContent 的排版应用场景。
 *
 * 不同场景共用文字语义，
 * 但拥有各自独立的排版基准。
 */
enum class BlockTypographyProfile {
    Note,
    Doc,
    DocTableCell,
    Slide,
    SlideTableCell,
}

/**
 * 基础文字样式。
 *
 * fontSize:
 * - Note / Doc：sp
 * - Slide：设计画布坐标
 *
 * lineHeightRatio:
 * - 相对于基础字号的行高倍率
 */
data class BlockTypographyStyle(
    val fontSize: Double,
    val lineHeightRatio: Double,
) {
    val lineHeight: Double
        get() = fontSize * lineHeightRatio
}

/**
 * BlockContent 公共排版标准。
 *
 * 此处只定义样式基准，
 * 不参与内容存储、编辑和同步。
 */
object BlockTypography {

    const val BASE_SLIDE_CANVAS_WIDTH = 1920.0

    // --------------------------------------------------
    // Note
    // 保持目前 Android Note 阅读样式
    // --------------------------------------------------

    private val noteStyles = mapOf(
        "title" to BlockTypographyStyle(25.0, 36.0 / 25.0),
        "h1" to BlockTypographyStyle(23.0, 33.0 / 23.0),
        "h2" to BlockTypographyStyle(20.0, 30.0 / 20.0),
        "h3" to BlockTypographyStyle(18.0, 28.0 / 18.0),

        "paragraph" to BlockTypographyStyle(18.0, 24.0 / 18.0),

        "small" to BlockTypographyStyle(12.0, 19.0 / 12.0),
        "caption" to BlockTypographyStyle(11.0, 18.0 / 11.0),
    )

    // --------------------------------------------------
    // Doc
    // 后续核实现有 Renderer 后补充
    // --------------------------------------------------

    // --------------------------------------------------
    // Doc Table Cell
    // 后续核实现有 Renderer 后补充
    // --------------------------------------------------

    // --------------------------------------------------
    // Slide
    // 与 Web DEFAULT_TYPOGRAPHY 对齐
    // 基准画布：1920 × 1080
    // --------------------------------------------------

    private val slideStyles = mapOf(
        "title" to BlockTypographyStyle(72.0, 1.10),
        "h1" to BlockTypographyStyle(54.0, 1.20),
        "h2" to BlockTypographyStyle(42.0, 1.25),
        "h3" to BlockTypographyStyle(33.0, 1.35),

        "paragraph" to BlockTypographyStyle(27.0, 1.50),

        "small" to BlockTypographyStyle(21.0, 1.40),
        "caption" to BlockTypographyStyle(18.0, 1.40),
    )

    // --------------------------------------------------
    // Slide Table Cell
    // 后续独立定义
    // --------------------------------------------------

    /**
     * 获取指定场景的基础文字样式。
     */
    fun baseStyle(
        profile: BlockTypographyProfile,
        variant: String?,
    ): BlockTypographyStyle {

        val styles = when (profile) {

            BlockTypographyProfile.Note ->
                noteStyles

            BlockTypographyProfile.Slide ->
                slideStyles

            // 暂未建立独立标准，不应提前接入。
            else -> error(
                "Typography profile not configured: $profile"
            )
        }

        val key = variant
            ?.trim()
            ?.lowercase()

        return styles[key]
            ?: styles.getValue("paragraph")
    }

    /**
     * Slide 设计画布的 Typography Scale。
     *
     * 与 Web canvasTypoScale 一致。
     */
    fun canvasTypoScale(
        canvasWidth: Double,
    ): Double {

        val width = canvasWidth
            .takeIf { it.isFinite() && it > 0 }
            ?: BASE_SLIDE_CANVAS_WIDTH

        return (width / BASE_SLIDE_CANVAS_WIDTH)
            .coerceIn(0.5, 2.5)
    }

    /**
     * 根据场景获取最终的基础样式。
     *
     * Slide 需要根据设计画布调整字号。
     * Note / Doc 使用各自原始排版基准。
     */
    fun resolve(
        profile: BlockTypographyProfile,
        variant: String?,
        canvasWidth: Double = BASE_SLIDE_CANVAS_WIDTH,
    ): BlockTypographyStyle {

        val base = baseStyle(
            profile = profile,
            variant = variant,
        )

        return when (profile) {

            BlockTypographyProfile.Slide,
            BlockTypographyProfile.SlideTableCell -> {

                val fontSize = (
                        base.fontSize *
                                canvasTypoScale(canvasWidth)
                        )
                    .roundToInt()
                    .coerceAtLeast(8)
                    .toDouble()

                base.copy(fontSize = fontSize)
            }

            else -> base
        }
    }
}

/**
 * 当前 Slide 的原始设计画布宽度。
 *
 * 与实际屏幕显示宽度无关。
 */
val LocalSlideCanvasWidth = staticCompositionLocalOf {
    BlockTypography.BASE_SLIDE_CANVAS_WIDTH
}