package top.role.android.feature.main.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import top.role.android.core.auth.AuthState

import top.role.android.app.navigation.EntityServiceRoute
import top.role.android.app.navigation.EntityServiceReferencePreviewRoute
import top.role.android.app.navigation.PdfReaderRoute

import top.role.android.feature.entity.service.EntityServiceScreen
import top.role.android.feature.entity.service.EntityServiceViewModel
import top.role.android.feature.entity.service.EntityServiceViewModelFactory

import top.role.android.feature.entity.service.EntityServiceReferencePreviewScreen
import top.role.android.feature.entity.service.EntityServiceReferencePreviewViewModel
import top.role.android.feature.entity.service.EntityServiceReferencePreviewViewModelFactory
import top.role.android.feature.entity.service.EntityServiceReferenceSource
import top.role.android.feature.entity.service.EntityServiceReferenceType

fun NavGraphBuilder.entityServiceNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
) {

    /*
     * Space Service
     *
     * 服务申请、审核、处理及正式交付。
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

        val serviceViewModel: EntityServiceViewModel =
            viewModel(
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
            assetUploadService = dependencies.assetUploadService,
            onOpenReference = { ref, source ->
                val serviceId = serviceState.selectedServiceId

                val targetId = ref.targetId
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }

                val targetType =
                    EntityServiceReferenceType.values()
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

            onRetry =
                serviceViewModel::retry,

            onOpenDetail =
                serviceViewModel::openDetail,

            onCloseDetail =
                serviceViewModel::closeDetail,

            onRetryDetail =
                serviceViewModel::retryDetail,

            onWithdraw =
                serviceViewModel::withdrawService,

            onSubmit =
                serviceViewModel::submitService,

            onConsumeCreated =
                serviceViewModel::consumeCreatedService,

            onClearSubmitError =
                serviceViewModel::clearSubmitError,

            onMarkInReview =
                serviceViewModel::markInReview,

            onAccept =
                serviceViewModel::acceptService,

            onDecline =
                serviceViewModel::declineService,

            onClose =
                serviceViewModel::closeService,

            onBack = {
                navController.popBackStack()
            },
        )
    }

    /*
     * Space Service Reference Preview
     *
     * Note / Doc / Slide / PDF
     *
     * 同时支持参考资料与正式交付内容。
     */
    composable(
        route = EntityServiceReferencePreviewRoute.route,
        arguments = listOf(
            navArgument(
                EntityServiceReferencePreviewRoute.serviceIdArg
            ) {
                type = NavType.StringType
            },

            navArgument(
                EntityServiceReferencePreviewRoute.targetTypeArg
            ) {
                type = NavType.StringType
            },

            navArgument(
                EntityServiceReferencePreviewRoute.targetIdArg
            ) {
                type = NavType.StringType
            },

            navArgument(
                EntityServiceReferencePreviewRoute.sourceArg
            ) {
                type = NavType.StringType
            },
        ),
    ) { backStackEntry ->

        val serviceId = backStackEntry.arguments
            ?.getString(
                EntityServiceReferencePreviewRoute.serviceIdArg
            )
            .orEmpty()

        val targetTypeValue = backStackEntry.arguments
            ?.getString(
                EntityServiceReferencePreviewRoute.targetTypeArg
            )
            .orEmpty()

        val targetId = backStackEntry.arguments
            ?.getString(
                EntityServiceReferencePreviewRoute.targetIdArg
            )
            .orEmpty()

        val sourceValue = backStackEntry.arguments
            ?.getString(
                EntityServiceReferencePreviewRoute.sourceArg
            )
            .orEmpty()

        val targetType =
            EntityServiceReferenceType.values()
                .firstOrNull {
                    it.value == targetTypeValue
                }

        val source =
            EntityServiceReferenceSource.values()
                .firstOrNull {
                    it.value == sourceValue
                }

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

                    repository =
                        dependencies.entityServiceRepository,

                    imageRepository =
                        dependencies.imageRepository,
                )
            }

            val previewViewModel:
                    EntityServiceReferencePreviewViewModel =
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

                onRetry =
                    previewViewModel::retry,

                onRetryPdfDelivery =
                    previewViewModel::retryPdfDelivery,

                onRetryMediaPreview =
                    previewViewModel::retryMediaPreview,

                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}