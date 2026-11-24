package top.role.android.feature.main.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

import top.role.android.core.auth.AuthState
import top.role.android.app.navigation.ContentListRoute
import top.role.android.app.navigation.DocDetailRoute
import top.role.android.app.navigation.EntityDetailRoute
import top.role.android.app.navigation.MainDestination
import top.role.android.app.navigation.MembershipRoute
import top.role.android.app.navigation.SlideDetailRoute
import top.role.android.app.navigation.SpaceListRoute
import top.role.android.app.navigation.TopicDetailRoute
import top.role.android.app.navigation.TopicListRoute
import top.role.android.core.model.space.SpaceListMode
import top.role.android.feature.discover.DiscoverScreen
import top.role.android.feature.discover.DiscoverViewModel
import top.role.android.feature.discover.DiscoverViewModelFactory
import top.role.android.feature.discover.content.ContentListScreen
import top.role.android.feature.discover.content.ContentListViewModel
import top.role.android.feature.discover.content.ContentListViewModelFactory
import top.role.android.feature.discover.space.SpaceListScreen
import top.role.android.feature.discover.space.SpaceListViewModel
import top.role.android.feature.discover.space.SpaceListViewModelFactory
import top.role.android.feature.discover.topic.TopicListScreen
import top.role.android.feature.discover.topic.TopicListViewModel
import top.role.android.feature.discover.topic.TopicListViewModelFactory
import top.role.android.feature.entity.EntityDetailScreen
import top.role.android.feature.entity.EntityDetailViewModel
import top.role.android.feature.entity.EntityDetailViewModelFactory
import top.role.android.feature.reading.DocDetailScreen
import top.role.android.feature.reading.DocDetailViewModel
import top.role.android.feature.reading.DocDetailViewModelFactory
import top.role.android.feature.slide.SlideDetailScreen
import top.role.android.feature.slide.SlideDetailViewModel
import top.role.android.feature.slide.SlideDetailViewModelFactory
import top.role.android.feature.topic.TopicDetailScreen
import top.role.android.feature.topic.TopicDetailViewModel
import top.role.android.feature.topic.TopicDetailViewModelFactory
import top.role.android.feature.membership.MembershipScreen
import top.role.android.feature.membership.MembershipViewModel
import top.role.android.feature.membership.MembershipViewModelFactory
import top.role.android.app.navigation.EntityServiceRoute
import top.role.android.app.navigation.EntityServiceReferencePreviewRoute
import top.role.android.app.navigation.PdfReaderRoute
import top.role.android.feature.entity.service.EntityServiceReferencePreviewScreen
import top.role.android.feature.entity.service.EntityServiceReferencePreviewViewModel
import top.role.android.feature.entity.service.EntityServiceReferencePreviewViewModelFactory
import top.role.android.feature.entity.service.EntityServiceReferenceSource
import top.role.android.feature.entity.service.EntityServiceReferenceType
import top.role.android.feature.entity.service.EntityServiceScreen
import top.role.android.feature.entity.service.EntityServiceViewModel
import top.role.android.feature.entity.service.EntityServiceViewModelFactory

