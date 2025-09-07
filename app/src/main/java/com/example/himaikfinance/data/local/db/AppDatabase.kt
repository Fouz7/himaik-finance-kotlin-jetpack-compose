package com.example.himaikfinance.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.himaikfinance.data.local.db.dao.BalanceDao
import com.example.himaikfinance.data.local.db.dao.IncomeDao
import com.example.himaikfinance.data.local.db.dao.TransactionDao
import com.example.himaikfinance.data.local.db.entities.BalanceEvidenceEntity
import com.example.himaikfinance.data.local.db.entities.BalanceTotalsEntity
import com.example.himaikfinance.data.local.db.entities.IncomeEntity
import com.example.himaikfinance.data.local.db.entities.TransactionEntity

@Database(
    entities = [
        BalanceTotalsEntity::class,
        BalanceEvidenceEntity::class,
        IncomeEntity::class,
        TransactionEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun balanceDao(): BalanceDao
    abstract fun incomeDao(): IncomeDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "himaik_finance.db"
            ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
        }
    }
}
