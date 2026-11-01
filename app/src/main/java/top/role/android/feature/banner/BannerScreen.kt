package top.role.android.feature.banner

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun BannerScreen(
    repository: BannerRepository,
    onClick: (String) -> Unit,
) {
    val factory = remember(repository) {
        BannerViewModelFactory(repository)
    }

    val viewModel: BannerViewModel = viewModel(
        factory = factory,
    )

    LaunchedEffect(viewModel) {
        viewModel.start()
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val items = state.items

    if (state.isLoading || items.isEmpty()) return

    var index by remember(items) {
        mutableIntStateOf(0)
    }

    LaunchedEffect(items) {
        if (items.size > 1) {
            while (true) {
                delay(6000)
                index = (index + 1) % items.size
            }
        }
    }

    val current = items[index.coerceIn(items.indices)]
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape,
            )
            .padding(18.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = current.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            if (items.size > 1) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            index = (index - 1 + items.size) % items.size
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "上一条",
                        )
                    }

                    IconButton(
                        onClick = {
                            index = (index + 1) % items.size
                        },
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "下一条",
                        )
                    }
                }
            }
        }

        Text(
            text = current.text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
        )

        if (!current.author.isNullOrBlank()) {
            Text(
                text = current.author,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (!current.ctaHref.isNullOrBlank()) {
            Text(
                text = current.ctaLabel ?: "了解更多 →",
                modifier = Modifier
                    .padding(top = 12.dp)
                    .clickable {
                        onClick(current.ctaHref)
                    },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
