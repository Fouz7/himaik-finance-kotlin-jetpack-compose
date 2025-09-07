package com.example.himaikfinance.data.local.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "balance_evidence")
data class BalanceEvidenceEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "key") val key: String = "",
    @ColumnInfo(name = "url") val url: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

