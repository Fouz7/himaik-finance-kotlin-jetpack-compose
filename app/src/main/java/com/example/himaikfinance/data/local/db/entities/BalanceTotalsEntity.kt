package com.example.himaikfinance.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balance_totals")
data class BalanceTotalsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "balance") val balance: Long = 0,
    @ColumnInfo(name = "total_income") val totalIncome: Long = 0,
    @ColumnInfo(name = "total_outcome") val totalOutcome: Long = 0,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)
