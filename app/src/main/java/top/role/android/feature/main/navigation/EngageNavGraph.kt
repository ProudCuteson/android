package top.role.android.feature.main.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import top.role.android.core.auth.AuthState
import top.role.android.app.navigation.BountyDetailRoute
import top.role.android.app.navigation.BountyListRoute
import top.role.android.app.navigation.MarketDetailRoute
import top.role.android.app.navigation.MarketListRoute
import top.role.android.app.navigation.MarketOrderDetailRoute
import top.role.android.app.navigation.MarketOrderListRoute
import top.role.android.app.navigation.SurveyDetailRoute
import top.role.android.app.navigation.SurveyListRoute
import top.role.android.core.model.bounty.MobileBountyListMode
import top.role.android.core.model.market.MobileMarketListMode
import top.role.android.core.model.survey.MobileSurveyListMode
import top.role.android.app.navigation.NoteAdoptionRoute
import top.role.android.feature.note.adoption.NoteAdoptionScreen
import top.role.android.feature.survey.SurveyListScreen
import top.role.android.feature.survey.SurveyListViewModel
import top.role.android.feature.survey.SurveyListViewModelFactory
import top.role.android.feature.survey.detail.SurveyDetailScreen
import top.role.android.feature.survey.detail.SurveyDetailViewModel
import top.role.android.feature.survey.detail.SurveyDetailViewModelFactory
import top.role.android.feature.bounty.BountyListScreen
import top.role.android.feature.bounty.BountyListViewModel
import top.role.android.feature.bounty.BountyListViewModelFactory
import top.role.android.feature.bounty.detail.BountyDetailScreen
import top.role.android.feature.bounty.detail.BountyDetailViewModel
import top.role.android.feature.bounty.detail.BountyDetailViewModelFactory
import top.role.android.feature.market.MarketListScreen
import top.role.android.feature.market.MarketListViewModel
import top.role.android.feature.market.MarketListViewModelFactory
import top.role.android.feature.market.detail.MarketDetailScreen
import top.role.android.feature.market.detail.MarketDetailViewModel
import top.role.android.feature.market.detail.MarketDetailViewModelFactory
import top.role.android.feature.market.order.MarketOrderListScreen
import top.role.android.feature.market.order.MarketOrderListViewModel
import top.role.android.feature.market.order.MarketOrderListViewModelFactory
import top.role.android.feature.market.order.detail.MarketOrderDetailScreen
import top.role.android.feature.market.order.detail.MarketOrderDetailViewModel
import androidx.compose.runtime.remember
import top.role.android.core.data.note.NoteAdoptionLocalRepository

