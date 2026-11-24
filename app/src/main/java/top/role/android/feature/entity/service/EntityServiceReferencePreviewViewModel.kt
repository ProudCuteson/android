package top.role.android.feature.entity.service

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

import top.role.android.core.data.image.ImageRepository

data class EntityServiceReferenceMediaUi(
    val imageId: String,
    val order: Int,
    val displayUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class EntityServiceReferencePreviewUiState(
    val isLoading: Boolean = true,
    val content: JsonObject? = null,
    val media: List<EntityServiceReferenceMediaUi> = emptyList(),
    val errorMessage: String? = null,

    // PDF Delivery
    val isPdfLoading: Boolean = false,
    val pdfDelivery: AssetDeliveryResult? = null,
    val pdfErrorMessage: String? = null,
)

class EntityServiceReferencePreviewViewModel(
    private val serviceId: String,
    private val targetType: EntityServiceReferenceType,
    private val targetId: String,
    private val source: EntityServiceReferenceSource,
    private val repository: EntityServiceRepository,
    private val imageRepository: ImageRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        EntityServiceReferencePreviewUiState()
    )

    val uiState: StateFlow<EntityServiceReferencePreviewUiState> =
        _uiState.asStateFlow()

    private var observeJob: Job? = null

    // MARK: - Media Preview

    private val mediaPreviewUrls = mutableMapOf<String, String>()

    private val mediaPreviewErrors = mutableMapOf<String, String>()

    private val resolvingMediaPreviewIds = mutableSetOf<String>()

    private var currentMedia =
        emptyList<EntityServiceReferenceMediaUi>()

    // MARK: - PDF Delivery

    /**
     * 使用原始 assetId 作为稳定缓存键。
     *
     * 缓存仅存在于当前 ViewModel 生命周期内。
     * 不将临时签名 URL 作为资源身份。
     */
    private val pdfDeliveries =
        mutableMapOf<String, AssetDeliveryResult>()

    private val pdfDeliveryErrors =
        mutableMapOf<String, String>()

    private val resolvingPdfAssetIds =
        mutableSetOf<String>()

    fun start() {
        if (observeJob?.isActive == true) return

        observeContent()
    }

    fun stop() {
        observeJob?.cancel()
        observeJob = null
    }

    fun retry() {
        stop()

        mediaPreviewErrors.clear()

        val assetId = currentPdfAssetId()
        if (assetId != null) {
            pdfDeliveries.remove(assetId)
            pdfDeliveryErrors.remove(assetId)
        }

        observeContent()
    }

    private fun observeContent() {
        currentMedia = emptyList()

        _uiState.update {
            it.copy(
                isLoading = true,
                content = null,
                media = emptyList(),
                errorMessage = null,
                isPdfLoading = false,
                pdfDelivery = null,
                pdfErrorMessage = null,
            )
        }

        observeJob = viewModelScope.launch {
            try {
                repository.observeReferenceContent(
                    serviceId = serviceId,
                    targetType = targetType,
                    targetId = targetId,
                    source = source,
                ).collect { result ->

                    result.fold(
                        onSuccess = { response ->

                            if (
                                response.ok &&
                                response.content != null
                            ) {
                                applyResponse(
                                    content = response.content,
                                )
                            } else {
                                currentMedia = emptyList()

                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        content = null,
                                        media = emptyList(),
                                        errorMessage =
                                            referencePreviewErrorMessage(
                                                response.code
                                            ),
                                        isPdfLoading = false,
                                        pdfDelivery = null,
                                        pdfErrorMessage = null,
                                    )
                                }
                            }
                        },

                        onFailure = { error ->

                            if (error is CancellationException) {
                                throw error
                            }

                            currentMedia = emptyList()

                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    content = null,
                                    media = emptyList(),
                                    errorMessage = error.message ?: "关联内容加载失败",
                                    isPdfLoading = false,
                                    pdfDelivery = null,
                                    pdfErrorMessage = null,
                                )
                            }
                        },
                    )
                }

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                currentMedia = emptyList()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        content = null,
                        media = emptyList(),
                        errorMessage =
                            error.message ?: "关联内容加载失败",
                        isPdfLoading = false,
                        pdfDelivery = null,
                        pdfErrorMessage = null,
                    )
                }
            }
        }
    }

    // MARK: - Response

    private fun applyResponse(
        content: JsonObject,
    ) {
        currentMedia =
            if (
                targetType ==
                EntityServiceReferenceType.NOTE
            ) {
                parseNoteMedia(content)
            } else {
                emptyList()
            }

        val assetId =
            if (
                targetType ==
                EntityServiceReferenceType.FILE_ASSET
            ) {
                content.stringValue("id")
                    ?.takeIf { it.isNotBlank() }
            } else {
                null
            }

        val cachedDelivery =
            assetId?.let { pdfDeliveries[it] }

        _uiState.update {
            it.copy(
                isLoading = false,
                content = content,
                media = emptyList(),
                errorMessage = null,

                isPdfLoading =
                    assetId != null &&
                            cachedDelivery == null &&
                            assetId !in pdfDeliveryErrors,

                pdfDelivery = cachedDelivery,
                pdfErrorMessage =
                    assetId?.let { pdfDeliveryErrors[it] },
            )
        }

        refreshMediaUiState()
        resolveMissingMediaPreviews()

        if (assetId != null && cachedDelivery == null) {
            resolvePdfDelivery(assetId)
        }
    }

    // MARK: - Parse Note Media

    private fun parseNoteMedia(
        content: JsonObject,
    ): List<EntityServiceReferenceMediaUi> {

        val mediaArray =
            content["media"] as? JsonArray
                ?: return emptyList()

        return mediaArray
            .mapIndexedNotNull { index, element ->

                val item =
                    element as? JsonObject
                        ?: return@mapIndexedNotNull null

                val type =
                    item.stringValue("type")
                        ?: return@mapIndexedNotNull null

                if (type != "image") {
                    return@mapIndexedNotNull null
                }

                val imageId =
                    item.stringValue("imageId")
                        ?.takeIf { it.isNotBlank() }
                        ?: return@mapIndexedNotNull null

                val order =
                    item.intValue("order") ?: index

                EntityServiceReferenceMediaUi(
                    imageId = imageId,
                    order = order,
                )
            }
            .distinctBy { it.imageId }
            .sortedBy { it.order }
    }

    // MARK: - Resolve Image Preview

    private fun resolveMissingMediaPreviews() {
        currentMedia
            .map { it.imageId }
            .distinct()
            .forEach { imageId ->
                resolveMediaPreview(imageId)
            }
    }

    private fun resolveMediaPreview(
        imageId: String,
    ) {
        if (
            imageId.isBlank() ||
            imageId in mediaPreviewUrls ||
            imageId in mediaPreviewErrors ||
            imageId in resolvingMediaPreviewIds
        ) {
            return
        }

        resolvingMediaPreviewIds += imageId

        refreshMediaUiState()

        viewModelScope.launch {
            try {
                val preview =
                    imageRepository.getPreviewUrl(
                        imageId = imageId,
                    )

                val url =
                    preview.url.takeIf {
                        it.isNotBlank()
                    }

                if (url != null) {
                    mediaPreviewUrls[imageId] = url
                } else {
                    mediaPreviewErrors[imageId] =
                        "图片预览地址不可用"
                }

            } catch (error: CancellationException) {
                throw error

            } catch (_: Exception) {
                mediaPreviewErrors[imageId] =
                    "图片暂时无法加载"

            } finally {
                resolvingMediaPreviewIds -= imageId

                refreshMediaUiState()
            }
        }
    }

    private fun refreshMediaUiState() {
        val mediaUiItems =
            currentMedia.map { item ->
                val url = mediaPreviewUrls[item.imageId]
                val error = mediaPreviewErrors[item.imageId]

                item.copy(
                    displayUrl = url,
                    isLoading = url == null && error == null,
                    errorMessage = error,
                )
            }

        _uiState.update {
            it.copy(media = mediaUiItems)
        }
    }

    fun retryMediaPreview(
        imageId: String,
    ) {
        if (
            currentMedia.none {
                it.imageId == imageId
            }
        ) {
            return
        }

        mediaPreviewErrors.remove(imageId)

        resolveMediaPreview(imageId)
    }

    // MARK: - PDF Delivery

    private fun resolvePdfDelivery(
        assetId: String,
    ) {
        if (
            assetId.isBlank() ||
            assetId in pdfDeliveries ||
            assetId in pdfDeliveryErrors ||
            assetId in resolvingPdfAssetIds
        ) {
            return
        }

        resolvingPdfAssetIds += assetId

        _uiState.update {
            it.copy(
                isPdfLoading = true,
                pdfDelivery = null,
                pdfErrorMessage = null,
            )
        }

        viewModelScope.launch {
            try {
                val delivery =
                    repository.getReferenceAssetDelivery(
                        serviceId = serviceId,
                        assetId = assetId,
                        source = source,
                    )

                // 防止后端返回其他资源的 Delivery。
                require(delivery.assetId == assetId) {
                    "PDF 资源 ID 不匹配"
                }

                require(delivery.url.isNotBlank()) {
                    "PDF 下载地址不可用"
                }

                pdfDeliveries[assetId] = delivery
                pdfDeliveryErrors.remove(assetId)

                // 防止旧请求覆盖当前页面。
                if (currentPdfAssetId() == assetId) {
                    _uiState.update {
                        it.copy(
                            isPdfLoading = false,
                            pdfDelivery = delivery,
                            pdfErrorMessage = null,
                        )
                    }
                }

            } catch (error: CancellationException) {
                throw error

            } catch (error: Exception) {
                val message =
                    error.message ?: "PDF 访问地址获取失败"

                pdfDeliveryErrors[assetId] = message

                if (currentPdfAssetId() == assetId) {
                    _uiState.update {
                        it.copy(
                            isPdfLoading = false,
                            pdfDelivery = null,
                            pdfErrorMessage = message,
                        )
                    }
                }

            } finally {
                resolvingPdfAssetIds -= assetId
            }
        }
    }

    fun retryPdfDelivery(
        assetId: String,
    ) {
        if (currentPdfAssetId() != assetId) {
            return
        }

        pdfDeliveries.remove(assetId)
        pdfDeliveryErrors.remove(assetId)

        resolvePdfDelivery(assetId)
    }

    private fun currentPdfAssetId(): String? {
        return _uiState.value.content
            ?.stringValue("id")
            ?.takeIf {
                targetType ==
                        EntityServiceReferenceType.FILE_ASSET &&
                        it.isNotBlank()
            }
    }

    override fun onCleared() {
        observeJob?.cancel()
        super.onCleared()
    }
}

// MARK: - JSON Helpers

private fun JsonObject.stringValue(
    key: String,
): String? {
    return (this[key] as? JsonPrimitive)
        ?.contentOrNull
}

private fun JsonObject.intValue(
    key: String,
): Int? {
    return (this[key] as? JsonPrimitive)
        ?.intOrNull
}

private fun referencePreviewErrorMessage(
    code: String?,
): String {
    return when (code) {
        "SPACE_SERVICE_NOT_FOUND" ->
            "服务请求不存在或已不可用"

        else ->
            "暂时无法访问这条关联内容，请稍后重试"
    }
}

// MARK: - Factory

class EntityServiceReferencePreviewViewModelFactory(
    private val serviceId: String,
    private val targetType: EntityServiceReferenceType,
    private val targetId: String,
    private val source: EntityServiceReferenceSource,
    private val repository: EntityServiceRepository,
    private val imageRepository: ImageRepository,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
    ): T {

        if (
            modelClass.isAssignableFrom(
                EntityServiceReferencePreviewViewModel::class.java
            )
        ) {
            return EntityServiceReferencePreviewViewModel(
                serviceId = serviceId,
                targetType = targetType,
                targetId = targetId,
                source = source,
                repository = repository,
                imageRepository = imageRepository,
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
