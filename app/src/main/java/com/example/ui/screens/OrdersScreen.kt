package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.EcommerceOrder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrdersScreen(
    orders: List<EcommerceOrder>,
    onPullSync: () -> Unit,
    onApproveOrder: (EcommerceOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var platformFilter by remember { mutableStateOf("All") }
    val platforms = listOf("All", "Amazon", "Flipkart", "Shopify")

    val filteredOrders = orders.filter {
        platformFilter == "All" || it.platform.equals(platformFilter, ignoreCase = true)
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // --- HEADER ACTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Ecommerce Sync Gate",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Amazon / Flipkart / Shopify live API channels",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            Button(
                onClick = onPullSync,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("pull_simulate_orders")
            ) {
                Icon(Icons.Default.CloudSync, contentDescription = "Sync Now")
                Spacer(Modifier.width(4.dp))
                Text("Simulate Pull", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- PLATFORM FILTER ROSTER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            platforms.forEach { plat ->
                val isSelected = platformFilter == plat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surface
                        )
                        .border(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.secondary else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { platformFilter = plat }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = plat,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- ORDERS QUEUE ---
        if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = "empty sync",
                        modifier = Modifier.size(56.dp),
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No imported channel orders found.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredOrders) { order ->
                    OrderQueueItemCard(
                        order = order,
                        onApprove = { onApproveOrder(order) }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderQueueItemCard(
    order: EcommerceOrder,
    onApprove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_item_${order.orderId}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Brand + Status Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                when (order.platform.uppercase()) {
                                    "AMAZON" -> Color(0xFFFF9900).copy(alpha = 0.15f)
                                    "FLIPKART" -> Color(0xFF2874F0).copy(alpha = 0.15f)
                                    else -> Color(0xFF96BF48).copy(alpha = 0.15f)
                                },
                                RoundedCornerShape(4.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = order.platform.first().toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = when (order.platform.uppercase()) {
                                "AMAZON" -> Color(0xFFFF9900)
                                "FLIPKART" -> Color(0xFF2874F0)
                                else -> Color(0xFF96BF48)
                            }
                        )
                    }

                    Text(
                        text = order.orderId,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Dynamic invoice generated badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (order.invoiceGenerated) EmeraldGreen.copy(alpha = 0.15f)
                            else Color.Gray.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (order.invoiceGenerated) "INVOICED" else "PENDING ACTION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (order.invoiceGenerated) EmeraldGreen else Color.Gray
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Body Detail Rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Retail Client Name", fontSize = 10.sp, color = SoftGray)
                    Text(order.customerName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Product SKU", fontSize = 10.sp, color = SoftGray)
                    Text(order.sku, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Net Volume", fontSize = 10.sp, color = SoftGray)
                    Text("${order.quantity} x ₹${order.price.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(Modifier.height(8.dp))

            // Conversion Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault()).format(Date(order.date)),
                    fontSize = 10.sp,
                    color = SoftGray
                )

                if (!order.invoiceGenerated) {
                    Button(
                        onClick = onApprove,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                        modifier = Modifier.height(28.dp).testTag("approve_order_${order.orderId}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Approve invoice", modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Approve & Book Invoice", fontSize = 11.sp)
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.DoneAll, contentDescription = "approved", tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                        Text("Double-Entry Ledger Verified", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