fun NavGraphBuilder.discoverNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
) {
    /*
     * Membership
     *
     * Space / Topic / Content 的所有会员入口，
     * 最终统一只传 spaceId。
     */
    fun openMembership(spaceId: String?) {
        val normalizedSpaceId = spaceId
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: return

        navController.navigate(
            MembershipRoute.create(
                spaceId = normalizedSpaceId,
            )
        )
    }

    /*
     * Discover Home
     */
    composable(route = MainDestination.Discover.route) {
        val discoverViewModel: DiscoverViewModel = viewModel(
            factory = DiscoverViewModelFactory(
                repository = dependencies.discoverRepository,
            )
        )

        LaunchedEffect(
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                discoverViewModel.start()
            } else {
                discoverViewModel.stop()
            }
        }

        DiscoverScreen(
            viewModel = discoverViewModel,
            authState = dependencies.authState,
            remoteOnline = dependencies.remoteOnline,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,
            onEntityClick = { item ->
                navController.navigate(
                    EntityDetailRoute.create(
                        type = "space",
                        id = item.id,
                    )
                )
            },
            onTopicClick = { item ->
                if (!item.canOpen) {
                    if (item.lockReason == "member_required") {
                        openMembership(item.space.id)
                    }
                    return@DiscoverScreen
                }

                navController.navigate(
                    TopicDetailRoute.create(
                        id = item.id,
                        publishId = item.publishId,
                    )
                )
            },
            onDocClick = { item ->
                when (item.type) {
                    "doc" -> {
                        navController.navigate(
                            DocDetailRoute.create(
                                docId = item.id,
                            )
                        )
                    }

                    "slide" -> {
                        navController.navigate(
                            SlideDetailRoute.create(
                                slideId = item.id,
                            )
                        )
                    }
                }
            },
            onMoreEntities = { mode ->
                navController.navigate(
                    SpaceListRoute.create(mode = mode)
                )
            },
            onMoreTopics = {
                navController.navigate(TopicListRoute.route)
            },
            onMoreContent = {
                navController.navigate(ContentListRoute.route)
            },
        )
    }

    /*
     * Space List
     */
    composable(
        route = SpaceListRoute.route,
        arguments = listOf(
            navArgument(SpaceListRoute.modeArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        val mode = SpaceListMode.fromValue(
            backStackEntry.arguments?.getString(
                SpaceListRoute.modeArg
            )
        )

        val factory = remember(
            mode,
            dependencies.spaceListRepository,
        ) {
            SpaceListViewModelFactory(
                mode = mode,
                repository = dependencies.spaceListRepository,
            )
        }

        val spaceListViewModel: SpaceListViewModel = viewModel(
            key = "space-list:${mode.value}",
            factory = factory,
        )

        val spaceListState by spaceListViewModel.uiState.collectAsStateWithLifecycle()

        SpaceListScreen(
            state = spaceListState,
            onSpaceClick = { item ->
                navController.navigate(
                    EntityDetailRoute.create(
                        type = "space",
                        id = item.id,
                    )
                )
            },
            onLoadMore = spaceListViewModel::loadMore,
            onRetry = spaceListViewModel::retry,
        )
    }

    /*
     * Topic List
     */
    composable(route = TopicListRoute.route) {
        val factory = remember(
            dependencies.topicListRepository,
        ) {
            TopicListViewModelFactory(
                repository = dependencies.topicListRepository,
            )
        }

        val topicListViewModel: TopicListViewModel = viewModel(
            key = "topic-list",
            factory = factory,
        )

        val topicListState by topicListViewModel.uiState.collectAsStateWithLifecycle()

        TopicListScreen(
            state = topicListState,
            onTopicClick = { item ->
                navController.navigate(
                    TopicDetailRoute.create(
                        id = item.id,
                        publishId = item.publishId,
                    )
                )
            },
            onLoadMore = { mode ->
                topicListViewModel.loadMore(mode = mode)
            },
            onRetry = topicListViewModel::retry,
            onRetrySection = { mode ->
                topicListViewModel.retrySection(mode = mode)
            },
        )
    }

    /*
     * Content List
     */
    composable(route = ContentListRoute.route) {
        val factory = remember(
            dependencies.contentListRepository,
        ) {
            ContentListViewModelFactory(
                repository = dependencies.contentListRepository,
            )
        }

        val contentListViewModel: ContentListViewModel = viewModel(
            key = "content-list",
            factory = factory,
        )

        val contentListState by contentListViewModel.uiState.collectAsStateWithLifecycle()

        ContentListScreen(
            state = contentListState,
            onContentClick = { item ->
                when (item.type) {
                    "doc" -> {
                        navController.navigate(
                            DocDetailRoute.create(
                                docId = item.id,
                            )
                        )
                    }

                    "slide" -> {
                        navController.navigate(
                            SlideDetailRoute.create(
                                slideId = item.id,
                            )
                        )
                    }
                }
            },
            onLoadMore = { type ->
                contentListViewModel.loadMore(contentType = type)
            },
            onRetry = contentListViewModel::retry,
        )
    }

    /*
     * Membership
     */
    composable(
        route = MembershipRoute.route,
        arguments = listOf(
            navArgument(MembershipRoute.spaceIdArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        val spaceId = backStackEntry.arguments
            ?.getString(MembershipRoute.spaceIdArg)
            .orEmpty()

        val membershipViewModel: MembershipViewModel = viewModel(
            key = "membership:$spaceId",
            factory = MembershipViewModelFactory(
                spaceId = spaceId,
                repository = dependencies.membershipRepository,
                wechatPayLauncher = dependencies.wechatPayLauncher,
            ),
        )

        val membershipState by membershipViewModel.uiState.collectAsStateWithLifecycle()

        MembershipScreen(
            uiState = membershipState,
            onRetry = membershipViewModel::retry,
            onSelectPlan = membershipViewModel::selectPlan,
            onPurchase = membershipViewModel::purchase,
        )
    }

    /*
     * Space Service
     */
    composable(
        route = EntityServiceRoute.route,
        arguments = listOf(
            navArgument(EntityServiceRoute.spaceIdArg) {
                type = NavType.StringType
            },
            navArgument(EntityServiceRoute.modeArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->

        val spaceId = backStackEntry.arguments
            ?.getString(EntityServiceRoute.spaceIdArg)
            .orEmpty()

        val mode = backStackEntry.arguments
            ?.getString(EntityServiceRoute.modeArg)
            .orEmpty()

        val factory = remember(
            spaceId,
            mode,
            dependencies.entityServiceRepository,
        ) {
            EntityServiceViewModelFactory(
                spaceId = spaceId,
                initialMode = mode,
                repository = dependencies.entityServiceRepository,
            )
        }

        val serviceViewModel: EntityServiceViewModel = viewModel(
            key = "entity-service:$spaceId:$mode",
            factory = factory,
        )

        val serviceState by serviceViewModel.uiState
            .collectAsStateWithLifecycle()

        LaunchedEffect(
            serviceViewModel,
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            when {
                dependencies.authState !is AuthState.SignedIn -> {
                    serviceViewModel.setUnavailable(
                        "请先登录后查看服务请求"
                    )
                }

                !dependencies.remoteOnline -> {
                    serviceViewModel.setUnavailable(
                        "当前无法连接服务"
                    )
                }

                else -> {
                    serviceViewModel.start()
                }
            }
        }

        EntityServiceScreen(
            state = serviceState,
            pickerRepository = dependencies.contentPickerRepository,

            onOpenReference = { ref, source ->

                val serviceId = serviceState.selectedServiceId

                val targetId = ref.targetId
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

                val targetType = EntityServiceReferenceType
                    .values()
                    .firstOrNull {
                        it.value == ref.targetType
                    }

                if (
                    serviceId != null &&
                    targetId != null &&
                    targetType != null
                ) {
                    navController.navigate(
                        EntityServiceReferencePreviewRoute.create(
                            serviceId = serviceId,
                            targetType = targetType.value,
                            targetId = targetId,
                            source = source.value,
                        )
                    )
                }
            },

            onRetry = serviceViewModel::retry,

            onOpenDetail = serviceViewModel::openDetail,
            onCloseDetail = serviceViewModel::closeDetail,
            onRetryDetail = serviceViewModel::retryDetail,
            onWithdraw = serviceViewModel::withdrawService,

            onSubmit = serviceViewModel::submitService,
            onConsumeCreated = serviceViewModel::consumeCreatedService,
            onClearSubmitError = serviceViewModel::clearSubmitError,

            onMarkInReview = serviceViewModel::markInReview,
            onAccept = serviceViewModel::acceptService,
            onDecline = serviceViewModel::declineService,
            onClose = serviceViewModel::closeService,

            onBack = {
                navController.popBackStack()
            },
        )
    }

    /*
     * Space Service Reference Preview
     */
    composable(
        route = EntityServiceReferencePreviewRoute.route,
        arguments = listOf(
            navArgument(EntityServiceReferencePreviewRoute.serviceIdArg) {
                type = NavType.StringType
            },
            navArgument(EntityServiceReferencePreviewRoute.targetTypeArg) {
                type = NavType.StringType
            },
            navArgument(EntityServiceReferencePreviewRoute.targetIdArg) {
                type = NavType.StringType
            },
            navArgument(EntityServiceReferencePreviewRoute.sourceArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->

        val serviceId = backStackEntry.arguments
            ?.getString(EntityServiceReferencePreviewRoute.serviceIdArg)
            .orEmpty()

        val targetTypeValue = backStackEntry.arguments
            ?.getString(EntityServiceReferencePreviewRoute.targetTypeArg)
            .orEmpty()

        val targetId = backStackEntry.arguments
            ?.getString(EntityServiceReferencePreviewRoute.targetIdArg)
            .orEmpty()

        val sourceValue = backStackEntry.arguments
            ?.getString(EntityServiceReferencePreviewRoute.sourceArg)
            .orEmpty()

        val targetType = EntityServiceReferenceType.values()
            .firstOrNull { it.value == targetTypeValue }

        val source = EntityServiceReferenceSource.values()
            .firstOrNull { it.value == sourceValue }

        if (
            serviceId.isBlank() ||
            targetId.isBlank() ||
            targetType == null ||
            source == null
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("关联内容参数无效")
            }
        } else {

            val factory = remember(
                serviceId,
                targetType,
                targetId,
                source,
                dependencies.entityServiceRepository,
                dependencies.imageRepository,
            ) {
                EntityServiceReferencePreviewViewModelFactory(
                    serviceId = serviceId,
                    targetType = targetType,
                    targetId = targetId,
                    source = source,
                    repository = dependencies.entityServiceRepository,
                    imageRepository = dependencies.imageRepository,
                )
            }

            val previewViewModel: EntityServiceReferencePreviewViewModel =
                viewModel(
                    key = "entity-service-reference:$serviceId:${targetType.value}:$targetId:${source.value}",
                    factory = factory,
                )

            val previewState by previewViewModel.uiState
                .collectAsStateWithLifecycle()

            DisposableEffect(previewViewModel) {
                previewViewModel.start()

                onDispose {
                    previewViewModel.stop()
                }
            }

            EntityServiceReferencePreviewScreen(
                state = previewState,
                onOpenPdf = { title, url, assetId ->
                    navController.navigate(
                        PdfReaderRoute.create(
                            title = title,
                            url = url,
                            assetId = assetId,
                        )
                    )
                },
                onRetry = previewViewModel::retry,
                onRetryPdfDelivery = previewViewModel::retryPdfDelivery,
                onRetryMediaPreview =
                    previewViewModel::retryMediaPreview,
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }

    /*
     * Space Detail
     *
     * 当前有两个会员入口：
     * 1. EntityHeader：开通会员 / 会员中心
     * 2. 点击 member-only Topic / Content：后端 canOpen / lockReason 拦截。
     */
    composable(
        route = EntityDetailRoute.route,
        arguments = listOf(
            navArgument(EntityDetailRoute.typeArg) {
                type = NavType.StringType
            },
            navArgument(EntityDetailRoute.idArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        val type = backStackEntry.arguments
            ?.getString(EntityDetailRoute.typeArg)
            .orEmpty()

        val id = backStackEntry.arguments
            ?.getString(EntityDetailRoute.idArg)
            .orEmpty()

        if (type != "space") {
            return@composable
        }

        val entityViewModel: EntityDetailViewModel = viewModel(
            key = "entity:$id",
            factory = EntityDetailViewModelFactory(
                id = id,
                repository = dependencies.entityDetailRepository,
            ),
        )

        val entityDetailState by entityViewModel.uiState.collectAsStateWithLifecycle()

        EntityDetailScreen(
            state = entityDetailState,
            onMembershipClick = { spaceId -> openMembership(spaceId) },

            onContentClick = { item ->
                /*
                 * 后端已经计算访问权限，
                 * Android 不重新推导会员权限。
                 */
                if (!item.canOpen) {
                    if (item.lockReason == "member_required") {
                        openMembership(
                            entityDetailState.detail?.entity?.id
                        )
                    }
                    return@EntityDetailScreen
                }

                when (item.type) {
                    "collection" -> {
                        navController.navigate(
                            TopicDetailRoute.create(
                                id = item.id,
                                publishId = item.publishId,
                            )
                        )
                    }

                    "doc" -> {
                        navController.navigate(
                            DocDetailRoute.create(
                                docId = item.id,
                            )
                        )
                    }

                    "slide" -> {
                        navController.navigate(
                            SlideDetailRoute.create(
                                slideId = item.id,
                            )
                        )
                    }
                }
            },

            onServiceClick = { spaceId ->
                val role =
                    entityDetailState.detail?.entity?.role

                val isManager =
                    role == "owner" || role == "admin"

                navController.navigate(
                    EntityServiceRoute.create(
                        spaceId = spaceId,
                        isManager = isManager,
                    )
                )
            },

        )
    }

    /*
     * Topic Detail
     *
     * 当前同样有两个会员入口：
     * 1. Topic 本身 member_required → TopicHeader Membership CTA
     * 2. Topic 内某个 Doc 被锁 → 点击 Doc 时跳 Membership
     */
    composable(
        route = TopicDetailRoute.route,
        arguments = listOf(
            navArgument(TopicDetailRoute.idArg) {
                type = NavType.StringType
            },
            navArgument(TopicDetailRoute.publishIdArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->
        val id = backStackEntry.arguments
            ?.getString(TopicDetailRoute.idArg)
            .orEmpty()

        val topicViewModel: TopicDetailViewModel = viewModel(
            key = "topic:$id",
            factory = TopicDetailViewModelFactory(
                topicId = id,
                repository = dependencies.topicDetailRepository,
            ),
        )

        val topicDetailState by topicViewModel.uiState.collectAsStateWithLifecycle()

        TopicDetailScreen(
            state = topicDetailState,
            onSpaceClick = {
                topicDetailState.detail?.space?.let { space ->
                    navController.navigate(
                        EntityDetailRoute.create(
                            type = "space",
                            id = space.id,
                        )
                    )
                }
            },
            onDocClick = { doc ->
                /*
                 * Topic Doc 自己的 access guard。
                 */
                if (!doc.canOpen) {
                    if (doc.lockReason == "member_required") {
                        openMembership(
                            topicDetailState.detail?.space?.id
                        )
                    }
                    return@TopicDetailScreen
                }

                val topicId = topicDetailState.detail?.topic?.id

                navController.navigate(
                    DocDetailRoute.create(
                        docId = doc.id,
                        topicId = topicId,
                    )
                )
            },
            /*
             * Topic Header 自身的会员 CTA。
             * TopicDetailScreen 内已经限定：
             * topic.lockReason == "member_required"
             * 时才会把这个 callback 给 TopicHeader。
             */
            onMembershipClick = {
                openMembership(
                    topicDetailState.detail?.space?.id
                )
            },
        )
    }

    /*
     * Doc Detail
     */
    composable(
        route = DocDetailRoute.route,
        arguments = listOf(
            navArgument(DocDetailRoute.docIdArg) {
                type = NavType.StringType
            },
            navArgument(DocDetailRoute.topicIdArg) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            },
        ),
    ) { backStackEntry ->
        val docId = backStackEntry.arguments
            ?.getString(DocDetailRoute.docIdArg)
            .orEmpty()

        val topicId = backStackEntry.arguments
            ?.getString(DocDetailRoute.topicIdArg)

        val docViewModel: DocDetailViewModel = viewModel(
            key = "doc:$docId:${topicId.orEmpty()}",
            factory = DocDetailViewModelFactory(
                docId = docId,
                topicId = topicId,
                repository = dependencies.docDetailRepository,
            ),
        )

        val docDetailState by docViewModel.uiState.collectAsStateWithLifecycle()

        DocDetailScreen(
            state = docDetailState,
            onSpaceClick = {
                docDetailState.detail?.space?.let { space ->
                    navController.navigate(
                        EntityDetailRoute.create(
                            type = "space",
                            id = space.id,
                        )
                    )
                }
            },
        )
    }

    /*
     * Slide Detail
     */
    composable(
        route = SlideDetailRoute.route,
        arguments = listOf(
            navArgument(SlideDetailRoute.slideIdArg) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->
        val slideId = backStackEntry.arguments
            ?.getString(SlideDetailRoute.slideIdArg)
            .orEmpty()

        val slideViewModel: SlideDetailViewModel = viewModel(
            key = "slide:$slideId",
            factory = SlideDetailViewModelFactory(
                slideId = slideId,
                repository = dependencies.slideDetailRepository,
            ),
        )

        val slideDetailState by slideViewModel.uiState.collectAsStateWithLifecycle()

        SlideDetailScreen(
            state = slideDetailState,
            onPreviousPage = slideViewModel::goToPreviousPage,
            onNextPage = slideViewModel::goToNextPage,
            onLoadQuestions = slideViewModel::loadQuestions,
            onSubmitQuestion = { text, onSuccess ->
                slideViewModel.submitQuestion(
                    text = text,
                    onSuccess = onSuccess,
                )
            },
            onMarkQuestionAnswered = slideViewModel::markQuestionAnswered,
            onHideQuestion = slideViewModel::hideQuestion,
            onClearQuestionSubmitError = slideViewModel::clearQuestionSubmitError,
            onClearQuestionActionError = slideViewModel::clearQuestionActionError,
            onRetry = slideViewModel::retry,
        )
    }
}