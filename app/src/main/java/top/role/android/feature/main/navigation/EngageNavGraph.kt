package top.role.android.feature.main.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument

import top.role.android.app.navigation.MainDestination
import top.role.android.app.navigation.SurveyListRoute
import top.role.android.app.navigation.SurveyDetailRoute
import top.role.android.app.navigation.BountyListRoute
import top.role.android.app.navigation.BountyDetailRoute
import top.role.android.app.navigation.MarketListRoute
import top.role.android.app.navigation.MarketDetailRoute

import top.role.android.feature.engage.EngageScreen
import top.role.android.feature.engage.EngageViewModel
import top.role.android.feature.engage.EngageViewModelFactory

import top.role.android.core.model.survey.MobileSurveyListMode

import top.role.android.feature.survey.SurveyListViewModelFactory
import top.role.android.feature.survey.SurveyListViewModel
import top.role.android.feature.survey.SurveyListScreen
import top.role.android.feature.survey.detail.SurveyDetailScreen
import top.role.android.feature.survey.detail.SurveyDetailViewModel
import top.role.android.feature.survey.detail.SurveyDetailViewModelFactory
import top.role.android.core.model.bounty.MobileBountyListMode
import top.role.android.feature.bounty.BountyListViewModel
import top.role.android.feature.bounty.BountyListScreen
import top.role.android.feature.bounty.BountyListViewModelFactory
import top.role.android.feature.bounty.detail.BountyDetailViewModel
import top.role.android.feature.bounty.detail.BountyDetailScreen
import top.role.android.feature.bounty.detail.BountyDetailViewModelFactory
import top.role.android.feature.market.MarketListViewModel
import top.role.android.feature.market.MarketListScreen
import top.role.android.feature.market.MarketListViewModelFactory
import top.role.android.feature.market.detail.MarketDetailViewModel
import top.role.android.feature.market.detail.MarketDetailScreen
import top.role.android.feature.market.detail.MarketDetailViewModelFactory
import top.role.android.core.model.market.MobileMarketListMode

