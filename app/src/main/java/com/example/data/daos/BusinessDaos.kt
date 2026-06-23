package com.example.data.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

@Dao
interface WorkspaceDao {
    @Query("SELECT * FROM workspaces ORDER BY id ASC")
    fun getAllWorkspaces(): Flow<List<Workspace>>

    @Query("SELECT * FROM workspaces WHERE id = :id LIMIT 1")
    suspend fun getWorkspaceById(id: Int): Workspace?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkspace(workspace: Workspace): Long

    @Update
    suspend fun updateWorkspace(workspace: Workspace)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE workspaceId = :workspaceId ORDER BY sku ASC")
    fun getProductsByWorkspace(workspaceId: Int): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): Product?

    @Query("SELECT * FROM products WHERE workspaceId = :workspaceId AND sku = :sku LIMIT 1")
    suspend fun getProductBySku(workspaceId: Int, sku: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Update
    suspend fun updateProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Int)
}

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM warehouses WHERE workspaceId = :workspaceId")
    fun getWarehousesByWorkspace(workspaceId: Int): Flow<List<Warehouse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarehouse(warehouse: Warehouse): Long
}

@Dao
interface StockTransactionDao {
    @Query("SELECT * FROM stock_transactions WHERE workspaceId = :workspaceId ORDER BY timestamp DESC")
    fun getTransactionsByWorkspace(workspaceId: Int): Flow<List<StockTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransaction): Long
}

@Dao
interface LedgerAccountDao {
    @Query("SELECT * FROM ledger_accounts WHERE workspaceId = :workspaceId ORDER BY name ASC")
    fun getAccountsByWorkspace(workspaceId: Int): Flow<List<LedgerAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: LedgerAccount): Long

    @Query("SELECT * FROM ledger_accounts WHERE workspaceId = :workspaceId AND groupType = :groupType")
    suspend fun getAccountsByGroup(workspaceId: Int, groupType: String): List<LedgerAccount>
}

@Dao
interface LedgerTransactionDao {
    @Query("SELECT * FROM ledger_transactions WHERE workspaceId = :workspaceId ORDER BY date DESC")
    fun getTransactionsByWorkspace(workspaceId: Int): Flow<List<LedgerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: LedgerTransaction): Long

    @Query("DELETE FROM ledger_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)
}

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices WHERE workspaceId = :workspaceId ORDER BY date DESC")
    fun getInvoicesByWorkspace(workspaceId: Int): Flow<List<Invoice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: Invoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItem>)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId")
    fun getItemsForInvoice(invoiceId: Int): Flow<List<InvoiceItem>>
}

@Dao
interface EcommerceOrderDao {
    @Query("SELECT * FROM ecommerce_orders WHERE workspaceId = :workspaceId ORDER BY date DESC")
    fun getOrdersByWorkspace(workspaceId: Int): Flow<List<EcommerceOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: EcommerceOrder)

    @Update
    suspend fun updateOrder(order: EcommerceOrder)
}
