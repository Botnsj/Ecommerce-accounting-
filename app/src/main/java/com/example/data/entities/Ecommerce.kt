package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ecommerce_orders")
data class EcommerceOrder(
    @PrimaryKey val orderId: String, // e.g. "AMZN-2026-9811"
    val workspaceId: Int,
    val customerName: String,
    val sku: String,
    val quantity: Int,
    val price: Double,
    val platform: String, // "Amazon", "Flipkart", "Shopify"
    val status: String = "PENDING", // PENDING, SHIPPED, DELIVERED
    val date: Long = System.currentTimeMillis(),
    val invoiceGenerated: Boolean = false
)
