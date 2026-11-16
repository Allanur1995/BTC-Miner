package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.auth.GoogleAuthManager
import com.example.data.auth.GoogleSignInOutcome
import com.example.model.LeaderboardEntry
import com.example.ui.components.GameBottomNav
import com.example.ui.components.TopBalanceBar
import com.example.ui.theme.CaveCardBg
import com.example.ui.theme.CaveCardStroke
import com.example.ui.theme.CaveDarkBg
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import com.example.ui.theme.TextMuted
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GameUiState
import com.example.viewmodel.LeaderboardSyncStatus
import com.example.viewmodel.LeaderboardUiState
import com.example.viewmodel.LeaderboardViewModel
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun LeaderboardScreen(
    gameUiState: GameUiState,
    authManager: GoogleAuthManager,
    onTabSelected: (CurrentScreen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by authManager.observeAuthState()
        .collectAsStateWithLifecycle(initialValue = authManager.currentUser)

    var isSigningIn by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (authManager.currentUser == null) {
            authManager.attemptAutoSignIn(context)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CaveDarkBg)
            .statusBarsPadding()
    ) {
        TopBalanceBar(
            btcBalance = gameUiState.btcBalance,
            pinkGems = gameUiState.pinkGems,
            blueDiamonds = gameUiState.blueDiamonds,
            onGemClick = { onTabSelected(CurrentScreen.SHOP) },
            onDiamondClick = { onTabSelected(CurrentScreen.SHOP) },
            onWalletClick = { onTabSelected(CurrentScreen.WALLET) }
        )

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF263238))
                    .testTag("leaderboard_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Mine",
                    tint = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Global Leaderboard",
                    color = GoldLight,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Top miners ranked by total mined Bitcoin",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            if (currentUser != null) {
                IconButton(
                    onClick = {
                        scope.launch {
                            authManager.signOut()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF263238))
                        .testTag("sign_out_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign out",
                        tint = Color(0xFFFF8A80)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp)
        ) {
            val user = currentUser
            if (user == null) {
                LeaderboardSignInGate(
                    gameUiState = gameUiState,
                    isSigningIn = isSigningIn,
                    errorMessage = signInError,
                    onSignInClick = {
                        if (!isSigningIn) {
                            scope.launch {
                                isSigningIn = true
                                signInError = null
                                when (val outcome = authManager.signInWithGoogle(context)) {
                                    is GoogleSignInOutcome.Success -> Unit
                                    is GoogleSignInOutcome.Cancelled -> Unit
                                    is GoogleSignInOutcome.Error -> signInError = outcome.message
                                }
                                isSigningIn = false
                            }
                        }
                    }
                )
            } else {
                key(user.uid) {
                    val leaderboardViewModel: LeaderboardViewModel = viewModel(
                        key = user.uid,
                        factory = LeaderboardViewModel.provideFactory(context, user.uid)
                    )
                    LeaderboardAuthenticatedContent(
                        user = user,
                        gameUiState = gameUiState,
                        viewModel = leaderboardViewModel
                    )
                }
            }
        }

        GameBottomNav(
            currentScreen = gameUiState.currentScreen,
            onTabSelected = onTabSelected,
            hasBonusAvailable = gameUiState.dailyBonuses.any { it.isCurrent && !it.isClaimed },
            hasTasksAvailable = gameUiState.tasks.any { it.isCompleted && !it.isClaimed }
        )
    }
}

