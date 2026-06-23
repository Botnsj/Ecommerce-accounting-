package com.example.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
 * Double-entry Chart of Accounts (COA) Account
 * Normal balances depend on account groups:
 * DEBIT-Normal: Assets (Cash, Bank, Inventory), Expenses (Purchases, Cost of Goods Sold)
 * CREDIT-Normal: Liabilities (GST Payable, Creditors), Equity (Capital), Income (Sales)
 */
@Entity(tableName = "ledger_accounts")
data class LedgerAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val name: String,
    val groupType: String, // "ASSETS", "LIABILITIES", "EQUITY", "SALES", "PURCHASES", "EXPENSES"
    val initialBalance: Double = 0.0
)

/**
 * Journal Ledger Transaction - represents a pristine double-entry bookkeeping line.
 * For any volume movement, Debit amount must equal Credit amount.
 * Recording debitAccountId and creditAccountId in a single entry ensures perfect balance.
 */
@Entity(tableName = "ledger_transactions")
data class LedgerTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workspaceId: Int,
    val date: Long = System.currentTimeMillis(),
    val description: String,
    val debitAccountId: Int,
    val creditAccountId: Int,
    val amount: Double,
    val referenceNo: String = ""
)
