package top.role.android.core.asset

import android.net.Uri
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 公共文件资产上传服务。
 *
 * 负责：
 * - 解析本地文件
 * - 请求上传授权
 * - OSS PUT
 * - 完成资产登记
 *
 * 不直接持有具体界面的上传状态。
 */
class AssetUploadService(
    private val repository: AssetUploadRepository,
    private val fileResolver: AssetUploadFileResolver,
    client: OkHttpClient,
) {

    /**
     * 文件上传使用独立的超时配置，
     * 但复用公共 OkHttpClient 的基础设施。
     */
    private val uploadClient = client.newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.MINUTES)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()

    /**
     * 为具体调用场景创建独立上传会话。
     */
    fun createSession(): AssetUploadSession {
        return AssetUploadSession(
            service = this,
        )
    }

    internal suspend fun resolveFile(
        uri: Uri,
        options: AssetUploadOptions,
    ): ResolvedAssetUploadFile {
        return fileResolver.resolve(
            uri = uri,
            options = options,
        )
    }

    internal suspend fun prepare(
        file: ResolvedAssetUploadFile,
        options: AssetUploadOptions,
    ): PreparedAssetUpload {
        return repository.prepare(
            fileName = file.fileName,
            mime = file.mime,
            bytes = file.bytes,
            options = options,
        )
    }

    /**
     * 按照服务端返回的上传指令，
     * 将实际文件直接上传至 OSS。
     */
    internal suspend fun putFile(
        prepared: PreparedAssetUpload,
        file: ResolvedAssetUploadFile,
    ) {

        val instruction = prepared.upload

        require(prepared.provider == "aliyun-oss") {
            "Unsupported asset provider"
        }

        require(instruction.mode == "put") {
            "Unsupported upload mode"
        }

        require(instruction.method == "PUT") {
            "Unsupported upload method"
        }

        val mime = instruction.headers
            .entries
            .firstOrNull {
                it.key.equals(
                    "Content-Type",
                    ignoreCase = true,
                )
            }
            ?.value
            ?: error("Missing upload Content-Type")

        /**
         * 使用文件作为 RequestBody。
         *
         * OkHttp 会流式读取文件，
         * 不需要将整个文件加载到内存。
         */
        val body = file.file.asRequestBody(
            mime.toMediaType(),
        )

        val requestBuilder = Request.Builder()
            .url(instruction.uploadUrl)
            .put(body)

        /**
         * 必须保留服务端签名要求的 Headers。
         */
        instruction.headers.forEach { (name, value) ->
            requestBuilder.header(
                name,
                value,
            )
        }

        val request = requestBuilder.build()

        executeUpload(request)
    }

    internal suspend fun finalizeUpload(
        pending: PendingAssetFinalize,
    ): AssetUploadResult {
        return repository.finalize(pending)
    }

    /**
     * 将 OkHttp 异步请求转换为可取消的挂起函数。
     */
    private suspend fun executeUpload(
        request: Request,
    ): Unit = suspendCancellableCoroutine { continuation ->

        val call = uploadClient.newCall(request)

        /**
         * 调用方协程取消时，
         * 同时取消实际的 HTTP 请求。
         */
        continuation.invokeOnCancellation {
            call.cancel()
        }

        call.enqueue(
            object : Callback {

                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    response.use {

                        if (!response.isSuccessful) {

                            continuation.resumeWithException(
                                IOException(
                                    "OSS 上传失败 (${response.code})"
                                )
                            )

                            return
                        }

                        continuation.resume(Unit)
                    }
                }
            }
        )
    }
}

/**
 * 单个调用场景中的文件上传会话。
 *
 * 对应 Web useAssetUpload 中的：
 * - status
 * - error
 * - result
 * - pendingFinalizeRef
 * - retryFinalize
 */
