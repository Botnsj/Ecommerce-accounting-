package com.example.ui.screens

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entities.LedgerAccount
import com.example.data.entities.LedgerTransaction
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.CoralRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AccountingScreen(
    accounts: List<LedgerAccount>,
    transactions: List<LedgerTransaction>,
    onGetBalance: (Int, List<LedgerTransaction>, LedgerAccount) -> Double,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf("Ledgers") } // "Ledgers", "Journals", "ProfitLoss"

    val tabs = listOf("Ledgers", "Journals", "ProfitLoss")

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // --- HEADER SECTION ---
        Column {
            Text(
                "Double-Entry Accounts Registry",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "GAAP compliant ledger matrices & tax books",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // --- SUB MENU SELECTORS ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEach { tabName ->
                val isSelected = activeSubTab == tabName
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else Color.Transparent
                        )
                        .clickable { activeSubTab = tabName }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (tabName) {
                            "Ledgers" -> "Chart of COA"
                            "Journals" -> "Journal Book"
                            "ProfitLoss" -> "Profit & Loss"
                            else -> tabName
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // --- SUB VIEWS CONDITIONAL ---
        when (activeSubTab) {
            "Ledgers" -> COAMenuView(accounts = accounts, transactions = transactions, onGetBalance = onGetBalance)
            "Journals" -> JournalBookView(accounts = accounts, transactions = transactions)
            "ProfitLoss" -> ProfitLossStatementView(accounts = accounts, transactions = transactions, onGetBalance = onGetBalance)
        }
    }
}

// --- SUB Tab 1: Chart of Accounts View ---
@Composable
fun COAMenuView(
    accounts: List<LedgerAccount>,
    transactions: List<LedgerTransaction>,
    onGetBalance: (Int, List<LedgerTransaction>, LedgerAccount) -> Double
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Ledger Account Name", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            Text("Normal Bal. Sheet Group", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            Text("Real-Time Bal.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f), textAlign = TextAlign.End)
        }

        Divider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))

        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(accounts) { acc ->
                val currentBalance = onGetBalance(acc.id, transactions, acc)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(acc.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("ID: #${acc.id}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }

                        // Type Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(acc.groupType, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        // Balance value
                        Text(
                            "₹${"%,.2f".format(currentBalance)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (currentBalance >= 0) EmeraldGreen else CoralRed,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}

// --- SUB Tab 2: Balanced Journal Entry view ---
@Composable
fun JournalBookView(
    accounts: List<LedgerAccount>,
    transactions: List<LedgerTransaction>
) {
    if (transactions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No transactions captured contextually.", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(transactions) { tx ->
                val debitAccountName = accounts.find { it.id == tx.debitAccountId }?.name ?: "Dr Account"
                val creditAccountName = accounts.find { it.id == tx.creditAccountId }?.name ?: "Cr Account"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = tx.description,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.date)),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        // Double columns bookkeeping style lines
                        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)).padding(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Debit Account (Dr)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = EmeraldGreen)
                                Text(debitAccountName, fontSize = 12.sp, fontWeight = FontWeight.Normal)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Amount Dr", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = EmeraldGreen)
                                Text("₹${"%,.2f".format(tx.amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f)).padding(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Credit Account (Cr)", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = CoralRed)
                                Text(creditAccountName, fontSize = 12.sp, fontWeight = FontWeight.Normal)
                            }
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                Text("Amount Cr", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = CoralRed)
                                Text("₹${"%,.2f".format(tx.amount)}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CoralRed)
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Ref No: ${tx.referenceNo.ifEmpty { "JV-MOCK" }}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Text("No variance (Balanced)", fontSize = 10.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- SUB Tab 3: Profit and Loss Statement ---
@Composable
fun ProfitLossStatementView(
    accounts: List<LedgerAccount>,
    transactions: List<LedgerTransaction>,
    onGetBalance: (Int, List<LedgerTransaction>, LedgerAccount) -> Double
) {
    // 1. Calculate Sales revenue (Credits to Sales Account Group or specific account balances)
    val salesAccounts = accounts.filter { it.groupType == "SALES" }
    val totalRevenue = salesAccounts.sumOf { onGetBalance(it.id, transactions, it) }

    // 2. Calculate Cost of Purchases / Expenses
    val expenseAccounts = accounts.filter { it.groupType == "PURCHASES" || it.groupType == "EXPENSES" }
    val totalExpenses = expenseAccounts.sumOf { onGetBalance(it.id, transactions, it) }

    val netIncome = totalRevenue - totalExpenses

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // High level P&L Visual Scorecard
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Operational Scorecard Summary", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("YTD Net surplus", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                        Text(
                            text = "₹${"%,.2f".format(netIncome)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = if (netIncome >= 0) EmeraldGreen else CoralRed
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(30.dp))
                            .background(
                                if (netIncome >= 0) EmeraldGreen.copy(alpha = 0.15f)
                                else CoralRed.copy(alpha = 0.15f)
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (netIncome >= 0) "+ Profit Profile" else "Deficit State",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (netIncome >= 0) EmeraldGreen else CoralRed
                        )
                    }
                }
            }
        }

        // Detailed statement table
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.04f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Standard Operating Income Statement (P&L)", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))

                // Line 1: Revenues
                PLTableRow(title = "Revenues (A)", subtitle = "Direct ecommerce and manual sales", amount = totalRevenue, direction = 1)
                salesAccounts.forEach { acc ->
                    PLSubTableRow(name = acc.name, balance = onGetBalance(acc.id, transactions, acc))
                }

                Spacer(Modifier.height(10.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(Modifier.height(10.dp))

                // Line 2: Operating Costs
                PLTableRow(title = "Cost of Sales / Replenishment (B)", subtitle = "Acquisitions and raw expenses", amount = totalExpenses, direction = -1)
                expenseAccounts.forEach { acc ->
                    PLSubTableRow(name = acc.name, balance = onGetBalance(acc.id, transactions, acc))
                }

                Spacer(Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 2.dp)
                Spacer(Modifier.height(14.dp))

                // EBITDA Line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total net income (A - B)", fontSize = 13.sp, fontWeight = FontWeight.Black)
                        Text("Operational profit margins", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    }
                    Text(
                        text = "₹${"%,.2f".format(netIncome)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (netIncome >= 0) EmeraldGreen else CoralRed
                    )
                }
            }
        }
    }
}

@Composable
fun PLTableRow(title: String, subtitle: String, amount: Double, direction: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Text(
            text = "₹${"%,.2f".format(amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (direction > 0) EmeraldGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PLSubTableRow(name: String, balance: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(
            text = "₹${"%,.2f".format(balance)}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
    }
}
