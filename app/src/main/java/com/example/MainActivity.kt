package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BusinessViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    val model: BusinessViewModel = viewModel()
    
    // Reactive States
    val activeWorkspace by model.activeWorkspace.collectAsState()
    val workspaces by model.workspaces.collectAsState()
    val products by model.products.collectAsState()
    val ledgerAccounts by model.ledgerAccounts.collectAsState()
    val ledgerTransactions by model.ledgerTransactions.collectAsState()
    val invoices by model.invoices.collectAsState()
    val ecommerceOrders by model.ecommerceOrders.collectAsState()

    // Calculated metrics
    val totalSales by model.totalSales.collectAsState()
    val stockValuation by model.stockValuation.collectAsState()
    val totalGstLiability by model.totalGstLiability.collectAsState()
    val availableFunds by model.availableFunds.collectAsState()
    val businessHealthScore by model.businessHealthScore.collectAsState()

    var activeTab by remember { mutableStateOf("dashboard") }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    val navigationItems = listOf(
        NavigationItem("dashboard", "Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
        NavigationItem("inventory", "Catalog", Icons.Default.Inventory2, "nav_catalog"),
        NavigationItem("invoices", "Billing", Icons.Default.Receipt, "nav_billing"),
        NavigationItem("accounting", "Ledgers", Icons.Default.AccountBalance, "nav_ledgers"),
        NavigationItem("orders", "Sync Gate", Icons.Default.CloudSync, "nav_sync"),
        NavigationItem("settings", "Console", Icons.Default.Business, "nav_console")
    )

    Scaffold(
        bottomBar = {
            if (!isTablet) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    navigationItems.forEach { item ->
                        val isSelected = activeTab == item.id
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { activeTab = item.id },
                            label = { Text(item.title, fontSize = 9.sp, fontWeight = FontWeight.Bold) },
                            icon = { Icon(item.icon, contentDescription = item.title, modifier = Modifier.size(20.dp)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Adaptive Side Navigation Rail for tablets
            if (isTablet) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                contentDescription = "Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                "Tally SaaS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    },
                    modifier = Modifier.width(72.dp)
                ) {
                    navigationItems.forEach { item ->
                        val isSelected = activeTab == item.id
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { activeTab = item.id },
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = { Text(item.title, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }

            // Central Operational Screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (activeTab) {
                    "dashboard" -> DashboardScreen(
                        totalSales = totalSales,
                        stockValuation = stockValuation,
                        gstLiability = totalGstLiability,
                        funds = availableFunds,
                        healthScore = businessHealthScore,
                        transactions = ledgerTransactions,
                        onSyncTrigger = { model.triggerMockOrderSync() }
                    )
                    "inventory" -> InventoryScreen(
                        productList = products,
                        onAddProduct = { sku, name, desc, category, gst, sell, buy, min ->
                            model.addNewProduct(sku, name, desc, category, gst, sell, buy, min)
                        },
                        onRestockProduct = { prodId, qty, cost, account ->
                            model.addStockReplenishment(prodId, qty, cost, account)
                        },
                        onDeleteProduct = { prodId ->
                            model.deleteProduct(prodId)
                        }
                    )
                    "accounting" -> AccountingScreen(
                        accounts = ledgerAccounts,
                        transactions = ledgerTransactions,
                        onGetBalance = { id, txList, acc ->
                            model.getAccountBalance(id, txList, acc)
                        }
                    )
                    "invoices" -> InvoicingScreen(
                        invoices = invoices,
                        products = products,
                        workspaceName = activeWorkspace?.name ?: "Business Hub Ltd.",
                        workspaceGst = activeWorkspace?.gstNumber ?: "27AAAPP8211ZS",
                        onGenerateInvoice = { name, gst, prodId, qty, account ->
                            model.recordSalesInvoice(name, gst, prodId, qty, account)
                        }
                    )
                    "orders" -> OrdersScreen(
                        orders = ecommerceOrders,
                        onPullSync = { model.triggerMockOrderSync() },
                        onApproveOrder = { order -> model.approveEcommerceOrder(order) }
                    )
                    "settings" -> SettingsScreen(
                        workspaces = workspaces,
                        activeWorkspaceId = model.currentWorkspaceId.value,
                        onSwitchWorkspace = { id -> model.switchWorkspace(id) },
                        onRegisterWorkspace = { name, gst, addr ->
                            model.createNewWorkspace(name, gst, addr)
                        }
                    )
                }
            }
        }
    }
}

data class NavigationItem(
    val id: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val testTag: String
)
