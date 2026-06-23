package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.entities.Workspace
import com.example.data.entities.Product
import com.example.data.entities.Warehouse
import com.example.data.entities.StockTransaction
import com.example.data.entities.LedgerAccount
import com.example.data.entities.LedgerTransaction
import com.example.data.entities.Invoice
import com.example.data.entities.InvoiceItem
import com.example.data.entities.EcommerceOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class BusinessRepository(private val db: AppDatabase) {

    // --- WORKSPACE ---
    val allWorkspaces: Flow<List<Workspace>> = db.workspaceDao.getAllWorkspaces()

    suspend fun getWorkspaceById(id: Int): Workspace? = db.workspaceDao.getWorkspaceById(id)

    suspend fun createWorkspace(name: String, gstNumber: String, address: String): Long {
        val workspace = Workspace(
            name = name,
            gstNumber = gstNumber,
            address = address
        )
        val wsId = db.workspaceDao.insertWorkspace(workspace).toInt()

        // Automatically provision Default COA (Chart of Accounts)
        db.ledgerAccountDao.insertAccount(LedgerAccount(workspaceId = wsId, name = "Cash Account", groupType = "ASSETS", initialBalance = 10000.0))
        db.ledgerAccountDao.insertAccount(LedgerAccount(workspaceId = wsId, name = "Bank Account", groupType = "ASSETS", initialBalance = 490000.0))
        db.ledgerAccountDao.insertAccount(LedgerAccount(workspaceId = wsId, name = "Sales Revenue", groupType = "SALES", initialBalance = 0.0))
        db.ledgerAccountDao.insertAccount(LedgerAccount(workspaceId = wsId, name = "GST Payable Liability", groupType = "LIABILITIES", initialBalance = 0.0))
        db.ledgerAccountDao.insertAccount(LedgerAccount(workspaceId = wsId, name = "Purchase Expenses", groupType = "PURCHASES", initialBalance = 0.0))
        db.ledgerAccountDao.insertAccount(LedgerAccount(workspaceId = wsId, name = "Capital Equity", groupType = "EQUITY", initialBalance = 500000.0))

        // Auto provision default warehouse
        db.warehouseDao.insertWarehouse(Warehouse(workspaceId = wsId, name = "Main Warehouse", location = "Primary Sector"))
        db.warehouseDao.insertWarehouse(Warehouse(workspaceId = wsId, name = "Amazon FBA Warehouse", location = "Regional Hub"))

        // Create initial starting capital transaction
        // Debit Bank Account 490K + Cash 10K, Credit Capital Equity 500K
        val bankId = findAccountId(wsId, "Bank Account")
        val cashId = findAccountId(wsId, "Cash Account")
        val capitalId = findAccountId(wsId, "Capital Equity")

        db.ledgerTransactionDao.insertTransaction(
            LedgerTransaction(
                workspaceId = wsId,
                description = "Initial Capital Funding - Bank",
                debitAccountId = bankId,
                creditAccountId = capitalId,
                amount = 490000.0,
                referenceNo = "SETUP-01"
            )
        )
        db.ledgerTransactionDao.insertTransaction(
            LedgerTransaction(
                workspaceId = wsId,
                description = "Initial Capital Funding - Cash",
                debitAccountId = cashId,
                creditAccountId = capitalId,
                amount = 10000.0,
                referenceNo = "SETUP-02"
            )
        )

        return wsId.toLong()
    }

    private suspend fun findAccountId(workspaceId: Int, name: String): Int {
        val accounts = db.ledgerAccountDao.getAccountsByWorkspace(workspaceId).first()
        return accounts.find { it.name.equals(name, ignoreCase = true) }?.id ?: 0
    }

    // --- PRODUCTS & WAREHOUSE ---
    fun getProducts(workspaceId: Int): Flow<List<Product>> = db.productDao.getProductsByWorkspace(workspaceId)
    
    suspend fun insertProduct(product: Product) = db.productDao.insertProduct(product)
    
    suspend fun deleteProduct(id: Int) = db.productDao.deleteProduct(id)

    fun getWarehouses(workspaceId: Int): Flow<List<Warehouse>> = db.warehouseDao.getWarehousesByWorkspace(workspaceId)
    
    suspend fun insertWarehouse(warehouse: Warehouse) = db.warehouseDao.insertWarehouse(warehouse)

    fun getStockTransactions(workspaceId: Int): Flow<List<StockTransaction>> = db.stockTransactionDao.getTransactionsByWorkspace(workspaceId)

    // --- ACCOUNTING LEDGER ---
    fun getLedgerAccounts(workspaceId: Int): Flow<List<LedgerAccount>> = db.ledgerAccountDao.getAccountsByWorkspace(workspaceId)
    
    suspend fun insertLedgerAccount(account: LedgerAccount) = db.ledgerAccountDao.insertAccount(account)

    fun getLedgerTransactions(workspaceId: Int): Flow<List<LedgerTransaction>> = db.ledgerTransactionDao.getTransactionsByWorkspace(workspaceId)

    // --- BILLING INVOICES ---
    fun getInvoices(workspaceId: Int): Flow<List<Invoice>> = db.invoiceDao.getInvoicesByWorkspace(workspaceId)

    suspend fun saveSaleInvoice(
        workspaceId: Int,
        customerName: String,
        customerGst: String,
        items: List<InvoiceItem>,
        isPaid: Boolean,
        paidToAccountName: String, // "Cash Account" or "Bank Account"
        platform: String = "Manual"
    ): Long {
        val subtotal = items.sumOf { it.amount }
        val cgstSum = items.sumOf { it.cgst }
        val sgstSum = items.sumOf { it.sgst }
        val igstSum = items.sumOf { it.igst }
        val total = subtotal + cgstSum + sgstSum + igstSum

        val nextId = (db.invoiceDao.getInvoicesByWorkspace(workspaceId).first().size + 1)
        val invoiceNo = "INV-${System.currentTimeMillis() % 100000}-$nextId"

        val invoice = Invoice(
            workspaceId = workspaceId,
            invoiceNo = invoiceNo,
            customerName = customerName,
            customerGst = customerGst,
            subtotal = subtotal,
            cgstSum = cgstSum,
            sgstSum = sgstSum,
            igstSum = igstSum,
            totalAmount = total,
            isPaid = isPaid,
            paidAmount = if (isPaid) total else 0.0,
            ecommercePlatform = platform
        )

        val invId = db.invoiceDao.insertInvoice(invoice).toInt()

        val itemEntities = items.map { it.copy(invoiceId = invId) }
        db.invoiceDao.insertInvoiceItems(itemEntities)

        // Post stock changes and ledger entries
        val defaultWarehouseId = db.warehouseDao.getWarehousesByWorkspace(workspaceId).first().firstOrNull()?.id ?: 1

        for (item in items) {
            val product = db.productDao.getProductById(item.productId)
            if (product != null) {
                // Deduct stock
                val updatedStock = (product.currentStockTotal - item.quantity).coerceAtLeast(0)
                db.productDao.updateProduct(product.copy(currentStockTotal = updatedStock))

                // Record stock outflow ledger log
                db.stockTransactionDao.insertTransaction(
                    StockTransaction(
                        workspaceId = workspaceId,
                        productId = product.id,
                        warehouseId = defaultWarehouseId,
                        quantityChange = -item.quantity,
                        type = "STOCK-OUT",
                        sourceDestination = "Invoice $invoiceNo"
                    )
                )
            }
        }

        // --- Double-Entry Automated Posting ---
        val salesRevId = findAccountId(workspaceId, "Sales Revenue")
        val gstPayableId = findAccountId(workspaceId, "GST Payable Liability")
        val billingAccountId = findAccountId(workspaceId, paidToAccountName) // e.g. "Cash Account" or "Bank Account"

        // 1. Debit Cash/Bank for total invoice value
        // 2. Credit Sales Revenue for the subtotal
        // 3. Credit GST Payable Liability for tax sums

        if (subtotal > 0 && salesRevId > 0 && billingAccountId > 0) {
            // Post sales revenue allocation
            db.ledgerTransactionDao.insertTransaction(
                LedgerTransaction(
                    workspaceId = workspaceId,
                    description = "Sales revenue allocation: $invoiceNo",
                    debitAccountId = billingAccountId,
                    creditAccountId = salesRevId,
                    amount = subtotal,
                    referenceNo = invoiceNo
                )
            )
        }

        val totalGst = cgstSum + sgstSum + igstSum
        if (totalGst > 0 && gstPayableId > 0 && billingAccountId > 0) {
            // Post tax liability booking
            db.ledgerTransactionDao.insertTransaction(
                LedgerTransaction(
                    workspaceId = workspaceId,
                    description = "GST tax liability booking: $invoiceNo",
                    debitAccountId = billingAccountId,
                    creditAccountId = gstPayableId,
                    amount = totalGst,
                    referenceNo = invoiceNo
                )
            )
        }

        // Update company balance
        val ws = getWorkspaceById(workspaceId)
        if (ws != null && isPaid) {
            db.workspaceDao.updateWorkspace(ws.copy(currentBalance = ws.currentBalance + total))
        }

        return invId.toLong()
    }

    // Register purchases (restocking items)
    suspend fun registerPurchaseReceipt(
        workspaceId: Int,
        productId: Int,
        quantity: Int,
        unitCost: Int,
        paidFromAccountName: String // "Cash Account" or "Bank Account"
    ) {
        val product = db.productDao.getProductById(productId) ?: return
        val totalCost = quantity * unitCost.toDouble()

        // 1. Increment Stock
        val updatedStock = product.currentStockTotal + quantity
        db.productDao.updateProduct(product.copy(currentStockTotal = updatedStock))

        // 2. Record Stock Inflow Transaction log
        val defaultWarehouseId = db.warehouseDao.getWarehousesByWorkspace(workspaceId).first().firstOrNull()?.id ?: 1
        db.stockTransactionDao.insertTransaction(
            StockTransaction(
                workspaceId = workspaceId,
                productId = productId,
                warehouseId = defaultWarehouseId,
                quantityChange = quantity,
                type = "STOCK-IN",
                sourceDestination = "Purchase stock receipt"
            )
        )

        // 3. Double-entry transaction
        val purchaseAccId = findAccountId(workspaceId, "Purchase Expenses")
        val payFromId = findAccountId(workspaceId, paidFromAccountName)

        if (totalCost > 0 && purchaseAccId > 0 && payFromId > 0) {
            // Debit Purchases Expense Account, Credit Bank Account / Cash
            db.ledgerTransactionDao.insertTransaction(
                LedgerTransaction(
                    workspaceId = workspaceId,
                    description = "Stock replenishment: ${product.name} x$quantity",
                    debitAccountId = purchaseAccId,
                    creditAccountId = payFromId,
                    amount = totalCost,
                    referenceNo = "PUR-${System.currentTimeMillis() % 1000}"
                )
            )
        }

        // Update company balance
        val ws = getWorkspaceById(workspaceId)
        if (ws != null) {
            db.workspaceDao.updateWorkspace(ws.copy(currentBalance = ws.currentBalance - totalCost))
        }
    }

    // --- ECOMMERCE SALES SYNC ---
    fun getEcommerceOrders(workspaceId: Int): Flow<List<EcommerceOrder>> = db.ecommerceOrderDao.getOrdersByWorkspace(workspaceId)

    suspend fun syncMockOrders(workspaceId: Int) {
        val products = db.productDao.getProductsByWorkspace(workspaceId).first()
        if (products.isEmpty()) return

        // Seed some platform orders simulating incoming live streams
        val orderSamples = listOf(
            EcommerceOrder("AMZN-26-9810", workspaceId, "Aditya R.", products[0 % products.size].sku, 2, products[0 % products.size].salePrice, "Amazon", "PENDING", System.currentTimeMillis()),
            EcommerceOrder("FLIP-26-4412", workspaceId, "Priya Nair", products[minOf(1, products.size - 1)].sku, 1, products[minOf(1, products.size - 1)].salePrice, "Flipkart", "PENDING", System.currentTimeMillis() - 5400000),
            EcommerceOrder("SHPF-26-1188", workspaceId, "Rohan Verma", products[minOf(2, products.size - 1)].sku, 5, products[minOf(2, products.size - 1)].salePrice, "Shopify", "DELIVERED", System.currentTimeMillis() - 86400000)
        )

        for (order in orderSamples) {
            db.ecommerceOrderDao.insertOrder(order)
        }
    }

    suspend fun convertOrderToInvoice(workspaceId: Int, order: EcommerceOrder) {
        if (order.invoiceGenerated) return

        val product = db.productDao.getProductBySku(workspaceId, order.sku) ?: return
        
        // Calculate dynamic GST for the individual order SKU
        val baseRate = product.salePrice / (1.0 + (product.gstRate / 100.0))
        val totalBase = baseRate * order.quantity
        val gstTaxTotal = product.salePrice * order.quantity - totalBase
        
        val item = InvoiceItem(
            invoiceId = 0, // Assigned later
            productId = product.id,
            productName = product.name,
            sku = product.sku,
            hsnCode = product.hsnCode,
            quantity = order.quantity,
            rate = baseRate,
            gstRate = product.gstRate,
            amount = totalBase,
            cgst = gstTaxTotal / 2.0,
            sgst = gstTaxTotal / 2.0,
            igst = 0.0
        )

        // Save Legal invoice via unified pipeline (deducts stock and books double-entry)
        saveSaleInvoice(
            workspaceId = workspaceId,
            customerName = order.customerName,
            customerGst = "GSTIN22PLM4911Z",
            items = listOf(item),
            isPaid = true,
            paidToAccountName = "Bank Account", // Online order direct cleared
            platform = order.platform
        )

        // Mark order as completed / invoice generated
        db.ecommerceOrderDao.updateOrder(order.copy(invoiceGenerated = true, status = "DELIVERED"))
    }
}