class AssetUploadSession internal constructor(
    private val service: AssetUploadService,
) {

    private val operationMutex = Mutex()

    private val _state = MutableStateFlow(
        AssetUploadState()
    )

    val state: StateFlow<AssetUploadState> =
        _state.asStateFlow()

    /**
     * OSS PUT 成功后保存确认参数。
     *
     * 不保存文件内容，
     * 因此 Finalize 失败后也可以删除临时文件。
     */
    private var pendingFinalize:
            PendingAssetFinalize? = null

    /**
     * 完整上传流程。
     *
     * Resolve → Prepare → PUT → Finalize
     */
    suspend fun upload(
        uri: Uri,
        options: AssetUploadOptions =
            AssetUploadOptions(),
    ): AssetUploadResult {

        if (!operationMutex.tryLock()) {
            error("已有文件正在上传")
        }

        var resolvedFile:
                ResolvedAssetUploadFile? = null

        var stage = AssetUploadStatus.Preparing

        try {

            pendingFinalize = null

            _state.value = AssetUploadState(
                status = stage,
            )

            /**
             * 1. 解析文件。
             */
            val file = service.resolveFile(
                uri = uri,
                options = options,
            )

            resolvedFile = file

            /**
             * 2. 获取上传授权。
             */
            val prepared = service.prepare(
                file = file,
                options = options,
            )

            /**
             * 3. 上传 OSS。
             */
            stage = AssetUploadStatus.Uploading

            _state.value = AssetUploadState(
                status = stage,
            )

            service.putFile(
                prepared = prepared,
                file = file,
            )

            /**
             * OSS 已经上传成功。
             *
             * 保留 Finalize 参数。
             */
            val pending = PendingAssetFinalize(
                prepared = prepared,
                bytes = file.bytes,
            )

            pendingFinalize = pending

            /**
             * 4. 完成资产登记。
             */
            stage = AssetUploadStatus.Finalizing

            _state.value = AssetUploadState(
                status = stage,
            )

            val result = service.finalizeUpload(
                pending = pending,
            )

            pendingFinalize = null

            _state.value = AssetUploadState(
                status = AssetUploadStatus.Done,
                result = result,
            )

            return result

        } catch (cancel: CancellationException) {

            handleCancellation(stage)

            throw cancel

        } catch (error: Exception) {

            _state.value = AssetUploadState(
                status = AssetUploadStatus.Error,

                error = error.message
                    ?: "文件上传失败",

                failedStage = stage,

                canRetryFinalize =
                    pendingFinalize != null,
            )

            throw error

        } finally {

            /**
             * 无论成功、失败还是取消，
             * 都清理本地临时上传文件。
             */
            resolvedFile?.cleanup()

            operationMutex.unlock()
        }
    }

    /**
     * 单独重试 Finalize。
     *
     * 不重新解析文件、
     * 不重新 Prepare、
     * 不重新上传 OSS。
     */
    suspend fun retryFinalize(): AssetUploadResult {

        if (!operationMutex.tryLock()) {
            error("已有文件正在处理")
        }

        try {

            val pending = pendingFinalize
                ?: error("没有待确认的上传文件")

            _state.value = AssetUploadState(
                status = AssetUploadStatus.Finalizing,
            )

            val result = service.finalizeUpload(
                pending = pending,
            )

            pendingFinalize = null

            _state.value = AssetUploadState(
                status = AssetUploadStatus.Done,
                result = result,
            )

            return result

        } catch (cancel: CancellationException) {

            handleCancellation(
                AssetUploadStatus.Finalizing,
            )

            throw cancel

        } catch (error: Exception) {

            _state.value = AssetUploadState(
                status = AssetUploadStatus.Error,

                error = error.message
                    ?: "文件确认失败",

                failedStage =
                    AssetUploadStatus.Finalizing,

                canRetryFinalize =
                    pendingFinalize != null,
            )

            throw error

        } finally {

            operationMutex.unlock()
        }
    }

    /**
     * 清除当前会话状态。
     *
     * 不删除 OSS 对象，
     * 不删除已经登记的资产。
     */
    fun reset() {

        if (!operationMutex.tryLock()) {
            return
        }

        try {

            pendingFinalize = null

            _state.value = AssetUploadState()

        } finally {

            operationMutex.unlock()
        }
    }

    private fun handleCancellation(
        stage: AssetUploadStatus,
    ) {

        _state.value =
            if (pendingFinalize != null) {

                AssetUploadState(
                    status = AssetUploadStatus.Error,

                    error = "文件已上传，尚未完成确认",

                    failedStage = stage,

                    canRetryFinalize = true,
                )

            } else {

                AssetUploadState()
            }
    }
}