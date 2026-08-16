package top.role.android.app

import android.content.Context
import top.role.android.core.auth.AuthRepository
import top.role.android.core.convex.ConvexConfig
import top.role.android.core.convex.ConvexMobileClient
import top.role.android.core.network.ApiConfig
import top.role.android.core.network.auth.AuthApi
import top.role.android.core.storage.SessionStorage
import top.role.android.core.storage.NoteSyncCheckpointStorage
import top.role.android.feature.discover.DiscoverRepository
import top.role.android.feature.entity.EntityDetailRepository
import top.role.android.feature.discover.topic.TopicListRepository
import top.role.android.feature.topic.TopicDetailRepository
import top.role.android.feature.reading.DocDetailRepository
import top.role.android.feature.discover.space.SpaceListRepository
import top.role.android.feature.discover.content.ContentListRepository
import top.role.android.feature.slide.SlideDetailRepository
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.database.TopRoleDatabase
import top.role.android.core.data.note.NoteRemoteDataSource
import top.role.android.core.data.note.NoteRemoteMergeService
import top.role.android.core.data.note.NoteRefreshService
import top.role.android.core.data.note.NoteSyncCoordinator
import top.role.android.core.data.note.NotePushService

class AppContainer(
    context: Context,
) {

    private val appContext =
        context.applicationContext

    val sessionStorage =
        SessionStorage(
            context = appContext,
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

    val database =
        TopRoleDatabase.create(
            context = appContext,
        )

    val noteLocalRepository =
        NoteLocalRepository(
            dao = database.localNoteDao(),
        )

    val noteSyncCheckpointStorage =
        NoteSyncCheckpointStorage(
            context = appContext,
        )

    val noteRemoteDataSource =
        NoteRemoteDataSource(
            convexClient =
                convexClient,
        )

    val noteRemoteMergeService =
        NoteRemoteMergeService(
            noteLocalRepository =
                noteLocalRepository,
        )

    val noteRefreshService =
        NoteRefreshService(
            noteRemoteDataSource =
                noteRemoteDataSource,

            noteRemoteMergeService =
                noteRemoteMergeService,
        )

    val notePushService =
        NotePushService(
            noteLocalRepository =
                noteLocalRepository,

            noteRemoteDataSource =
                noteRemoteDataSource,
        )

    val noteSyncCoordinator =
        NoteSyncCoordinator(
            noteRefreshService =
                noteRefreshService,

            notePushService =
                notePushService,

            noteLocalRepository =
                noteLocalRepository,

            checkpointStorage =
                noteSyncCheckpointStorage,
        )

    val discoverRepository =
        DiscoverRepository(
            convexClient = convexClient,
        )

    val spaceListRepository =
        SpaceListRepository(
            convexClient = convexClient,
        )

    val topicListRepository =
        TopicListRepository(
            convexClient = convexClient
        )

    val contentListRepository =
        ContentListRepository(
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

    val slideDetailRepository =
        SlideDetailRepository(
            convexClient = convexClient,
        )

}