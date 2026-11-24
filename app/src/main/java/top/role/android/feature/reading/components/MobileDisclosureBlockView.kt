package top.role.android.feature.reading.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import top.role.android.core.model.chart.MobileChartRuntime
import top.role.android.core.model.content.MobileDisclosureBlock

@Composable
fun MobileDisclosureBlockView(
    block: MobileDisclosureBlock,
    charts: Map<String, MobileChartRuntime>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable(
        block.id,
        block.defaultOpen,
    ) {
        mutableStateOf(
            block.defaultOpen == true
        )
    }

    val shape =
        RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                MaterialTheme.colorScheme
                    .surfaceContainerLow
            ),
    ) {

        /*
         * Disclosure 标题及展开入口。
         */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    role = Role.Button,
                    onClickLabel =
                        if (expanded) {
                            "收起内容"
                        } else {
                            "展开内容"
                        },
                ) {
                    expanded = !expanded
                }
                .padding(
                    horizontal = 12.dp,
                    vertical = 12.dp,
                ),

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
        ) {

            Icon(
                imageVector =
                    if (expanded) {
                        Icons.Default.KeyboardArrowDown
                    } else {
                        Icons.Default.KeyboardArrowRight
                    },

                contentDescription = null,

                modifier = Modifier.size(20.dp),

                tint =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
            )

            val titleStyle =
                readingParagraphStyle().copy(
                    fontWeight = FontWeight.SemiBold,
                )

            if (
                block.title.inlines.isEmpty() ||
                block.title.inlines.all {
                    it.text.isBlank()
                }
            ) {
                Text(
                    text = "折叠内容",
                    modifier = Modifier.weight(1f),
                    style = titleStyle,
                )
            } else {
                MobileInlineText(
                    inlines = block.title.inlines,

                    modifier = Modifier.weight(1f),

                    baseStyle = titleStyle,
                )
            }
        }

        /*
         * 展开后的正文。
         *
         * 直接复用统一 Block Renderer。
         */
        if (expanded && block.children.isNotEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 14.dp,
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        ReadingDimens.BlockSpacing
                    ),
            ) {

                block.children.forEach { child ->

                    key(child.id) {

                        MobileRenderBlockView(
                            block = child,
                            charts = charts,
                        )
                    }
                }
            }
        }
    }
}