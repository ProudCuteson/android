package top.role.android.feature.entity.service
import kotlinx.coroutines.flow.Flow
import top.role.android.core.convex.ConvexMobileClient

class EntityServiceRepository(
    private val convexClient: ConvexMobileClient,
) {

    /**
     * 会员侧：当前空间我的服务请求
     */
    fun observeMyServices(
        spaceId: String,
        status: String? = null,
    ): Flow<Result<EntityServiceListResponse>> {

        val args = buildMap<String, Any> {
            put("spaceId", spaceId)

            status?.let {
                put("status", it)
            }
        }

        return convexClient.subscribe(
            name = "space/service:listMySpaceServicesBySpace",
            args = args,
        )
    }

    /**
     * 管理侧：当前空间收到的服务请求
     */
    fun observeManagedServices(
        spaceId: String,
        status: String? = null,
    ): Flow<Result<EntityServiceListResponse>> {

        val args = buildMap<String, Any> {
            put("spaceId", spaceId)

            status?.let {
                put("status", it)
            }
        }

        return convexClient.subscribe(
            name = "space/service:listManagedSpaceServices",
            args = args,
        )
    }

    /**
     * 服务请求详情
     *
     * 会员和管理者共用。
     */
    fun observeServiceDetail(
        serviceId: String,
    ): Flow<Result<EntityServiceDetailResponse>> {

        return convexClient.subscribe(
            name = "space/service:getSpaceServiceDetail",
            args = mapOf(
                "serviceId" to serviceId,
            ),
        )
    }

    /**
     * 会员侧：发起服务请求
     */

    suspend fun submitService(
        spaceId: String,
        requestType: String,
        title: String,
        contextSummary: String,
        coreQuestion: String,
        expectedHelp: String,
        references: List<EntityServiceReference> = emptyList(),
    ): EntityServiceActionResponse {

        val args = buildMap<String, Any> {
            put("spaceId", spaceId)
            put("requestType", requestType)
            put("title", title)
            put("contextSummary", contextSummary)
            put("coreQuestion", coreQuestion)
            put("expectedHelp", expectedHelp)

            if (references.isNotEmpty()) {
                put(
                    "references",
                    references.map { it.toConvexArg() },
                )
            }
        }

        return convexClient.mutation(
            name = "space/service:submitSpaceService",
            args = args,
        )
    }

    /**
     * 会员侧：撤回请求
     */

    suspend fun withdrawService(
        serviceId: String,
    ): EntityServiceActionResponse {
        return convexClient.mutation(
            name = "space/service:withdrawSpaceService",
            args = mapOf(
                "serviceId" to serviceId,
            ),
        )
    }


    /**
     * 管理端：标记处理中
     */
    suspend fun markInReview(
        serviceId: String,
    ): EntityServiceActionResponse {
        return convexClient.mutation(
            name = "space/service:markSpaceServiceInReview",
            args = mapOf(
                "serviceId" to serviceId,
            ),
        )
    }

    /**
     * 管理端：接受请求
     */
    suspend fun acceptService(
        serviceId: String,
    ): EntityServiceActionResponse {
        return convexClient.mutation(
            name = "space/service:acceptSpaceService",
            args = mapOf(
                "serviceId" to serviceId,
            ),
        )
    }

    /**
     * 管理端：拒绝请求
     */
    suspend fun declineService(
        serviceId: String,
        responseNote: String,
    ): EntityServiceActionResponse {
        return convexClient.mutation(
            name = "space/service:declineSpaceService",
            args = mapOf(
                "serviceId" to serviceId,
                "responseNote" to responseNote.trim(),
            ),
        )
    }

    /**
     * 管理端：关闭请求
     */
    suspend fun closeService(
        serviceId: String,
        resultRef: EntityServiceReference? = null,

        // 兼容旧流程，暂时保留
        responseNote: String = "",
        resultLink: String = "",
        resultLinkLabel: String = "",
    ): EntityServiceActionResponse {

        val args = buildMap<String, Any> {
            put("serviceId", serviceId)

            resultRef?.let {
                put("resultRef", it.toConvexArg())
            }

            responseNote.trim()
                .takeIf { it.isNotEmpty() }
                ?.let { note -> put("responseNote", note) }

            resultLink.trim()
                .takeIf { it.isNotEmpty() }
                ?.let { put("resultLink", it) }

            resultLinkLabel.trim()
                .takeIf { it.isNotEmpty() }
                ?.let { put("resultLinkLabel", it) }
        }

        return convexClient.mutation(
            name = "space/service:closeSpaceService",
            args = args,
        )
    }


    /**
     * 读取 Space Service 关联内容。
     *
     * 后端负责验证：
     * - 当前用户是否有权访问服务请求
     * - 内容是否关联到当前请求
     * - 内容所有者及内容状态
     */
    fun observeReferenceContent(
        serviceId: String,
        targetType: EntityServiceReferenceType,
        targetId: String,
        source: EntityServiceReferenceSource,
    ): Flow<Result<EntityServiceReferenceContentResponse>> {

        return convexClient.subscribe(
            name = "space/service:getSpaceServiceReferenceContent",
            args = mapOf(
                "serviceId" to serviceId,
                "targetType" to targetType.value,
                "targetId" to targetId,
                "source" to source.value,
            ),
        )
    }

}
