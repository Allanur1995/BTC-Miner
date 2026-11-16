package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Miner
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import java.util.Locale

fun formatCountdown(seconds: Long): String {
    if (seconds <= 0) return "Expired"
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
}

@Composable
fun RecruitCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(105.dp)
            .height(130.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF5D3A1A), Color(0xFF432810))
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GoldLight, GoldPrimary)
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .testTag("recruit_card_btn")
            .padding(6.dp)
    ) {
        // Video badge in top left corner (like in the screenshot)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .size(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(GoldPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = "Video bonus",
                tint = Color(0xFF4E2600),
                modifier = Modifier.size(16.dp)
            )
        }

        // Center content: crossed pickaxe + recruit text
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Icon(
                imageVector = Icons.Default.Construction,
                contentDescription = "Recruit",
                tint = GoldPrimary,
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Recruit",
                color = GoldPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
fun ActiveMinerCard(
    miner: Miner,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(105.dp)
            .height(130.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF3E7C9)) // Warm parchment card background
            .border(2.dp, Color(0xFFBCAAA4), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("miner_card_${miner.id}")
    ) {
        // Top Speed Header (Grey curved pill tab)
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(RoundedCornerShape(bottomEnd = 8.dp))
                .background(Color(0xFF4A5568))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${miner.hashRateGh.toInt()}Gh/s",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Miner Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE2D1B3))
                    .border(1.dp, GoldBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_miner_intern),
                    contentDescription = miner.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp)
                )
            }

            // NEW Ribbon
            if (miner.isNew) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFE53935))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "NEW",
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(2.dp))
            }

            // Name
            Text(
                text = miner.name,
                color = Color(0xFF4E342E),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Timer Clock Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEFEBE9))
                    .border(1.dp, Color(0xFFD7CCC8), RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Time remaining",
                    tint = Color(0xFF8D6E63),
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = formatCountdown(miner.remainingSeconds),
                    color = Color(0xFF3E2723),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun MinersTray(
    miners: List<Miner>,
    onRecruitClick: () -> Unit,
    onMinerClick: (Miner) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMiners = miners.filter { it.isActive }
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recruit Card
        RecruitCard(onClick = onRecruitClick)

        // Active Miner Cards
        activeMiners.forEach { miner ->
            ActiveMinerCard(
                miner = miner,
                onClick = { onMinerClick(miner) }
            )
        }
    }
}
