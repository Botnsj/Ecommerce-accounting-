package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.example.data.repository.BusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BusinessViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = BusinessRepository(db)

    // Active workspace configuration state
    private val _currentWorkspaceId = MutableStateFlow<Int?>(null)
    val currentWorkspaceId: StateFlow<Int?> = _currentWorkspaceId.asStateFlow()

    // Load available workspaces
    val workspaces: StateFlow<List<Workspace>> = repository.allWorkspaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active workspace entity
    val activeWorkspace: StateFlow<Workspace?> = combine(_currentWorkspaceId, workspaces) { currentId, wsList ->
        wsList.find { it.id == currentId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Reactive lists for the current Workspace
    val products: StateFlow<List<Product>> = _currentWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getProducts(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val warehouses: StateFlow<List<Warehouse>> = _currentWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getWarehouses(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val stockTransactions: StateFlow<List<StockTransaction>> = _currentWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getStockTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ledgerAccounts: StateFlow<List<LedgerAccount>> = _currentWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getLedgerAccounts(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ledgerTransactions: StateFlow<List<LedgerTransaction>> = _currentWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getLedgerTransactions(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<Invoice>> = _currentWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getInvoices(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ecommerceOrders: StateFlow<List<EcommerceOrder>> = _currentWorkspaceId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getEcommerceOrders(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ANALYTICS COMPILATIONS (Real-time Power BI statistics) ---

    // Total Revenue (Sales billing sums)
    val totalSales: StateFlow<Double> = invoices
        .flatMapLatest { invList -> flowOf(invList.sumOf { it.totalAmount }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Stock Valuation = SUM(currentStock * purchasePrice) for all inventory products
    val stockValuation: StateFlow<Double> = products
        .flatMapLatest { prodList -> flowOf(prodList.sumOf { it.currentStockTotal * it.purchasePrice }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Accumulated GST Tax Payables mapping (CGST + SGST + IGST)
    val totalGstLiability: StateFlow<Double> = invoices
        .flatMapLatest { invList -> flowOf(invList.sumOf { it.cgstSum + it.sgstSum + it.igstSum }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Available Funds = Bank Balance + Cash balance mapped dynamically via Double entry logs
    val availableFunds: StateFlow<Double> = combine(ledgerAccounts, ledgerTransactions) { accounts, transactions ->
        val bankAcc = accounts.find { it.name.contains("Bank", ignoreCase = true) }
        val cashAcc = accounts.find { it.name.contains("Cash", ignoreCase = true) }
        
        val bankBal = bankAcc?.let { getAccountBalance(it.id, transactions, it) } ?: 0.0
        val cashBal = cashAcc?.let { getAccountBalance(it.id, transactions, it) } ?: 0.0
        bankBal + cashBal
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Top Selling products tracking based on Invoice registries
    val topSellingProducts: StateFlow<List<Pair<String, Int>>> = products
        .flatMapLatest { _ -> flowOf(emptyList<Pair<String, Int>>()) } // Populated via detail logic where appropriate
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Score indicator (0 to 100) representing operational viability
    val businessHealthScore: StateFlow<Int> = combine(totalSales, stockValuation, products) { sales, stockVal, prodList ->
        if (prodList.isEmpty()) return@combine 50
        // Healthy is when we have sales velocity, valid inventory value, and no low stock warnings
        val lowStockCount = prodList.count { it.currentStockTotal <= it.minStockLevel }
        val lowStockPenalty = (lowStockCount * 10).coerceAtMost(40)
        val salesScore = (sales / 10000).coerceAtMost(40.0).toInt()
        val stockScore = if (stockVal > 20000) 20 else 10
        (40 + salesScore + stockScore - lowStockPenalty).coerceIn(10, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 65)

    init {
        // Automatically check/create prototype workspace if empty
        viewModelScope.launch {
            workspaces.collect { list ->
                if (list.isEmpty()) {
                    val wsId = repository.createWorkspace(
                        name = "Apex Retailers Ltd.",
                        gstNumber = "27AAICA3912L1ZS",
                        address = "A-304, Business Plaza, Mumbai, IN"
                    )
                    _currentWorkspaceId.value = wsId.toInt()
                    seedDefaultProductsAndEvents(wsId.toInt())
                } else if (_currentWorkspaceId.value == null) {
                    _currentWorkspaceId.value = list.first().id
                }
            }
        }
    }

    // Dynamic Account calculator resolving debit/credit sums per trial account rules
    fun getAccountBalance(accountId: Int, transactions: List<LedgerTransaction>, account: LedgerAccount): Double {
        val debits = transactions.filter { it.debitAccountId == accountId }.sumOf { it.amount }
        val credits = transactions.filter { it.creditAccountId == accountId }.sumOf { it.amount }
        return when (account.groupType) {
            "ASSETS", "PURCHASES", "EXPENSES" -> account.initialBalance + debits - credits
            "LIABILITIES", "SALES", "EQUITY" -> account.initialBalance + credits - debits
            else -> debits - credits
        }
    }

    fun switchWorkspace(id: Int) {
        viewModelScope.launch {
            _currentWorkspaceId.value = id
        }
    }

    fun createNewWorkspace(name: String, gst: String, address: String) {
        viewModelScope.launch {
            val id = repository.createWorkspace(name, gst, address)
            _currentWorkspaceId.value = id.toInt()
            seedDefaultProductsAndEvents(id.toInt())
        }
    }

    // --- MUTATION HANDLING INTERFACES ---

    fun addNewProduct(
        sku: String,
        name: String,
        description: String,
        category: String,
        gstRate: Double,
        salePrice: Double,
        purchasePrice: Double,
        minStock: Int
    ) {
        val wsId = _currentWorkspaceId.value ?: return
        viewModelScope.launch {
            repository.insertProduct(
                Product(
                    workspaceId = wsId,
                    sku = sku,
                    name = name,
                    description = description,
                    category = category,
                    gstRate = gstRate,
                    salePrice = salePrice,
                    purchasePrice = purchasePrice,
                    minStockLevel = minStock,
                    currentStockTotal = 0
                )
            )
        }
    }

    fun addStockReplenishment(productId: Int, quantity: Int, unitCost: Int, paidFromAcc: String) {
        val wsId = _currentWorkspaceId.value ?: return
        viewModelScope.launch {
            repository.registerPurchaseReceipt(wsId, productId, quantity, unitCost, paidFromAcc)
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    fun recordSalesInvoice(
        customerName: String,
        customerGst: String,
        productId: Int,
        quantity: Int,
        payToAccount: String
    ) {
        viewModelScope.launch {
            val wsId = _currentWorkspaceId.value ?: return@launch
            val product = products.value.find { it.id == productId } ?: return@launch
            
            // Calculate base amount vs GST margins
            val unitPrice = product.salePrice
            val baseRate = unitPrice / (1.0 + (product.gstRate / 100.0))
            val baseTotal = baseRate * quantity
            val taxAmount = (unitPrice * quantity) - baseTotal

            val item = InvoiceItem(
                invoiceId = 0,
                productId = productId,
                productName = product.name,
                sku = product.sku,
                quantity = quantity,
                rate = baseRate,
                gstRate = product.gstRate,
                amount = baseTotal,
                cgst = taxAmount / 2.0,
                sgst = taxAmount / 2.0,
                igst = 0.0
            )

            repository.saveSaleInvoice(
                workspaceId = wsId,
                customerName = customerName,
                customerGst = customerGst,
                items = listOf(item),
                isPaid = true,
                paidToAccountName = payToAccount
            )
        }
    }

    fun triggerMockOrderSync() {
        val wsId = _currentWorkspaceId.value ?: return
        viewModelScope.launch {
            repository.syncMockOrders(wsId)
        }
    }

    fun approveEcommerceOrder(order: EcommerceOrder) {
        val wsId = _currentWorkspaceId.value ?: return
        viewModelScope.launch {
            repository.convertOrderToInvoice(wsId, order)
        }
    }

    // Seed helpful preset records to instantly beautify the visual trial dashboard
    private suspend fun seedDefaultProductsAndEvents(workspaceId: Int) {
        // 1. Initial product list
        val p1 = Product(
            workspaceId = workspaceId,
            sku = "SKU-IPH-15P",
            name = "iPhone 15 Pro Max 256GB",
            description = "Apple flagship smartphone with titanium design",
            category = "Electronics",
            barcode = "195949012356",
            hsnCode = "85171300",
            gstRate = 18.0,
            salePrice = 139900.0,
            purchasePrice = 110000.0,
            minStockLevel = 4,
            currentStockTotal = 0
        )
        val p2 = Product(
            workspaceId = workspaceId,
            sku = "SKU-APP-PRO",
            name = "AirPods Pro Generation 2",
            description = "Adaptive ANC active noise canceling earbuds",
            category = "Electronics",
            barcode = "195949392185",
            hsnCode = "85183000",
            gstRate = 18.0,
            salePrice = 24900.0,
            purchasePrice = 18000.0,
            minStockLevel = 8,
            currentStockTotal = 0
        )
        val p3 = Product(
            workspaceId = workspaceId,
            sku = "SKU-ECO-BAG",
            name = "Organic Hemp Travel Tote",
            description = "Sustainable plant wearable daily carry bag",
            category = "Apparel",
            barcode = "889912003822",
            hsnCode = "42022210",
            gstRate = 12.0,
            salePrice = 1850.0,
            purchasePrice = 1100.0,
            minStockLevel = 10,
            currentStockTotal = 0
        )

        val id1 = repository.insertProduct(p1).toInt()
        val id2 = repository.insertProduct(p2).toInt()
        val id3 = repository.insertProduct(p3).toInt()

        // 2. Perform mock purchases to populate live warehouse totals
        repository.registerPurchaseReceipt(workspaceId, id1, 10, 110000, "Bank Account")
        repository.registerPurchaseReceipt(workspaceId, id2, 25, 18000, "Bank Account")
        repository.registerPurchaseReceipt(workspaceId, id3, 50, 1100, "Cash Account")

        // 3. Record some pre-existing beautiful client sale invoices
        val item1 = InvoiceItem(
            invoiceId = 0,
            productId = id1,
            productName = "iPhone 15 Pro Max 256GB",
            sku = "SKU-IPH-15P",
            hsnCode = "85171300",
            quantity = 2,
            rate = 118559.32,  // Base price excl GST
            gstRate = 18.0,
            amount = 237118.64,
            cgst = 21340.68,
            sgst = 21340.68,
            igst = 0.0
        )
        repository.saveSaleInvoice(
            workspaceId = workspaceId,
            customerName = "Reliance Logistics Digital",
            customerGst = "27AAPL0122K1ZS",
            items = listOf(item1),
            isPaid = true,
            paidToAccountName = "Bank Account"
        )

        val item2 = InvoiceItem(
            invoiceId = 0,
            productId = id3,
            productName = "Organic Hemp Travel Tote",
            sku = "SKU-ECO-BAG",
            hsnCode = "42022210",
            quantity = 5,
            rate = 1651.78, // base price
            gstRate = 12.0,
            amount = 8258.9,
            cgst = 495.55,
            sgst = 495.55,
            igst = 0.0
        )
        repository.saveSaleInvoice(
            workspaceId = workspaceId,
            customerName = "Nisha Bhatia (D2C Direct)",
            customerGst = "",
            items = listOf(item2),
            isPaid = true,
            paidToAccountName = "Cash Account"
        )

        // Seed some e-commerce pending orders for user mock testing
        repository.syncMockOrders(workspaceId)
    }
}
