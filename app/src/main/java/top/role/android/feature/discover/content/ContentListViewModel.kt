package top.role.android.feature.discover.content

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import top.role.android.core.model.content.ContentListMode
import top.role.android.core.model.content.MobileContentCard
import top.role.android.core.model.content.MobileContentType

data class ContentPageState(
    val items: List<MobileContentCard> =
        emptyList(),

    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,

    val isDone: Boolean = false,
    val continueCursor: String? = null,

    val errorMessage: String? = null,
)

data class ContentListUiState(
    val isLoading: Boolean = true,

    val slideFeatured:
    ContentPageState =
        ContentPageState(),

    val slideLatest:
    ContentPageState =
        ContentPageState(),

    val docFeatured:
    ContentPageState =
        ContentPageState(),

    val docLatest:
    ContentPageState =
        ContentPageState(),
) {

    val slides: List<MobileContentCard>
        get() =
            mergeFeaturedAndLatest(
                featured =
                    slideFeatured.items,
                latest =
                    slideLatest.items,
            )

    val docs: List<MobileContentCard>
        get() =
            mergeFeaturedAndLatest(
                featured =
                    docFeatured.items,
                latest =
                    docLatest.items,
            )

    val slidesDone: Boolean
        get() =
            slideFeatured.isDone &&
                    slideLatest.isDone

    val docsDone: Boolean
        get() =
            docFeatured.isDone &&
                    docLatest.isDone

    val isLoadingSlides: Boolean
        get() =
            slideFeatured.isLoading ||
                    slideLatest.isLoading

    val isLoadingDocs: Boolean
        get() =
            docFeatured.isLoading ||
                    docLatest.isLoading

    val isLoadingMoreSlides: Boolean
        get() =
            slideFeatured.isLoadingMore ||
                    slideLatest.isLoadingMore

    val isLoadingMoreDocs: Boolean
        get() =
            docFeatured.isLoadingMore ||
                    docLatest.isLoadingMore

    companion object {

        private fun mergeFeaturedAndLatest(
            featured:
            List<MobileContentCard>,

            latest:
            List<MobileContentCard>,
        ): List<MobileContentCard> {

            if (
                featured.isEmpty() &&
                latest.isEmpty()
            ) {
                return emptyList()
            }

            val result =
                LinkedHashMap<
                        String,
                        MobileContentCard
                        >()

            featured.forEach { item ->
                result[item.id] = item
            }

            latest.forEach { item ->
                if (
                    !result.containsKey(
                        item.id
                    )
                ) {
                    result[item.id] = item
                }
            }

            return result.values.toList()
        }
    }
}

