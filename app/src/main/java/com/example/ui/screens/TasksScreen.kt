package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.GameTask
import com.example.ui.components.GameBottomNav
import com.example.ui.components.TopBalanceBar
import com.example.ui.theme.CaveCardBg
import com.example.ui.theme.CaveDarkBg
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import com.example.ui.theme.PinkGem
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GameUiState

@Composable
fun TasksScreen(
    uiState: GameUiState,
    onClaimTask: (String) -> Unit,
    onTabSelected: (CurrentScreen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    .testTag("tasks_back_btn")
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
                    text = "📋 Mining Quests & Tasks",
                    color = GoldLight,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Complete objectives to earn bonus Satoshis and Pink Gems!",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tasks List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.tasks, key = { it.id }) { task ->
                TaskItemCard(task = task, onClaim = { onClaimTask(task.id) })
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
fun TaskItemCard(
    task: GameTask,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CaveCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (task.isCompleted && !task.isClaimed) 2.dp else 1.dp,
                if (task.isCompleted && !task.isClaimed) GoldPrimary else Color(0xFF374151),
                RoundedCornerShape(14.dp)
            )
            .shadow(if (task.isCompleted && !task.isClaimed) 6.dp else 2.dp, RoundedCornerShape(14.dp))
            .testTag("task_card_${task.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = task.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar
                val progressFloat = (task.currentProgress.toFloat() / task.targetCount.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progressFloat },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (task.isCompleted) GreenHashRate else GoldPrimary,
                    trackColor = Color(0xFF263238)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${task.currentProgress}/${task.targetCount}",
                        color = Color.LightGray,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "+${task.rewardSats} Sats  💎 +${task.rewardGems}",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Action Button
            when {
                task.isClaimed -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1B5E20))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Done", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
                task.isCompleted -> {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("claim_task_btn_${task.id}")
                    ) {
                        Text("CLAIM", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2C324B))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("In Progress", color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