fun NavGraphBuilder.engageNavGraph(
    navController: NavHostController,
    dependencies: MainNavigationDependencies,
) {

    composable(
        route =
            MainDestination.Engage.route,
    ) {

        val factory =
            remember(
                dependencies.engageRepository,
            ) {
                EngageViewModelFactory(
                    repository = dependencies.engageRepository,
                )
            }

        val engageViewModel:
                EngageViewModel = viewModel(
                key = "engage-home",
                factory = factory,
            )

        LaunchedEffect(
            dependencies.remoteAuthenticated,
        ) {
            if (
                dependencies.remoteAuthenticated
            ) {
                engageViewModel.start()
            } else {
                engageViewModel.showSignedOut()
            }
        }

        val state by
        engageViewModel
            .uiState
            .collectAsStateWithLifecycle()

        EngageScreen(
            state = state,

            onRetry = engageViewModel::retry,

            onSurveyClick = {
                    surveyId ->

                navController.navigate(
                    SurveyDetailRoute.create(
                        surveyId =
                            surveyId,
                    )
                )
            },

            onSurveyMore = {
                navController.navigate(
                    SurveyListRoute.create(
                        mode =
                            MobileSurveyListMode.Featured,
                    )
                )
            },

            onBountyClick = {
                    bountyId ->

                navController.navigate(
                    BountyDetailRoute.create(
                        bountyId =
                            bountyId,
                    )
                )
            },

            onBountyMore = {
                navController.navigate(
                    BountyListRoute.create(
                        mode =
                            MobileBountyListMode.Featured,
                    )
                )
            },

            onMarketClick = {
                    listingId ->

                navController.navigate(
                    MarketDetailRoute.create(
                        listingId =
                            listingId,
                    )
                )
            },

            onMarketMore = {
                navController.navigate(
                    MarketListRoute.create(
                        mode =
                            MobileMarketListMode.Featured,
                    )
                )
            },
        )
    }

    composable(
        route =
            SurveyListRoute.route,

        arguments =
            listOf(
                navArgument(
                    SurveyListRoute
                        .modeArg
                ) {
                    type =
                        NavType.StringType
                }
            ),
    ) { backStackEntry ->

        val mode =
            MobileSurveyListMode
                .fromValue(
                    backStackEntry
                        .arguments
                        ?.getString(
                            SurveyListRoute
                                .modeArg
                        )
                )

        val factory =
            remember(
                mode,
                dependencies
                    .surveyListRepository,
            ) {
                SurveyListViewModelFactory(
                    mode =
                        mode,

                    repository =
                        dependencies
                            .surveyListRepository,
                )
            }

        val viewModel:
                SurveyListViewModel =
            viewModel(
                key =
                    "survey-list:${mode.value}",

                factory =
                    factory,
            )

        LaunchedEffect(
            dependencies
                .remoteAuthenticated,
        ) {
            if (
                dependencies
                    .remoteAuthenticated
            ) {
                viewModel.start()
            } else {
                viewModel.showSignedOut()
            }
        }

        val state by
        viewModel
            .uiState
            .collectAsStateWithLifecycle()

        SurveyListScreen(
            state =
                state,

            onModeChange = {
                    targetMode ->

                if (
                    targetMode !=
                    mode
                ) {
                    navController.navigate(
                        SurveyListRoute.create(
                            mode =
                                targetMode,
                        )
                    ) {
                        popUpTo(
                            SurveyListRoute.route
                        ) {
                            inclusive =
                                true
                        }
                    }
                }
            },

            onSurveyClick = {
                    surveyId ->

                navController.navigate(
                    SurveyDetailRoute.create(
                        surveyId =
                            surveyId,
                    )
                )
            },

            onLoadMore =
                viewModel::loadMore,

            onRetry =
                viewModel::retry,
        )
    }

    composable(
        route =
            SurveyDetailRoute.route,

        arguments =
            listOf(
                navArgument(
                    SurveyDetailRoute
                        .surveyIdArg
                ) {
                    type =
                        NavType.StringType
                }
            ),
    ) { backStackEntry ->

        val surveyId =
            backStackEntry
                .arguments
                ?.getString(
                    SurveyDetailRoute
                        .surveyIdArg
                )
                ?: return@composable

        val factory =
            remember(
                surveyId,
                dependencies
                    .surveyDetailRepository,
            ) {
                SurveyDetailViewModelFactory(
                    surveyId =
                        surveyId,

                    repository =
                        dependencies
                            .surveyDetailRepository,
                )
            }

        val viewModel:
                SurveyDetailViewModel =
            viewModel(
                key =
                    "survey-detail:$surveyId",

                factory =
                    factory,
            )

        LaunchedEffect(
            dependencies
                .remoteAuthenticated,
        ) {
            if (
                dependencies
                    .remoteAuthenticated
            ) {
                viewModel.start()
            } else {
                viewModel.showSignedOut()
            }
        }

        val state by
        viewModel
            .uiState
            .collectAsStateWithLifecycle()

        SurveyDetailScreen(
            state =
                state,

            onAnswerChange =
                viewModel::updateAnswer,

            onPrevious =
                viewModel::previousGroup,

            onNext =
                viewModel::nextGroup,

            onSubmit =
                viewModel::submit,
        )
    }

    composable(
        route =
            BountyListRoute.route,

        arguments =
            listOf(
                navArgument(
                    BountyListRoute.modeArg
                ) {
                    type =
                        NavType.StringType
                }
            ),
    ) { backStackEntry ->

        val mode =
            MobileBountyListMode
                .fromValue(
                    backStackEntry
                        .arguments
                        ?.getString(
                            BountyListRoute.modeArg
                        )
                )

        val factory =
            remember(
                mode,
                dependencies
                    .bountyListRepository,
            ) {
                BountyListViewModelFactory(
                    mode = mode,

                    repository =
                        dependencies
                            .bountyListRepository,
                )
            }

        val bountyViewModel:
                BountyListViewModel =
            viewModel(
                key =
                    "bounty-list:${mode.value}",

                factory =
                    factory,
            )

        LaunchedEffect(
            dependencies
                .remoteAuthenticated,
        ) {
            if (
                dependencies
                    .remoteAuthenticated
            ) {
                bountyViewModel.start()
            } else {
                bountyViewModel
                    .showSignedOut()
            }
        }

        val state by
        bountyViewModel
            .uiState
            .collectAsStateWithLifecycle()

        BountyListScreen(
            state = state,

            onModeChange = {
                    targetMode ->

                if (
                    targetMode != mode
                ) {
                    navController.navigate(
                        BountyListRoute.create(
                            targetMode
                        )
                    )
                }
            },

            onBountyClick = {
                    bountyId ->

                navController.navigate(
                    BountyDetailRoute.create(
                        bountyId
                    )
                )
            },

            onLoadMore =
                bountyViewModel::loadMore,

            onRetry =
                bountyViewModel::retry,
        )
    }

    composable(
        route =
            BountyDetailRoute.route,

        arguments =
            listOf(
                navArgument(
                    BountyDetailRoute
                        .bountyIdArg
                ) {
                    type =
                        NavType.StringType
                }
            ),
    ) { backStackEntry ->

        val bountyId =
            backStackEntry
                .arguments
                ?.getString(
                    BountyDetailRoute
                        .bountyIdArg
                )
                ?: return@composable

        val factory =
            remember(
                bountyId,
                dependencies
                    .bountyDetailRepository,
            ) {
                BountyDetailViewModelFactory(
                    bountyId =
                        bountyId,

                    repository =
                        dependencies
                            .bountyDetailRepository,
                )
            }

        val bountyViewModel:
                BountyDetailViewModel =
            viewModel(
                key =
                    "bounty-detail:$bountyId",

                factory =
                    factory,
            )

        LaunchedEffect(
            dependencies
                .remoteAuthenticated,
        ) {
            if (
                dependencies
                    .remoteAuthenticated
            ) {
                bountyViewModel.start()
            } else {
                bountyViewModel
                    .showSignedOut()
            }
        }

        val state by
        bountyViewModel
            .uiState
            .collectAsStateWithLifecycle()


        val uriHandler =
            LocalUriHandler.current

        BountyDetailScreen(
            state =
                state,

            onRetry =
                bountyViewModel::retry,

            onTake =
                bountyViewModel::take,

            onOpenWeb = {
                uriHandler.openUri(
                    "https://role.top/dashboard/bounty/$bountyId/submissions"
                )
            },
        )
    }

    composable(
        route =
            MarketListRoute.route,

        arguments =
            listOf(
                navArgument(
                    MarketListRoute.modeArg
                ) {
                    type =
                        NavType.StringType
                }
            ),
    ) { backStackEntry ->

        val mode =
            MobileMarketListMode
                .fromValue(
                    backStackEntry
                        .arguments
                        ?.getString(
                            MarketListRoute
                                .modeArg
                        )
                )

        val factory =
            remember(
                mode,
                dependencies
                    .marketListRepository,
            ) {
                MarketListViewModelFactory(
                    mode =
                        mode,

                    repository =
                        dependencies
                            .marketListRepository,
                )
            }

        val marketViewModel:
                MarketListViewModel =
            viewModel(
                key =
                    "market-list:${mode.value}",

                factory =
                    factory,
            )

        LaunchedEffect(
            dependencies
                .remoteAuthenticated,
        ) {
            if (
                dependencies
                    .remoteAuthenticated
            ) {
                marketViewModel.start()
            } else {
                marketViewModel
                    .showSignedOut()
            }
        }

        val state by
        marketViewModel
            .uiState
            .collectAsStateWithLifecycle()

        MarketListScreen(
            state =
                state,

            onModeChange = {
                    targetMode ->

                if (
                    targetMode !=
                    mode
                ) {
                    navController.navigate(
                        MarketListRoute.create(
                            mode =
                                targetMode,
                        )
                    )
                }
            },

            onMarketClick = {
                    listingId ->

                navController.navigate(
                    MarketDetailRoute.create(
                        listingId =
                            listingId,
                    )
                )
            },

            onLoadMore =
                marketViewModel::loadMore,

            onRetry =
                marketViewModel::retry,
        )
    }

    composable(
        route =
            MarketDetailRoute.route,

        arguments =
            listOf(
                navArgument(
                    MarketDetailRoute
                        .listingIdArg
                ) {
                    type =
                        NavType.StringType
                }
            ),
    ) { backStackEntry ->

        val listingId =
            backStackEntry
                .arguments
                ?.getString(
                    MarketDetailRoute
                        .listingIdArg
                )
                ?: return@composable

        val factory =
            remember(
                listingId,
                dependencies
                    .marketDetailRepository,
            ) {
                MarketDetailViewModelFactory(
                    listingId =
                        listingId,

                    repository =
                        dependencies
                            .marketDetailRepository,
                )
            }

        val marketViewModel:
                MarketDetailViewModel =
            viewModel(
                key =
                    "market-detail:$listingId",

                factory =
                    factory,
            )

        LaunchedEffect(
            dependencies
                .remoteAuthenticated,
        ) {
            if (
                dependencies
                    .remoteAuthenticated
            ) {
                marketViewModel.start()
            } else {
                marketViewModel
                    .showSignedOut()
            }
        }

        val state by
        marketViewModel
            .uiState
            .collectAsStateWithLifecycle()

        MarketDetailScreen(
            state =
                state,

            onRetry =
                marketViewModel::retry,

            onBuy = {
                /*
                 * 下一阶段：
                 *
                 * Native Market Order
                 * → quantity
                 * → shipping
                 * → create order
                 * → payment
                 */
            },
        )
    }

}