@Composable
private fun LeaderboardSignInGate(
    gameUiState: GameUiState,
    isSigningIn: Boolean,
    errorMessage: String?,
    onSignInClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CaveCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, GoldPrimary.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .shadow(12.dp, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(GoldLight, GoldPrimary, GoldDark)
                            )
                        )
                        .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Global Trophy",
                        tint = Color(0xFF3E2723),
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Compete Worldwide",
                    color = GoldLight,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Sign in with Google to view live top miners, sync your mined BTC score, and claim your spot on the global rankings.",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141826))
                        .border(1.dp, CaveCardStroke, RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOUR LOCAL SCORE",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.8f", gameUiState.btcBalance)} BTC",
                            color = GoldPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "HASH RATE",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${String.format(Locale.US, "%.1f", gameUiState.totalHashRateGh)} GH/s",
                            color = GreenHashRate,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF8A80),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onSignInClick,
                    enabled = !isSigningIn,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color(0xFF2B1800)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("google_signin_btn")
                ) {
                    if (isSigningIn) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF2B1800),
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Signing in...",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    } else {
                        Text(
                            text = "Sign in with Google",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardAuthenticatedContent(
    user: FirebaseUser,
    gameUiState: GameUiState,
    viewModel: LeaderboardViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val playerName = remember(user) {
        user.displayName?.takeIf { it.isNotBlank() } ?: "Miner-${user.uid.take(5)}"
    }

    LaunchedEffect(user.uid) {
        viewModel.syncPlayerScore(
            displayName = playerName,
            totalBtcMined = gameUiState.btcBalance,
            hashRateGh = gameUiState.totalHashRateGh,
            activeMiners = gameUiState.activeMinerCount
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Player Sync Banner Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = CaveCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.8f", gameUiState.btcBalance)} BTC • ${
                            String.format(Locale.US, "%.1f", gameUiState.totalHashRateGh)
                        } GH/s",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    when (val status = syncStatus) {
                        is LeaderboardSyncStatus.Syncing -> {
                            Text(
                                text = "Syncing score to cloud...",
                                color = Color(0xFF80D8FF),
                                fontSize = 10.sp
                            )
                        }
                        is LeaderboardSyncStatus.Synced -> {
                            Text(
                                text = "Score synced live",
                                color = GreenHashRate,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        is LeaderboardSyncStatus.Failed -> {
                            Text(
                                text = status.message,
                                color = Color(0xFFFF8A80),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        LeaderboardSyncStatus.Idle -> Unit
                    }
                }

                Button(
                    onClick = {
                        viewModel.syncPlayerScore(
                            displayName = playerName,
                            totalBtcMined = gameUiState.btcBalance,
                            hashRateGh = gameUiState.totalHashRateGh,
                            activeMiners = gameUiState.activeMinerCount
                        )
                    },
                    enabled = syncStatus !is LeaderboardSyncStatus.Syncing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color(0xFF2B1800)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("sync_score_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Sync Score",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Sync",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (val state = uiState) {
            is LeaderboardUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = GoldPrimary)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Fetching top miners...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            is LeaderboardUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CaveCardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Could Not Load Leaderboard",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                color = Color(0xFFFF8A80),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedButton(
                                onClick = { viewModel.retry() },
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("leaderboard_retry_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry",
                                    tint = GoldPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Retry", color = GoldLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            is LeaderboardUiState.Success -> {
                if (state.entries.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(text = "🏆", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Ranked Miners Yet",
                                color = GoldLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap Sync above to claim the #1 spot on the global leaderboard!",
                                color = TextMuted,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    val myRankIndex = state.entries.indexOfFirst { it.userId == state.currentUserId }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("leaderboard_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        if (myRankIndex >= 0) {
                            item(key = "my_rank_summary") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(GoldPrimary.copy(alpha = 0.14f))
                                        .border(1.dp, GoldPrimary.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "Your Current Global Rank: #${myRankIndex + 1} of ${state.entries.size}",
                                        color = GoldLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }

                        itemsIndexed(
                            items = state.entries,
                            key = { _, entry -> entry.userId.ifBlank { entry.id } }
                        ) { index, entry ->
                            LeaderboardRankRow(
                                rank = index + 1,
                                entry = entry,
                                isCurrentUser = entry.userId == state.currentUserId
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRankRow(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentUser: Boolean
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD54F)
        2 -> Color(0xFFE0E0E0)
        3 -> Color(0xFFFFB74D)
        else -> Color(0xFF90A4AE)
    }

    val borderColor = when {
        isCurrentUser -> GoldPrimary
        rank == 1 -> Color(0xFFFFD54F).copy(alpha = 0.7f)
        rank == 2 -> Color(0xFFBDBDBD).copy(alpha = 0.5f)
        rank == 3 -> Color(0xFFFFB74D).copy(alpha = 0.5f)
        else -> CaveCardStroke
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) Color(0xFF262B44) else CaveCardBg
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isCurrentUser || rank <= 3) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(14.dp))
            .testTag("leaderboard_row_$rank")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rank Badge
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(rankColor.copy(alpha = if (rank <= 3) 0.22f else 0.12f))
                    .border(1.5.dp, rankColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (rank) {
                        1 -> "👑"
                        2 -> "🥈"
                        3 -> "🥉"
                        else -> "#$rank"
                    },
                    color = rankColor,
                    fontSize = if (rank <= 3) 18.sp else 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.displayName,
                        color = if (isCurrentUser) GoldLight else Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isCurrentUser) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(GoldPrimary)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "YOU",
                                color = Color(0xFF2B1800),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Hash rate",
                        tint = GreenHashRate,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${String.format(Locale.US, "%.1f", entry.hashRateGh)} GH/s • ${entry.activeMiners} miners",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(Locale.US, "%.8f", entry.totalBtcMined)} BTC",
                    color = GoldPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${entry.totalSatsMined} Sats",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
