package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.Invoice
import com.example.data.entities.Product
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoicingScreen(
    invoices: List<Invoice>,
    products: List<Product>,
    workspaceName: String,
    workspaceGst: String,
    onGenerateInvoice: (String, String, Int, Int, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("InvoiceList") } // "InvoiceList" or "NewInvoice"

    // Form inputs state
    var customerName by remember { mutableStateOf("") }
    var customerGst by remember { mutableStateOf("") }
    var selectedProductId by remember { mutableStateOf<Int?>(null) }
    var quantityText by remember { mutableStateOf("1") }
    var payToAccount by remember { mutableStateOf("Bank Account") } // "Bank Account" or "Cash Account"

    var selectedInvoiceForDetail by remember { mutableStateOf<Invoice?>(null) }

    val selectedProduct = products.find { it.id == selectedProductId }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // --- HEADER ACTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "GST Compliance Billing",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Tax compliant invoicing under KGST models",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }

            // Tab switch toggles
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(3.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeTab == "InvoiceList") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = "InvoiceList" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "All Bills",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == "InvoiceList") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (activeTab == "NewInvoice") MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable { activeTab = "NewInvoice" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("create_invoice_tab")
                ) {
                    Text(
                        "New Bill",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == "NewInvoice") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (activeTab == "InvoiceList") {
            // --- INVOICE LIST VIEW ---
            if (invoices.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "No sales invoices generated yet.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(invoices) { inv ->
                        InvoiceRowItem(
                            invoice = inv,
                            onClick = { selectedInvoiceForDetail = inv }
                        )
                    }
                }
            }
        } else {
            // --- NEW INVOICE BUILDER ---
            if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        "Set up products in 'Catalog' first before building bills.",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Initialize choice
                if (selectedProductId == null && products.isNotEmpty()) {
                    selectedProductId = products.first().id
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Counter Billing Form", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)

                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name / Brand Name") },
                        placeholder = { Text("e.g. Mukesh Traders Ltd.") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("billing_customer_name")
                    )

                    OutlinedTextField(
                        value = customerGst,
                        onValueChange = { customerGst = it },
                        label = { Text("Customer GSTIN (Optional)") },
                        placeholder = { Text("e.g. 27AAPL0122K1ZS") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("billing_customer_gst")
                    )

                    // Simple Dropdown selection (using clickable box + modal/dialog or static select)
                    var showDropdownSelection by remember { mutableStateOf(false) }
                    Column {
                        Text("Select Product Master SKU", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                                .clickable { showDropdownSelection = true }
                                .padding(14.dp)
                        ) {
                            Text(
                                text = selectedProduct?.let { "${it.sku} - ${it.name} (Price: ₹${it.salePrice})" } ?: "Select a product...",
                                fontSize = 14.sp
                            )
                        }

                        if (showDropdownSelection) {
                            AlertDialog(
                                onDismissRequest = { showDropdownSelection = false },
                                title = { Text("Choose SKU Product") },
                                text = {
                                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(products) { prod ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedProductId = prod.id
                                                        showDropdownSelection = false
                                                    }
                                                    .padding(12.dp)
                                            ) {
                                                Text("${prod.sku} • ${prod.name} (₹${prod.salePrice})")
                                            }
                                        }
                                    }
                                },
                                confirmButton = {}
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("billing_quantity")
                        )

                        // Mode of Payment
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("Deposit Account", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = payToAccount == "Bank Account",
                                        onClick = { payToAccount = "Bank Account" }
                                    )
                                    Text("Bank", fontSize = 11.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = payToAccount == "Cash Account",
                                        onClick = { payToAccount = "Cash Account" }
                                    )
                                    Text("Cash", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Dynamic Live Invoice computation card
                    selectedProduct?.let { prod ->
                        val qty = quantityText.toIntOrNull() ?: 1
                        val unitPrice = prod.salePrice
                        val basePrice = unitPrice / (1.0 + (prod.gstRate / 100.0))
                        val totalBase = basePrice * qty
                        val totalTax = (unitPrice * qty) - totalBase

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Automated Tax Tally:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Base Taxable Subtotal:", fontSize = 11.sp, color = SoftGray)
                                    Text("₹${"%,.2f".format(totalBase)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Central GST (CGST ${prod.gstRate/2}%):", fontSize = 11.sp, color = SoftGray)
                                    Text("₹${"%,.2f".format(totalTax / 2.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("State GST (SGST ${prod.gstRate/2}%):", fontSize = 11.sp, color = SoftGray)
                                    Text("₹${"%,.2f".format(totalTax / 2.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Invoice Net:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("₹${"%,.2f".format(unitPrice * qty)}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = EmeraldGreen)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    Button(
                        onClick = {
                            val productId = selectedProductId
                            if (customerName.isNotEmpty() && productId != null) {
                                onGenerateInvoice(
                                    customerName,
                                    customerGst,
                                    productId,
                                    quantityText.toIntOrNull() ?: 1,
                                    payToAccount
                                )
                                // Clear inputs
                                customerName = ""
                                customerGst = ""
                                quantityText = "1"
                                activeTab = "InvoiceList"
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("dialog_create_invoice_confirm"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, "receipt")
                        Spacer(Modifier.width(8.dp))
                        Text("Save & Book Double-Entry Invoice", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- INVOICE PRINT SPEC MODAL (VOUCHER POPUP) ---
        selectedInvoiceForDetail?.let { inv ->
            InvoicePrintDetailDialog(
                invoice = inv,
                workspaceName = workspaceName,
                workspaceGst = workspaceGst,
                onDismiss = { selectedInvoiceForDetail = null }
            )
        }
    }
}

@Composable
fun InvoiceRowItem(invoice: Invoice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("invoice_row_${invoice.invoiceNo}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ReceiptLong,
                    contentDescription = "Invoice File",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    invoice.customerName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(invoice.invoiceNo, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    // Platform badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .padding(horizontal = 4.dp)
                    ) {
                        Text(invoice.ecommercePlatform, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${"%,.2f".format(invoice.totalAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    SimpleDateFormat("dd MMM yy", Locale.getDefault()).format(Date(invoice.date)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun InvoicePrintDetailDialog(
    invoice: Invoice,
    workspaceName: String,
    workspaceGst: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = null, // Custom styled layout inside text block
        text = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(8.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().testTag("invoice_tax_voucher_dialog"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Corporate header
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "TAX INVOICE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                        Text(
                            text = workspaceName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "GSTIN: $workspaceGst (Indian Corporate Hub)",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.Black)

                    // Invoice Specs details
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Billed To client:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(invoice.customerName, fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            if (invoice.customerGst.isNotEmpty()) {
                                Text("Client GSTIN: ${invoice.customerGst}", fontSize = 9.sp, fontFamily = FontFamily.Monospace, color = Color.Black)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Invoice No: ${invoice.invoiceNo}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = Color.Black)
                            Text("Date: ${SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(Date(invoice.date))}", fontSize = 10.sp, color = Color.Gray)
                            Text("Channel: ${invoice.ecommercePlatform}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider(color = Color.Black)

                    // Single itemized row display (Simple mock template)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Item description / SKU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Qty", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
                        Text("Rate Value", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                        Text("Taxable sub", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.End)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("SKU Sale (Base Price Net)", fontSize = 11.sp, color = Color.Black)
                        Text("Bulk sale", fontSize = 11.sp, color = Color.Black)
                        Text("₹${"%,.2f".format(invoice.subtotal)}", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.Gray, thickness = 1.dp)

                    // Ledger breakdown
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Taxable Subtotal (A):", fontSize = 11.sp, color = Color.Black)
                            Text("₹${"%,.2f".format(invoice.subtotal)}", fontSize = 11.sp, color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("CGST Central (Half split):", fontSize = 11.sp, color = Color.Black)
                            Text("₹${"%,.2f".format(invoice.cgstSum)}", fontSize = 11.sp, color = Color.Black)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("SGST State (Half split):", fontSize = 11.sp, color = Color.Black)
                            Text("₹${"%,.2f".format(invoice.sgstSum)}", fontSize = 11.sp, color = Color.Black)
                        }
                        Divider(color = Color.Black, thickness = 2.dp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("GRAND TOTAL (A + GST):", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            Text("₹${"%,.2f".format(invoice.totalAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This is a computer-generated GST compliance audit invoice recorded securely in offline Tally SaaS database. Auto-entry voucher posting completed successfully.",
                        fontSize = 8.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Dismiss Voucher")
            }
        }
    )
}
