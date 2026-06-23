package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.daos.WorkspaceDao
import com.example.data.daos.ProductDao
import com.example.data.daos.WarehouseDao
import com.example.data.daos.StockTransactionDao
import com.example.data.daos.LedgerAccountDao
import com.example.data.daos.LedgerTransactionDao
import com.example.data.daos.InvoiceDao
import com.example.data.daos.EcommerceOrderDao
import com.example.data.entities.Workspace
import com.example.data.entities.Product
import com.example.data.entities.Warehouse
import com.example.data.entities.StockTransaction
import com.example.data.entities.LedgerAccount
import com.example.data.entities.LedgerTransaction
import com.example.data.entities.Invoice
import com.example.data.entities.InvoiceItem
import com.example.data.entities.EcommerceOrder

@Database(
    entities = [
        Workspace::class,
        Product::class,
        Warehouse::class,
        StockTransaction::class,
        LedgerAccount::class,
        LedgerTransaction::class,
        Invoice::class,
        InvoiceItem::class,
        EcommerceOrder::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val workspaceDao: WorkspaceDao
    abstract val productDao: ProductDao
    abstract val warehouseDao: WarehouseDao
    abstract val stockTransactionDao: StockTransactionDao
    abstract val ledgerAccountDao: LedgerAccountDao
    abstract val ledgerTransactionDao: LedgerTransactionDao
    abstract val invoiceDao: InvoiceDao
    abstract val ecommerceOrderDao: EcommerceOrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tally_saas_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
