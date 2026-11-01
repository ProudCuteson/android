package top.role.android.feature.note.adoption

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import top.role.android.core.data.note.NoteAdoptionLocalRepository
import top.role.android.core.database.note.LocalNoteEntity
import androidx.compose.runtime.LaunchedEffect
import top.role.android.core.auth.AuthState
import top.role.android.ui.auth.AuthStatusContent

@Composable
fun NoteAdoptionScreen(
    guestLocalOwnerId: String,
    authState: AuthState,
    repository: NoteAdoptionLocalRepository,
    onLogin: () -> Unit,
    onRetry: () -> Unit,
    onReSignIn: () -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: NoteAdoptionViewModel = viewModel(
        key = "note-adoption:$guestLocalOwnerId",
        factory = NoteAdoptionViewModelFactory(
            guestLocalOwnerId = guestLocalOwnerId,
            repository = repository,
            initialAuthState = authState,
        ),
    )

    LaunchedEffect(authState) {
        viewModel.setAuthState(authState)
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AuthStatusContent(
        authState = state.authState,
        onLogin = onLogin,
        onRetry = onRetry,
        onReSignIn = onReSignIn,
    ) { _ ->

        val claimedCount = state.claimedCount

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                    )
                }

                Text(
                    text = "认领本地记录",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Description
            Text(
                text = "将此前在本机以访客身份创建的记录，归入当前登录账户。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(
                    top = 8.dp,
                    bottom = 20.dp,
                ),
            )

            HorizontalDivider()

            when {
                state.loading -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                claimedCount != null -> {
                    ClaimCompletedContent(
                        claimedCount = claimedCount,
                        modifier = Modifier.weight(1f),
                        onBack = onBack,
                    )
                }

                state.notes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )

                        Text(
                            text = "暂无待认领记录",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 12.dp),
                        )

                        Text(
                            text = "本机没有符合认领条件的访客记录。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "待认领记录（${state.unclaimedCount}）",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 12.dp),
                        )

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(
                                items = state.notes,
                                key = { it.localKey },
                            ) { note ->
                                AdoptionNoteRow(note)
                            }
                        }
                    }

                    state.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    Button(
                        onClick = viewModel::claimAll,
                        enabled = state.canClaim,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                    ) {
                        if (state.isClaiming) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(end = 8.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }

                        Text(
                            text = if (state.isClaiming) {
                                "正在认领..."
                            } else {
                                "认领全部 ${state.unclaimedCount} 条记录"
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AdoptionNoteRow(
    note: LocalNoteEntity,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = note.title
                    .takeIf { it.isNotBlank() }
                    ?: note.text
                        .takeIf { it.isNotBlank() }
                        ?.take(40)
                    ?: "无标题",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (note.text.isNotBlank()) {
                Text(
                    text = note.text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            note.dayKey?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ClaimCompletedContent(
    claimedCount: Int,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "认领完成",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )

        Text(
            text = "成功认领 $claimedCount 条记录",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )

        Spacer(modifier = Modifier.weight(0.2f))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("完成")
        }
    }
}