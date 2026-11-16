package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Miner
import com.example.ui.components.GameBottomNav
import com.example.ui.components.TopBalanceBar
import com.example.ui.components.formatCountdown
import com.example.ui.theme.CaveCardBg
import com.example.ui.theme.CaveDarkBg
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GameUiState

@Composable
fun MinersScreen(
    uiState: GameUiState,
    onHireMiner: (String) -> Unit,
    onTabSelected: (CurrentScreen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCount = uiState.activeMinerCount
    val totalMiners = uiState.miners.size

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CaveDarkBg)
            .statusBarsPadding()
    ) {
        // Top Balance Bar
        TopBalanceBar(
            btcBalance = uiState.btcBalance,
            pinkGems = uiState.pinkGems,
            blueDiamonds = uiState.blueDiamonds,
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
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF263238))
                    .testTag("miners_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Mine",
                    tint = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "⛑️ Miner Management Roster",
                    color = GoldLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "$activeCount of $totalMiners miners currently actively working shifts",
                    color = GreenHashRate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Miners Roster List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.miners, key = { it.id }) { miner ->
                MinerRosterCard(miner = miner, onHire = { onHireMiner(miner.id) })
            }
        }

        // Bottom Navigation Bar
        GameBottomNav(
            currentScreen = uiState.currentScreen,
            onTabSelected = onTabSelected,
            hasBonusAvailable = uiState.dailyBonuses.any { it.isCurrent && !it.isClaimed },
            hasTasksAvailable = uiState.tasks.any { it.isCompleted && !it.isClaimed }
        )
    }
}

@Composable
fun MinerRosterCard(
    miner: Miner,
    onHire: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CaveCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (miner.isActive) 2.dp else 1.dp,
                if (miner.isActive) GreenHashRate else Color(0xFF374151),
                RoundedCornerShape(14.dp)
            )
            .shadow(if (miner.isActive) 6.dp else 2.dp, RoundedCornerShape(14.dp))
            .testTag("miner_roster_${miner.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2D1B3))
                    .border(1.5.dp, GoldBorder, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_miner_intern),
                    contentDescription = miner.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(58.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = miner.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (miner.isActive) Color(0xFF1B5E20) else Color(0xFF374151))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = if (miner.isActive) "ACTIVE" else "OFF DUTY",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${miner.hashRateGh.toInt()} GH/s  •  ${miner.shiftDurationSeconds / 3600}h Shift",
                    color = GreenHashRate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = miner.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 10.sp,
                    lineHeight = 13.sp
                )

                if (miner.isActive) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Shift",
                            tint = GoldPrimary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Shift Ends In: ${formatCountdown(miner.remainingSeconds)}",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Hire / Extend Action Button
            Button(
                onClick = onHire,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (miner.isActive) Color(0xFF455A64) else GoldPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("hire_btn_${miner.id}")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (miner.isActive) "EXTEND" else "HIRE",
                        color = if (miner.isActive) Color.White else Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp
                    )
                    if (miner.hireCostGems > 0) {
                        Text(
                            text = "💎 ${miner.hireCostGems}",
                            color = if (miner.isActive) GoldLight else Color(0xFF3E2723),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (miner.hireCostSats > 0) {
                        Text(
                            text = "₿ ${miner.hireCostSats / 1000}k",
                            color = if (miner.isActive) GoldLight else Color(0xFF3E2723),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "FREE",
                            color = if (miner.isActive) GoldLight else Color(0xFF3E2723),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
