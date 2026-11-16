package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.WalletTransaction
import com.example.ui.components.GameBottomNav
import com.example.ui.components.TopBalanceBar
import com.example.ui.theme.CaveCardBg
import com.example.ui.theme.CaveDarkBg
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import com.example.viewmodel.CurrentScreen
import com.example.viewmodel.GameUiState
import java.util.Locale

@Composable
fun WalletScreen(
    uiState: GameUiState,
    onWithdraw: (Double, String) -> Boolean,
    onTabSelected: (CurrentScreen) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showWithdrawModal by remember { mutableStateOf(false) }
    var showDepositModal by remember { mutableStateOf(false) }

    val btcUsdPrice = 68450.0
    val usdValue = uiState.btcBalance * btcUsdPrice
    val satoshis = (uiState.btcBalance * 100_000_000).toLong()
    val walletAddress = "bc1qminer7x448kspwzq978v29a430xmld9q2"

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
            onWalletClick = {}
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
                    .testTag("wallet_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Mine",
                    tint = GoldPrimary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "👛 Crypto Miner Wallet",
                color = GoldLight,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Main Portfolio Balance Card
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF181C2B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(GoldLight, GoldPrimary, GoldDark)),
                            RoundedCornerShape(18.dp)
                        )
                        .shadow(8.dp, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "TOTAL MINED ASSETS",
                            color = GoldPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${String.format(Locale.US, "%.10f", uiState.btcBalance)} BTC",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "≈ $satoshis Satoshis",
                                color = GoldLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "($${String.format(Locale.US, "%.4f", usdValue)} USD)",
                                color = GreenHashRate,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons: Send/Withdraw & Receive/Deposit
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showWithdrawModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("btn_open_withdraw")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Withdraw",
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Withdraw", color = Color.Black, fontWeight = FontWeight.Black)
                            }

                            Button(
                                onClick = { showDepositModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A3147)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("btn_open_deposit")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.CallReceived,
                                    contentDescription = "Deposit",
                                    tint = GoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Deposit", color = GoldLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Wallet Address Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CaveCardBg),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF374151), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Address",
                            tint = GoldPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Your Mining Address", color = Color.Gray, fontSize = 10.sp)
                            Text(
                                text = walletAddress,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(walletAddress))
                                Toast.makeText(context, "Wallet address copied!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = GoldPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Mining Telemetry Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CaveCardBg),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF374151), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "⚡ Real-Time Mining Telemetry",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Current Hashrate:", color = Color.LightGray, fontSize = 12.sp)
                            Text(text = "${uiState.totalHashRateGh.toInt()} GH/s", color = GreenHashRate, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Tap Multiplier:", color = Color.LightGray, fontSize = 12.sp)
                            Text(text = "${uiState.tapPowerSats} Sats/tap", color = GoldPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Estimated 24h Payout:", color = Color.LightGray, fontSize = 12.sp)
                            val estBtc = uiState.totalHashRateGh * 0.000000000001 * 86400
                            Text(text = "${String.format(Locale.US, "%.8f", estBtc)} BTC", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Recent Transactions
            item {
                Text(
                    text = "Recent Transactions Ledger",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(uiState.transactions, key = { it.id }) { tx ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131724)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = tx.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = tx.timeAgo, color = Color.Gray, fontSize = 10.sp)
                        }
                        Text(
                            text = (if (tx.isPositive) "+" else "-") + String.format(Locale.US, "%.8f", tx.amountBtc) + " BTC",
                            color = if (tx.isPositive) GreenHashRate else Color(0xFFEF5350),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
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

    // Withdrawal Modal Dialog
    if (showWithdrawModal) {
        Dialog(onDismissRequest = { showWithdrawModal = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CaveCardBg),
                modifier = Modifier.fillMaxWidth().border(2.dp, GoldPrimary, RoundedCornerShape(18.dp))
            ) {
                var amountText by remember { mutableStateOf("") }
                var destAddress by remember { mutableStateOf("") }
                var errorMsg by remember { mutableStateOf<String?>(null) }

                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "🚀 Withdraw Bitcoin", color = GoldLight, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Available: ${String.format(Locale.US, "%.10f", uiState.btcBalance)} BTC", color = Color.LightGray, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it; errorMsg = null },
                        label = { Text("Amount (BTC)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_amount_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color.Gray
                        ),
                        trailingIcon = {
                            Text(
                                text = "MAX",
                                color = GoldPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { amountText = String.format(Locale.US, "%.8f", uiState.btcBalance) }
                                    .padding(8.dp)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = destAddress,
                        onValueChange = { destAddress = it; errorMsg = null },
                        label = { Text("Destination BTC Address") },
                        placeholder = { Text("bc1q...") },
                        modifier = Modifier.fillMaxWidth().testTag("withdraw_address_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )

                    errorMsg?.let {
                        Text(text = it, color = Color(0xFFEF5350), fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showWithdrawModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                val amt = amountText.toDoubleOrNull()
                                if (amt == null || amt <= 0) {
                                    errorMsg = "Please enter a valid BTC amount"
                                } else if (amt > uiState.btcBalance) {
                                    errorMsg = "Amount exceeds current balance"
                                } else if (destAddress.length < 8) {
                                    errorMsg = "Enter a valid destination wallet address"
                                } else {
                                    val success = onWithdraw(amt, destAddress)
                                    if (success) {
                                        showWithdrawModal = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier.weight(1f).testTag("btn_confirm_withdraw")
                        ) {
                            Text("Confirm", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Deposit Modal Dialog
    if (showDepositModal) {
        Dialog(onDismissRequest = { showDepositModal = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CaveCardBg),
                modifier = Modifier.fillMaxWidth().border(2.dp, GoldPrimary, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "📥 Deposit Bitcoin", color = GoldLight, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Deposit QR",
                            tint = Color.Black,
                            modifier = Modifier.size(140.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = walletAddress,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(walletAddress))
                            Toast.makeText(context, "Address copied to clipboard!", Toast.LENGTH_SHORT).show()
                            showDepositModal = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Copy Address & Close", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
