package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val invoiceNo: String,
    val customerName: String,
    val customerGst: String = "",
    val date: Long = System.currentTimeMillis(),
    val billingAddress: String = "",
    val subtotal: Double,
    val cgstSum: Double,
    val sgstSum: Double,
    val igstSum: Double,
    val totalAmount: Double,
    val isPaid: Boolean = true,
    val paidAmount: Double = 0.0,
    val ecommercePlatform: String = "Manual" // "Manual", "Amazon", "Flipkart", "Shopify"
)

@Entity(tableName = "invoice_items")
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val invoiceId: Int,
    val productId: Int,
    val productName: String,
    val sku: String,
    val hsnCode: String = "",
    val quantity: Int,
    val rate: Double,
    val gstRate: Double, // percentage, e.g. 18.0
    val amount: Double,  // quantity * rate
    val cgst: Double,    // CGST amount
    val sgst: Double,    // SGST amount
    val igst: Double     // IGST amount (interstate)
)
