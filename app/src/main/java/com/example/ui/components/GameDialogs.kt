package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ads.AdMobConfig
import com.example.ui.theme.CaveCardBg
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import com.example.ui.theme.PinkGem
import com.example.ui.theme.PurpleLilith
import com.example.ui.theme.WoodBrown
import com.example.ui.theme.WoodBrownDark
import com.example.viewmodel.GameDialogType

@Composable
fun DialogWrapper(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CaveCardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, GoldPrimary, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        color = GoldLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp).testTag("dialog_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                content()
            }
        }
    }
}

@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    totalTaps: Int,
    totalHashRate: Double,
    adsWatchedCount: Int = 0,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onResetGame: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogWrapper(title = "⚙️ Game Settings", onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Sound FX
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Sound",
                        tint = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Sound Effects", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = { onToggleSound() },
                    colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary)
                )
            }

            // Haptics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptics",
                        tint = GoldPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = "Haptic Vibration", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Switch(
                    checked = hapticsEnabled,
                    onCheckedChange = { onToggleHaptics() },
                    colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Stats box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF131722))
                    .padding(12.dp)
            ) {
                Column {
                    Text(text = "📊 Telemetry Stats", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Total Gold Taps: $totalTaps", color = Color.White, fontSize = 13.sp)
                    Text(text = "Mining Speed: ${totalHashRate.toInt()} GH/s", color = GreenHashRate, fontSize = 13.sp)
                    Text(text = "Rewarded Ads Watched: $adsWatchedCount", color = GoldPrimary, fontSize = 13.sp)
                    Text(
                        text = if (AdMobConfig.isTestMode) "AdMob: Test Ads Active" else "AdMob: Live Production Ads",
                        color = if (AdMobConfig.isTestMode) Color(0xFFFFB74D) else GreenHashRate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Reset Game Button
            Button(
                onClick = onResetGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                modifier = Modifier.fillMaxWidth().testTag("btn_reset_game")
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = "Reset", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Reset Game Progress", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FaqDialog(onDismiss: () -> Unit) {
    DialogWrapper(title = "📜 Miner's Guide & FAQ", onDismiss = onDismiss) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FaqItem(
                q = "How do I earn Bitcoin?",
                a = "Tap the central golden boulder to mine Satoshi manually! Recruit miners in the Miners tab for passive automated mining 24/7."
            )
            FaqItem(
                q = "What does GH/s mean?",
                a = "Gigahashes per second represents your computational mining speed. Higher GH/s solves blocks faster and yields more Bitcoin continually."
            )
            FaqItem(
                q = "Where do I get Pink Gems?",
                a = "Pink gems drop randomly while tapping the gold pile, and can be claimed in Daily Bonuses or Task achievements!"
            )
        }
    }
}

@Composable
fun FaqItem(q: String, a: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF161A29))
            .padding(10.dp)
    ) {
        Text(text = q, color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = a, color = Color(0xFFCFD8DC), fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
fun FreeMinerDialog(
    onClaim: () -> Unit,
    onDismiss: () -> Unit
) {
    DialogWrapper(title = "🎁 Lucky Miner Giveaway", onDismiss = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "🎰", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Claim your Free Miner Shift!",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Activates a dedicated 8-hour mining shift for +5 GH/s completely free!",
                color = Color(0xFFB0BEC5),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onClaim,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.fillMaxWidth().testTag("btn_claim_free_miner")
            ) {
                Text(text = "📺 Watch Ad & Claim Free Miner!", color = Color.Black, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PremiumPackDialog(
    onDismiss: () -> Unit
) {
    DialogWrapper(title = "👑 Premium Miner Bundle", onDismiss = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "💎", fontSize = 46.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Overclock Founder's Pack",
                color = GoldLight,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "• +50 GH/s Permanent Boost\n• 100 Pink Gems\n• 20 Blue Diamonds\n• Exclusive Gold Pickaxe Skin", color = Color.White, fontSize = 13.sp, lineHeight = 20.sp)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GoldDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Unlock in Shop", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun LilithEventDialog(
    onDismiss: () -> Unit
) {
    DialogWrapper(title = "😈 Lilith Descends Event", onDismiss = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "🔥", fontSize = 46.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Demonic Hash Power Unleashed!",
                color = PurpleLilith,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "The Lord of the Deep Mine has awakened. Hire the Lilith Overlord Miner in the Miners roster to generate an astronomical 1,000 GH/s!",
                color = Color(0xFFCFD8DC),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PurpleLilith),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "View in Miners Roster", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
