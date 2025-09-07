package com.example.himaikfinance.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "created_at") val createdAt: String,
    @ColumnInfo(name = "created_by") val createdBy: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "nominal") val nominal: String,
    @ColumnInfo(name = "transaction_id") val transactionId: Int,
    @ColumnInfo(name = "transfer_date") val transferDate: String,
)
