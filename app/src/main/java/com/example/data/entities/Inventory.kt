package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val sku: String,
    val name: String,
    val description: String = "",
    val category: String = "Electronics",
    val barcode: String = "",
    val hsnCode: String = "", // Harmonized System of Nomenclature for GST
    val gstRate: Double = 18.0, // e.g. 5.0, 12.0, 18.0, 28.0
    val salePrice: Double,
    val purchasePrice: Double,
    val minStockLevel: Int = 5,
    val currentStockTotal: Int = 0
)

@Entity(tableName = "warehouses")
data class Warehouse(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val name: String,
    val location: String = ""
)

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val productId: Int,
    val warehouseId: Int,
    val quantityChange: Int, // e.g. +10 for in, -2 for out
    val type: String, // "STOCK-IN", "STOCK-OUT", "TRANSFER"
    val sourceDestination: String, // e.g. "Amazon Order", "Manual Restock"
    val timestamp: Long = System.currentTimeMillis()
)
