package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.LedgerTransaction
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MetallicCyan
import com.example.ui.theme.VioletAccent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    totalSales: Double,
    stockValuation: Double,
    gstLiability: Double,
    funds: Double,
    healthScore: Int,
    transactions: List<LedgerTransaction>,
    onSyncTrigger: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- HEADER SECTION ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Business Intelligence",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Tally SaaS Engine • Live Sync Status",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = onSyncTrigger,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("sync_orders_button")
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Sync channels",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Sync Channels", fontSize = 12.sp)
                }
            }
        }

        // --- DASHBOARD TOP CARDS METRICS GRID (FlowRow is perfect for wrapping beautifully!) ---
        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                // Total Sales Box
                MetricCard(
                    title = "Total Sales",
                    value = "₹${"%,.2f".format(totalSales)}",
                    icon = Icons.Default.TrendingUp,
                    tint = EmeraldGreen,
                    modifier = Modifier.weight(1f).testTag("metric_sales_card")
                )

                // Stock Valuation Box
                MetricCard(
                    title = "Stock Book Value",
                    value = "₹${"%,.0f".format(stockValuation)}",
                    icon = Icons.Default.Inventory2,
                    tint = VioletAccent,
                    modifier = Modifier.weight(1f).testTag("metric_stock_card")
                )
            }
        }

        item {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2
            ) {
                // GST Payable
                MetricCard(
                    title = "GST Dues Payable",
                    value = "₹${"%,.2f".format(gstLiability)}",
                    icon = Icons.Default.Percent,
                    tint = Color(0xFFEAB308), // Yellow
                    modifier = Modifier.weight(1f).testTag("metric_gst_card")
                )

                // Liquid Capital
                MetricCard(
                    title = "Liquid Funds",
                    value = "₹${"%,.2f".format(funds)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    tint = MetallicCyan,
                    modifier = Modifier.weight(1f).testTag("metric_funds_card")
                )
            }
        }

        // --- BUSINESS HEALTH & HIGH-END CANVAS ANALYTICS ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular health meter gauge
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "SaaS Health",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(90.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw background ring
                                drawCircle(
                                    color = Color.DarkGray.copy(alpha = 0.3f),
                                    style = Stroke(width = 8.dp.toPx())
                                )
                                // Draw dynamic health indicator
                                val angle = (healthScore.toFloat() / 100f) * 360f
                                drawArc(
                                    color = if (healthScore > 75) EmeraldGreen else MetallicCyan,
                                    startAngle = -90f,
                                    sweepAngle = angle,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx())
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$healthScore%",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (healthScore > 75) "EXCELLENT" else "OPTIMAL",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (healthScore > 75) EmeraldGreen else MetallicCyan
                                )
                            }
                        }
                    }
                }

                // Bezier curve of Revenue Growth
                Card(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            "Financial Velocity Graph",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        // Beautiful mini line chart using standard canvas bezier curves
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            val points = listOf(
                                Offset(0f, size.height * 0.8f),
                                Offset(size.width * 0.2f, size.height * 0.7f),
                                Offset(size.width * 0.4f, size.height * 0.4f),
                                Offset(size.width * 0.6f, size.height * 0.65f),
                                Offset(size.width * 0.8f, size.height * 0.2f),
                                Offset(size.width, size.height * 0.15f)
                            )

                            // Helper path
                            val path = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    cubicTo(
                                        (prev.x + curr.x) / 2f, prev.y,
                                        (prev.x + curr.x) / 2f, curr.y,
                                        curr.x, curr.y
                                    )
                                }
                            }

                            // Fill gradient below path
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(size.width, size.height)
                                lineTo(0f, size.height)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        EmeraldGreen.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            )

                            // Draw curves line
                            drawPath(
                                path = path,
                                color = EmeraldGreen,
                                style = Stroke(width = 3.dp.toPx())
                            )
                        }
                    }
                }
            }
        }

        // --- DIRECT DOUBLE-ENTRY LEDGER FEED (Activity logs) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Double Entry Voucher Logs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${transactions.size} records",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No financial ledger entries recorded yet.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(transactions.take(10)) { tx ->
                LedgerNotificationRow(tx)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(tint.copy(alpha = 0.15f), circleShapeForMetric())
                    .border(1.dp, tint.copy(alpha = 0.3f), circleShapeForMetric()),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun LedgerNotificationRow(tx: LedgerTransaction) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("ledger_item_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalance,
                    contentDescription = "Journal Vouchers",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Voucher Ref: ${tx.referenceNo.ifEmpty { "JV-${tx.id}" }}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm • dd MMM", Locale.getDefault()).format(Date(tx.date)),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }

            Text(
                text = "₹${"%,.2f".format(tx.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen
            )
        }
    }
}

fun circleShapeForMetric() = CircleShape
