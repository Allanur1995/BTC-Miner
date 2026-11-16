package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DailyProductivityPoint
import com.example.ui.theme.CaveCardBg
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.GreenHashRate
import java.util.Locale

enum class ProductivityMetric(
    val label: String,
    val unit: String,
    val accentColor: Color,
    val secondaryColor: Color
) {
    SATS("Sats", "Sats", GoldPrimary, GoldDark),
    HASHRATE("GH/s", "GH/s", GreenHashRate, Color(0xFF1B5E20)),
    TAPS("Taps", "Taps", Color(0xFF4FC3F7), Color(0xFF0277BD))
}

/**
 * Interactive 7-day mining productivity chart combining rounded gradient bars,
 * a trend polyline with nodes, and tap-to-inspect day details.
 */
@Composable
fun MiningProductivityChart(
    points: List<DailyProductivityPoint>,
    modifier: Modifier = Modifier
) {
    val displayPoints = remember(points) {
        if (points.size == 7) points else emptyList()
    }
    var selectedMetric by remember { mutableStateOf(ProductivityMetric.SATS) }
    var selectedIndex by remember(displayPoints.size) {
        mutableIntStateOf((displayPoints.size - 1).coerceAtLeast(0))
    }

    val values: List<Double> = remember(displayPoints, selectedMetric) {
        displayPoints.map { point ->
            when (selectedMetric) {
                ProductivityMetric.SATS -> point.satsMined
                ProductivityMetric.HASHRATE -> point.peakHashRateGh
                ProductivityMetric.TAPS -> point.tapsCount.toDouble()
            }
        }
    }

    val maxValue = remember(values) {
        (values.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CaveCardBg),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, GoldPrimary.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .testTag("productivity_chart_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header + Metric Toggle Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "📊 7-Day Mining Productivity",
                        color = GoldLight,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Tap any day to inspect output",
                        color = Color(0xFF90A4AE),
                        fontSize = 11.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ProductivityMetric.entries.forEach { metric ->
                        val isSelected = selectedMetric == metric
                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) metric.accentColor.copy(alpha = 0.22f)
                                    else Color(0xFF141824)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) metric.accentColor else Color(0xFF2D3748),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedMetric = metric }
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                                .testTag("metric_tab_${metric.name.lowercase(Locale.US)}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = metric.label,
                                color = if (isSelected) metric.accentColor else Color(0xFFB0BEC5),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (displayPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF121622)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Start mining to populate 7-day productivity trends",
                        color = Color(0xFF90A4AE),
                        fontSize = 12.sp
                    )
                }
            } else {
                // Selected Day Callout Strip
                val clampedIndex = selectedIndex.coerceIn(0, displayPoints.lastIndex)
                val selectedPoint = displayPoints[clampedIndex]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF121622))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedPoint.dayLabel} (${selectedPoint.dateKey.takeLast(5)})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatMetricValue(selectedPoint.satsMined)} Sats • ${selectedPoint.peakHashRateGh.toInt()} GH/s • ${selectedPoint.tapsCount} taps",
                        color = selectedMetric.accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Animated normalized fractions for all 7 bars
                val animatedFractions = values.mapIndexed { idx, value ->
                    val target = (value / maxValue).toFloat().coerceIn(0f, 1f)
                    val anim by animateFloatAsState(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
                        label = "bar_$idx"
                    )
                    anim
                }

                // Chart Canvas + Interactive Column Hit Targets
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(152.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 10.dp, bottom = 24.dp, start = 4.dp, end = 4.dp)
                    ) {
                        val chartWidth = size.width
                        val chartHeight = size.height
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                        // 3 horizontal reference grid lines (0%, 50%, 100%)
                        for (step in 0..2) {
                            val y = chartHeight * (step / 2f)
                            drawLine(
                                color = Color(0xFF2B3447),
                                start = Offset(0f, y),
                                end = Offset(chartWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = if (step < 2) dashEffect else null
                            )
                        }

                        val slotWidth = chartWidth / 7f
                        val barWidth = (slotWidth * 0.46f).coerceAtLeast(10.dp.toPx())
                        val corner = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                        val nodeCenters = ArrayList<Offset>(7)

                        for (i in 0 until 7) {
                            val fraction = animatedFractions[i]
                            val barHeight = (chartHeight * fraction).coerceAtLeast(
                                if (values[i] > 0.0) 6.dp.toPx() else 2.dp.toPx()
                            )
                            val centerX = slotWidth * i + slotWidth / 2f
                            val barLeft = centerX - barWidth / 2f
                            val barTop = chartHeight - barHeight
                            val isSelected = (i == clampedIndex)

                            // Selected column subtle backdrop highlight
                            if (isSelected) {
                                drawRoundRect(
                                    color = selectedMetric.accentColor.copy(alpha = 0.08f),
                                    topLeft = Offset(slotWidth * i + 2.dp.toPx(), 0f),
                                    size = Size(slotWidth - 4.dp.toPx(), chartHeight),
                                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                                )
                            }

                            // Gradient Bar
                            val barBrush = Brush.verticalGradient(
                                colors = if (isSelected) {
                                    listOf(Color.White, selectedMetric.accentColor, selectedMetric.secondaryColor)
                                } else {
                                    listOf(
                                        selectedMetric.accentColor.copy(alpha = 0.85f),
                                        selectedMetric.secondaryColor.copy(alpha = 0.75f)
                                    )
                                },
                                startY = barTop,
                                endY = chartHeight
                            )
                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(barLeft, barTop),
                                size = Size(barWidth, barHeight),
                                cornerRadius = corner
                            )

                            nodeCenters.add(Offset(centerX, barTop))
                        }

                        // Trend line connecting top of each day's bar
                        if (nodeCenters.size >= 2) {
                            val trendPath = Path().apply {
                                moveTo(nodeCenters[0].x, nodeCenters[0].y)
                                for (i in 1 until nodeCenters.size) {
                                    lineTo(nodeCenters[i].x, nodeCenters[i].y)
                                }
                            }
                            drawPath(
                                path = trendPath,
                                color = Color.White.copy(alpha = 0.65f),
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        // Vertex dots on trend line
                        nodeCenters.forEachIndexed { idx, center ->
                            val isSelected = (idx == clampedIndex)
                            drawCircle(
                                color = if (isSelected) Color.White else selectedMetric.accentColor,
                                radius = if (isSelected) 4.5.dp.toPx() else 3.dp.toPx(),
                                center = center
                            )
                            if (isSelected) {
                                drawCircle(
                                    color = selectedMetric.accentColor,
                                    radius = 7.dp.toPx(),
                                    center = center,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                            }
                        }
                    }

                    // Clickable columns + X-axis day labels
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        displayPoints.forEachIndexed { idx, point ->
                            val isSelected = (idx == clampedIndex)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .clickable { selectedIndex = idx }
                                    .padding(bottom = 2.dp)
                                    .testTag("productivity_bar_$idx"),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Text(
                                    text = point.dayLabel,
                                    color = when {
                                        isSelected -> selectedMetric.accentColor
                                        point.isToday -> GoldLight
                                        else -> Color(0xFF90A4AE)
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected || point.isToday) FontWeight.Black else FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 7-Day Summary Metrics Footer
                val total7d = values.sum()
                val avg7d = total7d / 7.0
                val peak7d = values.maxOrNull() ?: 0.0
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF141824))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryStatColumn(
                        title = if (selectedMetric == ProductivityMetric.HASHRATE) "Active Days" else "7d Total",
                        value = if (selectedMetric == ProductivityMetric.HASHRATE) {
                            "${values.count { it > 0.0 }}/7"
                        } else {
                            "${formatMetricValue(total7d)} ${selectedMetric.unit}"
                        },
                        valueColor = selectedMetric.accentColor
                    )
                    SummaryStatColumn(
                        title = "Daily Avg",
                        value = "${formatMetricValue(avg7d)} ${selectedMetric.unit}",
                        valueColor = Color.White
                    )
                    SummaryStatColumn(
                        title = "7d Peak",
                        value = "${formatMetricValue(peak7d)} ${selectedMetric.unit}",
                        valueColor = GoldLight
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatColumn(
    title: String,
    value: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = Color(0xFF90A4AE), fontSize = 10.sp)
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Monospace
        )
    }
}

private fun formatMetricValue(value: Double): String {
    return when {
        value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
        value >= 10_000 -> String.format(Locale.US, "%.1fk", value / 1_000.0)
        value >= 10.0 -> value.toLong().toString()
        value > 0.0 -> String.format(Locale.US, "%.1f", value)
        else -> "0"
    }
}
