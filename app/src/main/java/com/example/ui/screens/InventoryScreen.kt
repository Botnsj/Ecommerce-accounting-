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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Product
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.CoralRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    productList: List<Product>,
    onAddProduct: (String, String, String, String, Double, Double, Double, Int) -> Unit,
    onRestockProduct: (Int, Int, Int, String) -> Unit,
    onDeleteProduct: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProductForRestock by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("All") + productList.map { it.category }.distinct()

    val filteredProducts = productList.filter {
        (searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.sku.contains(searchQuery, ignoreCase = true)) &&
        (selectedCategory == "All" || it.category == selectedCategory)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // --- HEADER ACTION ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Product Catalog Master",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${productList.size} items in active warehouses",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_product_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Product", modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add SKU", fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- FILTER CHIPS AND SEARCH BAR ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().testTag("product_search_input"),
                placeholder = { Text("Search by name, SKU model...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, "search") },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                )
            )

            Spacer(Modifier.height(12.dp))

            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 0.dp,
                divider = {},
                indicator = {}
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surface
                            )
                            .border(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- PRODUCTS CATALOG LAZYCOLUMN ---
            if (filteredProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inventory,
                            contentDescription = "Empty stock",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No inventory products match criteria.",
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductItemCard(
                            product = product,
                            onRestock = { selectedProductForRestock = product },
                            onDelete = { onDeleteProduct(product.id) }
                        )
                    }
                }
            }
        }

        // --- RESTOCK DIALOG (STOCK-IN VOUCHER) ---
        selectedProductForRestock?.let { prod ->
            RestockDialog(
                product = prod,
                onDismiss = { selectedProductForRestock = null },
                onAddStockSubmit = { qty, cost, acct ->
                    onRestockProduct(prod.id, qty, cost, acct)
                    selectedProductForRestock = null
                }
            )
        }

        // --- NEW PRODUCT CREATION MASTER DIALOG ---
        if (showAddDialog) {
            AddProductDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { sku, name, desc, category, gst, sell, buy, minStk ->
                    onAddProduct(sku, name, desc, category, gst, sell, buy, minStk)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onRestock: () -> Unit,
    onDelete: () -> Unit
) {
    val isLowStock = product.currentStockTotal <= product.minStockLevel

    Card(
        modifier = Modifier.fillMaxWidth().testTag("product_item_${product.sku}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title + SKU
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        product.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "SKU: ${product.sku} • HSN: ${product.hsnCode}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                // Low Stock status tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            if (isLowStock) CoralRed.copy(alpha = 0.15f)
                            else EmeraldGreen.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isLowStock) "LOW STOCK" else "OK STATUS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) CoralRed else EmeraldGreen
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Pricing and Quantities details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Warehouse Stock",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "${product.currentStockTotal} Units",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLowStock) CoralRed else MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        "Retail Price (GST ${product.gstRate.toInt()}%)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "₹${"%,.2f".format(product.salePrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column {
                    Text(
                        "Cost Price",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    Text(
                        "₹${"%,.2f".format(product.purchasePrice)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(Modifier.height(8.dp))

            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete product",
                        tint = CoralRed.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                OutlinedButton(
                    onClick = onRestock,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(28.dp).testTag("restock_${product.sku}")
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Restock", modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Stock-In Replenish", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddProductDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Double, Double, Double, Int) -> Unit
) {
    var sku by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var gstRate by remember { mutableStateOf("18") }
    var salePrice by remember { mutableStateOf("") }
    var purchasePrice by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("5") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setup New Product Master SKU", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = sku,
                    onValueChange = { sku = it },
                    label = { Text("SKU Model Code") },
                    placeholder = { Text("e.g. SKU-MNT-4K") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_product_sku")
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    placeholder = { Text("e.g. Dell UltraSharp 32''") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("add_product_name")
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("Base Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_product_buy")
                    )
                    OutlinedTextField(
                        value = salePrice,
                        onValueChange = { salePrice = it },
                        label = { Text("Retail Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("add_product_sell")
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = gstRate,
                        onValueChange = { gstRate = it },
                        label = { Text("GST Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minStock,
                        onValueChange = { minStock = it },
                        label = { Text("Low Stock Threshold") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Product Description (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (sku.isNotEmpty() && name.isNotEmpty()) {
                        onConfirm(
                            sku, name, description, category,
                            gstRate.toDoubleOrNull() ?: 18.0,
                            salePrice.toDoubleOrNull() ?: 0.0,
                            purchasePrice.toDoubleOrNull() ?: 0.0,
                            minStock.toIntOrNull() ?: 5
                        )
                    }
                },
                modifier = Modifier.testTag("dialog_add_sku_confirm")
            ) {
                Text("Add Master SKU")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun RestockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddStockSubmit: (Int, Int, String) -> Unit
) {
    var quantityText by remember { mutableStateOf("10") }
    var costText by remember { mutableStateOf(product.purchasePrice.toInt().toString()) }
    var selectedAccount by remember { mutableStateOf("Bank Account") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Inventory Replenish (Stock-In)", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "You are recording a replenishment order for ${product.name} (SKU: ${product.sku}). This will auto-update warehouse totals and book corresponding Purchases double-entry logs.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Replenish Qty (Units)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("restock_qty_input")
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Acquisition Unit Cost (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("Pay and debit from accounts ledger:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedAccount == "Bank Account",
                                onClick = { selectedAccount = "Bank Account" }
                            )
                            Text("Bank Account", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedAccount == "Cash Account",
                                onClick = { selectedAccount = "Cash Account" }
                            )
                            Text("Cash Account", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toIntOrNull() ?: 1
                    val cost = costText.toIntOrNull() ?: product.purchasePrice.toInt()
                    onAddStockSubmit(qty, cost, selectedAccount)
                },
                modifier = Modifier.testTag("dialog_restock_confirm")
            ) {
                Text("Confirm Receipt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
