package top.role.android.feature.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import top.role.android.feature.banner.BannerRepository
import top.role.android.feature.banner.BannerScreen

@Composable
fun QuickStartScreen(
    bannerRepository: BannerRepository,
    onBannerClick: (String) -> Unit,
    onOpenSurvey: () -> Unit,
    onOpenBounty: () -> Unit,
    onOpenCredits: () -> Unit,
    onOpenMarket: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        BannerScreen(
            repository = bannerRepository,
            onClick = onBannerClick,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            QuickStartCard(
                modifier = Modifier.weight(1f),
                title = "问卷",
                description = "测评与调查",
                icon = Icons.Outlined.Assignment,
                onClick = onOpenSurvey,
            )

            QuickStartCard(
                modifier = Modifier.weight(1f),
                title = "赏金任务",
                description = "参与并获取奖励",
                icon = Icons.Outlined.EmojiEvents,
                onClick = onOpenBounty,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            QuickStartCard(
                modifier = Modifier.weight(1f),
                title = "Credits",
                description = "购买与余额",
                icon = Icons.Outlined.Wallet,
                onClick = onOpenCredits,
            )

            QuickStartCard(
                modifier = Modifier.weight(1f),
                title = "市场",
                description = "极致单品",
                icon = Icons.Outlined.ShoppingBag,
                onClick = onOpenMarket,
            )
        }
    }
}

@Composable
private fun QuickStartCard(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxSize()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