class ContentListViewModel(
    private val repository:
    ContentListRepository,
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(
            ContentListUiState()
        )

    val uiState:
            StateFlow<ContentListUiState> =
        _uiState.asStateFlow()

    init {
        loadInitial()
    }

    fun retry() {
        loadInitial()
    }

    fun loadMore(
        contentType:
        MobileContentType,
    ) {
        when (contentType) {
            MobileContentType.Slide ->
                loadMoreSlide()

            MobileContentType.Doc ->
                loadMoreDoc()
        }
    }

    private fun loadInitial() {
        viewModelScope.launch {

            _uiState.value =
                ContentListUiState(
                    isLoading = true,

                    slideFeatured =
                        ContentPageState(
                            isLoading = true,
                        ),

                    slideLatest =
                        ContentPageState(
                            isLoading = true,
                        ),

                    docFeatured =
                        ContentPageState(
                            isLoading = true,
                        ),

                    docLatest =
                        ContentPageState(
                            isLoading = true,
                        ),
                )

            coroutineScope {
                listOf(
                    async {
                        loadInitialPage(
                            mode =
                                ContentListMode.Featured,

                            contentType =
                                MobileContentType.Slide,
                        )
                    },

                    async {
                        loadInitialPage(
                            mode =
                                ContentListMode.Latest,

                            contentType =
                                MobileContentType.Slide,
                        )
                    },

                    async {
                        loadInitialPage(
                            mode =
                                ContentListMode.Featured,

                            contentType =
                                MobileContentType.Doc,
                        )
                    },

                    async {
                        loadInitialPage(
                            mode =
                                ContentListMode.Latest,

                            contentType =
                                MobileContentType.Doc,
                        )
                    },
                ).awaitAll()
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                )
            }
        }
    }

    private suspend fun loadInitialPage(
        mode: ContentListMode,
        contentType: MobileContentType,
    ) {
        repository
            .loadPage(
                mode = mode,
                contentType =
                    contentType,
                cursor = null,
            )
            .onSuccess { result ->

                updatePage(
                    mode = mode,
                    contentType =
                        contentType,
                ) {
                    it.copy(
                        items =
                            result.page,

                        isLoading = false,
                        isLoadingMore = false,

                        isDone =
                            result.isDone,

                        continueCursor =
                            result
                                .continueCursor
                                .takeIf {
                                    !result.isDone
                                },

                        errorMessage = null,
                    )
                }
            }
            .onFailure { error ->
                error.printStackTrace()

                updatePage(
                    mode = mode,
                    contentType =
                        contentType,
                ) {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,

                        errorMessage =
                            error.message
                                ?: initialErrorMessage(
                                    mode =
                                        mode,
                                    contentType =
                                        contentType,
                                ),
                    )
                }
            }
    }

    private fun loadMoreSlide() {
        val state =
            _uiState.value

        when {
            canLoadMore(
                state.slideFeatured
            ) -> {
                loadMorePage(
                    mode =
                        ContentListMode.Featured,

                    contentType =
                        MobileContentType.Slide,
                )
            }

            canLoadMore(
                state.slideLatest
            ) -> {
                loadMorePage(
                    mode =
                        ContentListMode.Latest,

                    contentType =
                        MobileContentType.Slide,
                )
            }
        }
    }

    private fun loadMoreDoc() {
        val state =
            _uiState.value

        when {
            canLoadMore(
                state.docFeatured
            ) -> {
                loadMorePage(
                    mode =
                        ContentListMode.Featured,

                    contentType =
                        MobileContentType.Doc,
                )
            }

            canLoadMore(
                state.docLatest
            ) -> {
                loadMorePage(
                    mode =
                        ContentListMode.Latest,

                    contentType =
                        MobileContentType.Doc,
                )
            }
        }
    }

    private fun canLoadMore(
        page:
        ContentPageState,
    ): Boolean {

        return !page.isLoading &&
                !page.isLoadingMore &&
                !page.isDone &&
                page.continueCursor != null
    }

    private fun loadMorePage(
        mode: ContentListMode,
        contentType: MobileContentType,
    ) {
        val current =
            getPageState(
                state =
                    _uiState.value,
                mode =
                    mode,
                contentType =
                    contentType,
            )

        if (!canLoadMore(current)) {
            return
        }

        val cursor =
            current.continueCursor
                ?: return

        viewModelScope.launch {

            updatePage(
                mode = mode,
                contentType =
                    contentType,
            ) {
                it.copy(
                    isLoadingMore = true,
                    errorMessage = null,
                )
            }

            repository
                .loadPage(
                    mode = mode,
                    contentType =
                        contentType,
                    cursor = cursor,
                )
                .onSuccess { result ->

                    updatePage(
                        mode = mode,
                        contentType =
                            contentType,
                    ) { page ->

                        page.copy(
                            items =
                                mergeById(
                                    current =
                                        page.items,
                                    incoming =
                                        result.page,
                                ),

                            isLoadingMore = false,

                            isDone =
                                result.isDone,

                            continueCursor =
                                result
                                    .continueCursor
                                    .takeIf {
                                        !result.isDone
                                    },

                            errorMessage = null,
                        )
                    }
                }
                .onFailure { error ->
                    error.printStackTrace()

                    updatePage(
                        mode = mode,
                        contentType =
                            contentType,
                    ) {
                        it.copy(
                            isLoadingMore = false,

                            errorMessage =
                                error.message
                                    ?: "加载更多失败",
                        )
                    }
                }
        }
    }

    private fun getPageState(
        state:
        ContentListUiState,

        mode:
        ContentListMode,

        contentType:
        MobileContentType,
    ): ContentPageState {

        return when (contentType) {
            MobileContentType.Slide -> {
                when (mode) {
                    ContentListMode.Featured ->
                        state.slideFeatured

                    ContentListMode.Latest ->
                        state.slideLatest
                }
            }

            MobileContentType.Doc -> {
                when (mode) {
                    ContentListMode.Featured ->
                        state.docFeatured

                    ContentListMode.Latest ->
                        state.docLatest
                }
            }
        }
    }

    private fun updatePage(
        mode:
        ContentListMode,

        contentType:
        MobileContentType,

        transform:
            (ContentPageState) ->
        ContentPageState,
    ) {
        _uiState.update { state ->

            when (contentType) {
                MobileContentType.Slide -> {
                    when (mode) {
                        ContentListMode.Featured ->
                            state.copy(
                                slideFeatured =
                                    transform(
                                        state
                                            .slideFeatured
                                    )
                            )

                        ContentListMode.Latest ->
                            state.copy(
                                slideLatest =
                                    transform(
                                        state
                                            .slideLatest
                                    )
                            )
                    }
                }

                MobileContentType.Doc -> {
                    when (mode) {
                        ContentListMode.Featured ->
                            state.copy(
                                docFeatured =
                                    transform(
                                        state
                                            .docFeatured
                                    )
                            )

                        ContentListMode.Latest ->
                            state.copy(
                                docLatest =
                                    transform(
                                        state
                                            .docLatest
                                    )
                            )
                    }
                }
            }
        }
    }

    private fun initialErrorMessage(
        mode:
        ContentListMode,

        contentType:
        MobileContentType,
    ): String {

        val typeLabel =
            when (contentType) {
                MobileContentType.Slide ->
                    "演示文稿"

                MobileContentType.Doc ->
                    "文档"
            }

        val modeLabel =
            when (mode) {
                ContentListMode.Featured ->
                    "推荐"

                ContentListMode.Latest ->
                    "最新"
            }

        return "${modeLabel}${typeLabel}加载失败"
    }

    private fun mergeById(
        current:
        List<MobileContentCard>,

        incoming:
        List<MobileContentCard>,
    ): List<MobileContentCard> {

        if (current.isEmpty()) {
            return incoming
        }

        if (incoming.isEmpty()) {
            return current
        }

        val result =
            LinkedHashMap<
                    String,
                    MobileContentCard
                    >()

        current.forEach {
            result[it.id] = it
        }

        incoming.forEach {
            result[it.id] = it
        }

        return result.values.toList()
    }
}

class ContentListViewModelFactory(
    private val repository:
    ContentListRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass:
        Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                ContentListViewModel::class.java
            )
        ) {
            return ContentListViewModel(
                repository =
                    repository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}