fun NavGraphBuilder.engageNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
    onLogin: () -> Unit,
    onRetryAuth: () -> Unit,
    onReSignIn: () -> Unit,
) {

    /*
     * ================================================================
     * Survey List
     * ================================================================
     */
    composable(
        route = SurveyListRoute.route,
        arguments = listOf(
            navArgument(SurveyListRoute.modeArg) {
                type = NavType.StringType
            }
        ),
    ) { backStackEntry ->

        val mode = MobileSurveyListMode.fromValue(
            backStackEntry.arguments?.getString(
                SurveyListRoute.modeArg
            )
        )

        val factory = remember(
            mode,
            dependencies.surveyListRepository,
        ) {
            SurveyListViewModelFactory(
                mode = mode,
                repository = dependencies.surveyListRepository,
            )
        }

        val viewModel: SurveyListViewModel = viewModel(
            key = "survey-list:${mode.value}",
            factory = factory,
        )

        LaunchedEffect(
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                viewModel.start()
            } else {
                viewModel.stop()
            }
        }

        SurveyListScreen(
            viewModel = viewModel,
            authState = dependencies.authState,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,

            onModeChange = { targetMode ->
                if (targetMode != mode) {
                    navController.navigate(
                        SurveyListRoute.create(
                            mode = targetMode,
                        )
                    ) {
                        popUpTo(SurveyListRoute.route) {
                            inclusive = true
                        }
                    }
                }
            },

            onSurveyClick = { surveyId ->
                navController.navigate(
                    SurveyDetailRoute.create(
                        surveyId = surveyId,
                    )
                )
            },

            onLoadMore = viewModel::loadMore,
            onRetry = viewModel::retry,
        )
    }

    /*
     * ================================================================
     * Survey Detail
     * ================================================================
     */
    composable(
        route = SurveyDetailRoute.route,
        arguments = listOf(
            navArgument(SurveyDetailRoute.surveyIdArg) {
                type = NavType.StringType
            }
        ),
    ) { backStackEntry ->

        val surveyId = backStackEntry.arguments
            ?.getString(SurveyDetailRoute.surveyIdArg)
            ?: return@composable

        val factory = remember(
            surveyId,
            dependencies.surveyDetailRepository,
        ) {
            SurveyDetailViewModelFactory(
                surveyId = surveyId,
                repository = dependencies.surveyDetailRepository,
            )
        }

        val viewModel: SurveyDetailViewModel = viewModel(
            key = "survey-detail:$surveyId",
            factory = factory,
        )

        LaunchedEffect(
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                viewModel.start()
            } else {
                viewModel.stop()
            }
        }

        SurveyDetailScreen(
            viewModel = viewModel,
            authState = dependencies.authState,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,
            onAnswerChange = viewModel::updateAnswer,
            onPrevious = viewModel::previousGroup,
            onNext = viewModel::nextGroup,
            onSubmit = viewModel::submit,
            onRetry = viewModel::retry,
        )
    }

    /*
     * ================================================================
     * Bounty List
     * ================================================================
     */
    composable(
        route = BountyListRoute.route,
        arguments = listOf(
            navArgument(BountyListRoute.modeArg) {
                type = NavType.StringType
            }
        ),
    ) { backStackEntry ->

        val mode = MobileBountyListMode.fromValue(
            backStackEntry.arguments?.getString(
                BountyListRoute.modeArg
            )
        )

        val factory = remember(
            mode,
            dependencies.bountyListRepository,
        ) {
            BountyListViewModelFactory(
                mode = mode,
                repository = dependencies.bountyListRepository,
            )
        }

        val bountyViewModel: BountyListViewModel = viewModel(
            key = "bounty-list:${mode.value}",
            factory = factory,
        )

        LaunchedEffect(
            bountyViewModel,
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                bountyViewModel.start()
            } else {
                bountyViewModel.stop()
            }
        }

        BountyListScreen(
            viewModel = bountyViewModel,
            authState = dependencies.authState,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,
            onModeChange = { targetMode ->
                if (targetMode != mode) {
                    navController.navigate(
                        BountyListRoute.create(
                            mode = targetMode
                        )
                    )
                }
            },
            onBountyClick = { bountyId ->
                navController.navigate(
                    BountyDetailRoute.create(
                        bountyId = bountyId
                    )
                )
            },
            onLoadMore = bountyViewModel::loadMore,
            onRetry = bountyViewModel::retry,
        )
    }

    /*
     * ================================================================
     * Bounty Detail
     * ================================================================
     */
    composable(
        route = BountyDetailRoute.route,
        arguments = listOf(
            navArgument(BountyDetailRoute.bountyIdArg) {
                type = NavType.StringType
            }
        ),
    ) { backStackEntry ->

        val bountyId = backStackEntry.arguments
            ?.getString(BountyDetailRoute.bountyIdArg)
            ?: return@composable

        val factory = remember(
            bountyId,
            dependencies.bountyDetailRepository,
        ) {
            BountyDetailViewModelFactory(
                bountyId = bountyId,
                repository = dependencies.bountyDetailRepository,
            )
        }

        val bountyViewModel: BountyDetailViewModel = viewModel(
            key = "bounty-detail:$bountyId",
            factory = factory,
        )

        LaunchedEffect(
            bountyViewModel,
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                bountyViewModel.start()
            } else {
                bountyViewModel.stop()
            }
        }

        val uriHandler = LocalUriHandler.current

        BountyDetailScreen(
            viewModel = bountyViewModel,
            authState = dependencies.authState,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,
            onRetry = bountyViewModel::retry,
            onTake = bountyViewModel::take,
            onOpenWeb = {
                uriHandler.openUri(
                    "https://role.top/dashboard/bounty/$bountyId/submissions"
                )
            },
        )
    }

    /*
     * ================================================================
     * Market List
     * ================================================================
     */
    composable(
        route = MarketListRoute.route,
        arguments = listOf(
            navArgument(MarketListRoute.modeArg) {
                type = NavType.StringType
            }
        ),
    ) { backStackEntry ->

        val mode = MobileMarketListMode.fromValue(
            backStackEntry.arguments?.getString(
                MarketListRoute.modeArg
            )
        )

        val factory = remember(
            mode,
            dependencies.marketListRepository,
        ) {
            MarketListViewModelFactory(
                mode = mode,
                repository = dependencies.marketListRepository,
            )
        }

        val marketViewModel: MarketListViewModel = viewModel(
            key = "market-list:${mode.value}",
            factory = factory,
        )

        LaunchedEffect(
            marketViewModel,
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                marketViewModel.start()
            } else {
                marketViewModel.stop()
            }
        }

        MarketListScreen(
            viewModel = marketViewModel,
            authState = dependencies.authState,
            remoteOnline = dependencies.remoteOnline,
            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,
            onModeChange = { targetMode ->
                if (targetMode != mode) {
                    navController.navigate(
                        MarketListRoute.create(
                            mode = targetMode
                        )
                    )
                }
            },
            onMarketClick = { listingId ->
                navController.navigate(
                    MarketDetailRoute.create(
                        listingId = listingId
                    )
                )
            },
            onLoadMore = marketViewModel::loadMore,
            onRetry = marketViewModel::retry,
        )
    }

    /*
     * ================================================================
     * Market Detail
     * ================================================================
     */
    composable(
        route = MarketDetailRoute.route,
        arguments = listOf(
            navArgument(MarketDetailRoute.listingIdArg) {
                type = NavType.StringType
            }
        ),
    ) { backStackEntry ->

        val listingId = backStackEntry.arguments
            ?.getString(MarketDetailRoute.listingIdArg)
            ?: return@composable

        val factory = remember(
            listingId,
            dependencies.marketDetailRepository,
        ) {
            MarketDetailViewModelFactory(
                listingId = listingId,
                repository = dependencies.marketDetailRepository,
                wechatPayLauncher = dependencies.wechatPayLauncher,
            )
        }

        val marketViewModel: MarketDetailViewModel = viewModel(
            key = "market-detail:$listingId",
            factory = factory,
        )

        /*
         * ------------------------------------------------
         * Lifecycle
         * ------------------------------------------------
         */
        LaunchedEffect(
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                marketViewModel.start()
            } else {
                marketViewModel.stop()
            }
        }

        /*
         * ------------------------------------------------
         * Payment success navigation
         * ------------------------------------------------
         */
        val state by marketViewModel.uiState.collectAsStateWithLifecycle()

        val purchasedOrderId = state.purchasedOrderId
            ?.takeIf { state.paymentSuccess }

        LaunchedEffect(purchasedOrderId) {
            purchasedOrderId?.let { orderId ->

                marketViewModel.consumePaymentSuccess()

                navController.navigate(
                    MarketOrderDetailRoute.create(
                        orderId = orderId,
                    )
                )
            }
        }

        /*
         * ------------------------------------------------
         * Screen
         * ------------------------------------------------
         */
        MarketDetailScreen(
            viewModel = marketViewModel,

            authState = dependencies.authState,
            remoteOnline = dependencies.remoteOnline,

            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,

            onRetry = marketViewModel::retry,
            onDecreaseQuantity = marketViewModel::decreaseQuantity,
            onIncreaseQuantity = marketViewModel::increaseQuantity,

            onReceiverNameChange = marketViewModel::updateReceiverName,
            onReceiverPhoneChange = marketViewModel::updateReceiverPhone,
            onAddressLine1Change = marketViewModel::updateAddressLine1,
            onPostalCodeChange = marketViewModel::updatePostalCode,
            onBuyerNoteChange = marketViewModel::updateBuyerNote,

            onRegionSelected = marketViewModel::selectRegion,
            onBuy = marketViewModel::purchase,
        )
    }

    /*
     * ================================================================
     * Market Order List
     * ================================================================
     */

    composable(route = MarketOrderListRoute.route) {

        val factory = remember(
            dependencies.marketOrderListRepository,
        ) {
            MarketOrderListViewModelFactory(
                repository = dependencies.marketOrderListRepository,
            )
        }

        val marketOrderListViewModel: MarketOrderListViewModel = viewModel(
            key = "market-order-list",
            factory = factory,
        )

        LaunchedEffect(
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                marketOrderListViewModel.start()
            } else {
                marketOrderListViewModel.stop()
            }
        }

        val state by marketOrderListViewModel.uiState.collectAsStateWithLifecycle()

        MarketOrderListScreen(
            state = state,
            authState = dependencies.authState,
            remoteOnline = dependencies.remoteOnline,

            onBack = {
                navController.popBackStack()
            },

            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,

            onOrderClick = { item ->
                navController.navigate(
                    MarketOrderDetailRoute.create(
                        orderId = item.id,
                    )
                )
            },

            onLoadMore = marketOrderListViewModel::loadMore,
            onRetry = marketOrderListViewModel::retry,
        )
    }

    /*
  * ================================================================
  * Market Order Detail
  * ================================================================
  */
    composable(
        route = MarketOrderDetailRoute.route,
        arguments = listOf(
            navArgument(MarketOrderDetailRoute.orderIdArg) {
                type = NavType.StringType
            }
        ),
    ) { backStackEntry ->

        val orderId = backStackEntry.arguments
            ?.getString(MarketOrderDetailRoute.orderIdArg)
            ?: return@composable

        val factory = remember(
            orderId,
            dependencies.marketOrderDetailRepository,
        ) {
            MarketOrderDetailViewModel.factory(
                orderId = orderId,
                repository = dependencies.marketOrderDetailRepository,
            )
        }

        val marketOrderDetailViewModel: MarketOrderDetailViewModel =
            viewModel(
                key = "market-order-detail:$orderId",
                factory = factory,
            )

        /*
         * 认证与网络生命周期
         */
        LaunchedEffect(
            dependencies.authState,
            dependencies.remoteOnline,
        ) {
            if (
                dependencies.authState is AuthState.SignedIn &&
                dependencies.remoteOnline
            ) {
                marketOrderDetailViewModel.start()
            } else {
                marketOrderDetailViewModel.stop()
            }
        }

        val state by marketOrderDetailViewModel.uiState
            .collectAsStateWithLifecycle()

        MarketOrderDetailScreen(
            state = state,

            authState = dependencies.authState,
            remoteOnline = dependencies.remoteOnline,

            onLogin = onLogin,
            onRetryAuth = onRetryAuth,
            onReSignIn = onReSignIn,

            onBack = {
                navController.popBackStack()
            },

            onProductClick = { listingId ->
                navController.navigate(
                    MarketDetailRoute.create(
                        listingId = listingId
                    )
                )
            },

            onRetry = marketOrderDetailViewModel::retry,
        )
    }


    /*
     * ================================================================
     * Note Adoption
     * ================================================================
     */
    composable(NoteAdoptionRoute.route) {

        NoteAdoptionScreen(
            guestLocalOwnerId = dependencies.localOwnerUserId,
            authState = dependencies.authState,
            repository = dependencies.noteAdoptionLocalRepository,
            onLogin = onLogin,
            onRetry = onRetryAuth,
            onReSignIn = onReSignIn,
            onBack = {
                navController.popBackStack()
            },
        )
    }

}
