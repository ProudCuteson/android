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
import top.role.android.core.payment.WechatPayLauncher
import top.role.android.feature.account.AccountRepository
import top.role.android.feature.chat.conversationlist.ChatConversationListRepository
import top.role.android.feature.chat.conversation.ChatConversationRepository
import top.role.android.feature.chat.friends.ChatFriendsRepository
import top.role.android.feature.chat.group.ChatGroupRepository
import top.role.android.feature.engage.EngageRepository
import top.role.android.feature.survey.SurveyListRepository
import top.role.android.feature.survey.detail.SurveyDetailRepository
import top.role.android.feature.bounty.BountyListRepository
import top.role.android.feature.bounty.detail.BountyDetailRepository
import top.role.android.feature.market.MarketListRepository
import top.role.android.feature.market.detail.MarketDetailRepository
import top.role.android.feature.market.order.MarketOrderListRepository
import top.role.android.feature.membership.MembershipRepository

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
    val chatConversationListRepository: ChatConversationListRepository,
    val chatConversationRepository: ChatConversationRepository,
    val chatFriendsRepository: ChatFriendsRepository,
    val chatGroupRepository: ChatGroupRepository,
    val engageRepository: EngageRepository,
    val surveyListRepository: SurveyListRepository,
    val surveyDetailRepository: SurveyDetailRepository,
    val bountyListRepository: BountyListRepository,
    val bountyDetailRepository: BountyDetailRepository,
    val marketListRepository: MarketListRepository,
    val marketDetailRepository: MarketDetailRepository,
    val membershipRepository: MembershipRepository,
    val wechatPayLauncher: WechatPayLauncher,
    val marketOrderListRepository: MarketOrderListRepository,
    val accountRepository: AccountRepository,
)