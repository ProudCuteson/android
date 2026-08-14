package top.role.android.feature.reading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.role.android.feature.reading.components.MobileRenderBlockView
import top.role.android.feature.reading.components.ReadingDimens
import top.role.android.feature.reading.components.ReadingHeader

@Composable
fun DocDetailScreen(
    state: DocDetailUiState,
    onSpaceClick: (() -> Unit)? = null,
) {
    when {
        state.isLoading -> {
            Box(
                modifier =
                    Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text =
                        state.errorMessage,
                )
            }
        }

        state.detail?.doc == null -> {
            Box(
                modifier =
                    Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center,
            ) {
                Text(
                    text =
                        "文档不存在或不可访问",
                )
            }
        }

        else -> {
            val detail =
                state.detail

            val doc =
                detail.doc!!

            val blocks =
                detail.renderDoc
                    ?.blocks
                    .orEmpty()

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),

                contentPadding =
                    PaddingValues(
                        start = 18.dp,
                        end = 18.dp,
                        top = 18.dp,
                        bottom = 56.dp,
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        ReadingDimens
                            .BlockSpacing
                    ),
            ) {
                item(
                    key = "doc-header",
                ) {
                    ReadingHeader(
                        doc = doc,
                        author =
                            detail.author,
                        space =
                            detail.space,
                        modifier =
                            Modifier.padding(
                                bottom = 10.dp,
                            ),
                        onSpaceClick =
                            onSpaceClick,
                    )
                }

                itemsIndexed(
                    items = blocks,
                    key = { index, _ ->
                        "doc-block-$index"
                    },
                ) { _, block ->
                    MobileRenderBlockView(
                        block = block,
                        modifier =
                            Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}