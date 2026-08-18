package top.role.android.feature.main.navigation

import top.role.android.core.data.image.ImageRepository
import top.role.android.feature.discover.DiscoverRepository
import top.role.android.feature.discover.content.ContentListRepository
import top.role.android.feature.discover.space.SpaceListRepository
import top.role.android.feature.discover.topic.TopicListRepository
import top.role.android.feature.entity.EntityDetailRepository
import top.role.android.feature.reading.DocDetailRepository
import top.role.android.feature.slide.SlideDetailRepository
import top.role.android.feature.topic.TopicDetailRepository
import top.role.android.core.data.note.NoteLocalRepository
import top.role.android.core.data.image.NoteImageService
import top.role.android.core.data.note.ai.NoteAiRemoteDataSource

data class MainNavigationDependencies(
    val localOwnerUserId: String,
    val remoteAuthenticated: Boolean,
    val noteLocalRepository: NoteLocalRepository,
    val noteAiRemoteDataSource:NoteAiRemoteDataSource,
    val noteImageService: NoteImageService,
    val imageRepository: ImageRepository,
    val discoverRepository: DiscoverRepository,
    val spaceListRepository: SpaceListRepository,
    val topicListRepository: TopicListRepository,
    val contentListRepository: ContentListRepository,
    val entityDetailRepository: EntityDetailRepository,
    val topicDetailRepository: TopicDetailRepository,
    val docDetailRepository: DocDetailRepository,
    val slideDetailRepository: SlideDetailRepository,
)