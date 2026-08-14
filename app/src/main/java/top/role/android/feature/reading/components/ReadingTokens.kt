package top.role.android.feature.reading.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme

object ReadingDimens {
    val BlockSpacing = 14.dp

    val ParagraphTopSpacing = 0.dp

    val Heading1TopSpacing = 18.dp
    val Heading2TopSpacing = 16.dp
    val Heading3TopSpacing = 14.dp
    val HeadingSmallTopSpacing = 12.dp

    val QuotePaddingHorizontal = 14.dp
    val QuotePaddingVertical = 12.dp

    val ListItemSpacing = 8.dp
}

@Composable
fun readingParagraphStyle(): TextStyle {
    return MaterialTheme.typography.bodyLarge.copy(
        fontSize = 17.sp,
        lineHeight = 30.sp,
        color =
            MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun readingHeadingStyle(
    level: Int,
): TextStyle {
    return when (level) {
        1 -> {
            MaterialTheme.typography.headlineMedium.copy(
                fontSize = 25.sp,
                lineHeight = 34.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        2 -> {
            MaterialTheme.typography.headlineSmall.copy(
                fontSize = 22.sp,
                lineHeight = 31.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        3 -> {
            MaterialTheme.typography.titleLarge.copy(
                fontSize = 20.sp,
                lineHeight = 29.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        else -> {
            MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun readingCaptionStyle(): TextStyle {
    return MaterialTheme.typography.bodySmall.copy(
        fontSize = 13.sp,
        lineHeight = 21.sp,
        color =
            MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun readingQuoteStyle(): TextStyle {
    return MaterialTheme.typography.bodyMedium.copy(
        fontSize = 16.sp,
        lineHeight = 28.sp,
        color =
            MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun readingListStyle(): TextStyle {
    return MaterialTheme.typography.bodyLarge.copy(
        fontSize = 17.sp,
        lineHeight = 30.sp,
        color =
            MaterialTheme.colorScheme.onSurface,
    )
}