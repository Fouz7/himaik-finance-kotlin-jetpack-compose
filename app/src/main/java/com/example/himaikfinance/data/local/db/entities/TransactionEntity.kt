package com.example.himaikfinance.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey @ColumnInfo(name = "transaction_id") val transactionId: Int,
    @ColumnInfo(name = "balance") val balance: String,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "credit") val credit: String,
    @ColumnInfo(name = "debit") val debit: String,
    @ColumnInfo(name = "notes") val notes: String,
)
