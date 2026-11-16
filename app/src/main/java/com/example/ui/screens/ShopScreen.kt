package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShopCategory
import com.example.model.ShopItem
import com.example.ui.components.GameBottomNav
import com.example.ui.components.TopBalanceBar
import com.example.ui.theme.BlueDiamond
import com.example.ui.theme.CaveCardBg
import com.example.ui.theme.CaveDarkBg
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import com.example.ui.theme.PinkGem
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GameUiState

@Composable
fun ShopScreen(
    uiState: GameUiState,
    onBuyItem: (String) -> Unit,
    onWatchAdForGems: () -> Unit = {},
    onWatchAdForBoost: () -> Unit = {},
    onTabSelected: (CurrentScreen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(ShopCategory.PICKAXE) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CaveDarkBg)
            .statusBarsPadding()
    ) {
        // Top Bar
        TopBalanceBar(
            btcBalance = uiState.btcBalance,
            pinkGems = uiState.pinkGems,
            blueDiamonds = uiState.blueDiamonds,
            onGemClick = {},
            onDiamondClick = {},
            onWalletClick = { onTabSelected(CurrentScreen.WALLET) }
        )

        // Screen Title with Back Button
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
                    .testTag("shop_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Mine",
                    tint = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "🎪 Mining Equipment & Shop",
                color = GoldLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Category Tabs
        val categories = listOf(
            ShopCategory.PICKAXE to "⛏️ Pickaxes",
            ShopCategory.HARDWARE to "🖥️ Hardware",
            ShopCategory.BOOST to "⚡ Boosts",
            ShopCategory.GEMS to "💎 Gems"
        )
        val selectedIndex = categories.indexOfFirst { it.first == selectedCategory }

        TabRow(
            selectedTabIndex = selectedIndex,
            containerColor = Color(0xFF161B29),
            contentColor = GoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    color = GoldPrimary,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            categories.forEachIndexed { index, pair ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { selectedCategory = pair.first },
                    text = {
                        Text(
                            text = pair.second,
                            fontSize = 12.sp,
                            fontWeight = if (selectedIndex == index) FontWeight.Black else FontWeight.Bold,
                            color = if (selectedIndex == index) GoldLight else Color.LightGray
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Free Rewarded Ad Bonus Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onWatchAdForGems,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, PinkGem, RoundedCornerShape(10.dp))
                    .testTag("btn_watch_ad_gems")
            ) {
                Text(
                    text = "📺 Ad: +25 💎",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Button(
                onClick = onWatchAdForBoost,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .border(1.dp, GreenHashRate, RoundedCornerShape(10.dp))
                    .testTag("btn_watch_ad_boost")
            ) {
                Text(
                    text = "📺 Ad: 2x Speed 5m",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // Items List
        val filteredItems = uiState.shopItems.filter { it.category == selectedCategory }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                ShopItemCard(
                    item = item,
                    onBuy = { onBuyItem(item.id) }
                )
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
fun ShopItemCard(
    item: ShopItem,
    onBuy: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CaveCardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .testTag("shop_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Icon / Emoji Box
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C324B))
                    .border(1.dp, GoldPrimary, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val emoji = when (item.category) {
                    ShopCategory.PICKAXE -> "⛏️"
                    ShopCategory.HARDWARE -> "🖥️"
                    ShopCategory.BOOST -> "⚡"
                    ShopCategory.GEMS -> "💎"
                }
                Text(text = emoji, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (item.currentLevel > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldPrimary)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "LVL ${item.currentLevel}",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    color = Color(0xFFB0BEC5),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.bonusDescription,
                    color = GreenHashRate,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Buy Button
            Button(
                onClick = onBuy,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("buy_btn_${item.id}")
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when {
                        item.costDiamonds > 0 -> {
                            Text(text = "💠 ${item.costDiamonds}", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        item.costGems > 0 -> {
                            Text(text = "💎 ${item.costGems}", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                        else -> {
                            Text(text = "₿ ${item.costSats} Sats", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 12.sp)
                        }
                    }
                    Text(text = "BUY", color = Color(0xFF3E2723), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }
        }
    }
}
