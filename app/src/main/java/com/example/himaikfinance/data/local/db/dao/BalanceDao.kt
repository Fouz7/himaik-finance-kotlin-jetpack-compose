package com.example.himaikfinance.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.himaikfinance.data.local.db.entities.BalanceEvidenceEntity
import com.example.himaikfinance.data.local.db.entities.BalanceTotalsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BalanceDao {
    @Query("SELECT * FROM balance_totals WHERE id = 1")
    fun observeTotals(): Flow<BalanceTotalsEntity?>

    @Query("SELECT * FROM balance_totals WHERE id = 1")
    suspend fun getTotalsOnce(): BalanceTotalsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTotals(entity: BalanceTotalsEntity)

    @Query("SELECT * FROM balance_evidence WHERE id = 1")
    fun observeEvidence(): Flow<BalanceEvidenceEntity?>

    @Query("SELECT * FROM balance_evidence WHERE id = 1")
    suspend fun getEvidenceOnce(): BalanceEvidenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvidence(entity: BalanceEvidenceEntity)
}

