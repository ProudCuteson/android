package top.role.android.app

import android.content.Context
import top.role.android.core.auth.AuthRepository
import top.role.android.core.convex.ConvexConfig
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.network.ApiConfig
import top.role.android.core.network.auth.AuthApi
import top.role.android.core.storage.SessionStorage
import top.role.android.feature.discover.DiscoverRepository
import top.role.android.feature.entity.EntityDetailRepository
import top.role.android.feature.topic.TopicDetailRepository
import top.role.android.feature.reading.DocDetailRepository
import top.role.android.feature.discover.space.SpaceListRepository

class AppContainer(
    context: Context,
) {

    val sessionStorage =
        SessionStorage(
            context = context.applicationContext,
        )

    val authApi =
        AuthApi(
            baseUrl = ApiConfig.BASE_URL,
        )

    val authRepository =
        AuthRepository(
            authApi = authApi,
        )

    val convexClient =
        ConvexMobileClient(
            deploymentUrl =
                ConvexConfig.DEPLOYMENT_URL,
            sessionStorage =
                sessionStorage,
        )

    val discoverRepository =
        DiscoverRepository(
            convexClient = convexClient,
        )

    val spaceListRepository =
        SpaceListRepository(
            convexClient = convexClient,
        )

    val entityDetailRepository =
        EntityDetailRepository(
            convexClient = convexClient,
        )

    val topicDetailRepository =
        TopicDetailRepository(
            convexClient = convexClient,
        )

    val docDetailRepository =
        DocDetailRepository(
            convexClient = convexClient,
        )

}