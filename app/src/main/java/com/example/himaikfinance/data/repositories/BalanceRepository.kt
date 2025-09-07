package com.example.himaikfinance.data.repositories

import com.example.himaikfinance.data.local.db.dao.BalanceDao
import com.example.himaikfinance.data.local.db.entities.BalanceTotalsEntity
import com.example.himaikfinance.data.local.db.evidenceResponseToEntity
import com.example.himaikfinance.data.local.db.totalBalanceToTotalsEntity
import com.example.himaikfinance.data.model.BalanceEvidenceResponse
import com.example.himaikfinance.data.model.TotalBalanceResponse
import com.example.himaikfinance.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class BalanceRepository(
    private val api: ApiService,
    private val balanceDao: BalanceDao
) {
    fun observeTotals(): Flow<BalanceTotalsEntity?> = balanceDao.observeTotals()
    fun observeEvidence(): Flow<com.example.himaikfinance.data.local.db.entities.BalanceEvidenceEntity?> = balanceDao.observeEvidence()

    suspend fun hasTotals(): Boolean = balanceDao.getTotalsOnce() != null
    suspend fun hasEvidence(): Boolean = balanceDao.getEvidenceOnce() != null

    suspend fun refreshTotalBalance() {
        val resp = api.totalBalance()
        if (resp.isSuccessful) {
            val body: TotalBalanceResponse? = resp.body()
            if (body != null) {
                val prev = balanceDao.getTotalsOnce()
                balanceDao.upsertTotals(totalBalanceToTotalsEntity(body, prev))
                return
            }
        }
        throw HttpException(resp)
    }

    suspend fun recalcBalanceFromTotals() {
        val prev = balanceDao.getTotalsOnce() ?: return
        val newBal = (prev.totalIncome - prev.totalOutcome).coerceAtLeast(0)
        balanceDao.upsertTotals(prev.copy(balance = newBal, updatedAt = System.currentTimeMillis()))
    }

    suspend fun refreshBalanceEvidence() {
        val resp = api.balanceEvidence()
        if (resp.isSuccessful) {
            val body: BalanceEvidenceResponse? = resp.body()
            if (body != null) {
                balanceDao.upsertEvidence(evidenceResponseToEntity(body))
                return
            }
        }
        throw HttpException(resp)
    }

    suspend fun refreshTotalIncome(totalIncome: Int) {
        val prev = balanceDao.getTotalsOnce()
        balanceDao.upsertTotals(
            BalanceTotalsEntity(
                id = 1,
                balance = prev?.balance ?: 0,
                totalIncome = totalIncome.toLong(),
                totalOutcome = prev?.totalOutcome ?: 0,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun refreshTotalOutcome(totalOutcome: Int) {
        val prev = balanceDao.getTotalsOnce()
        balanceDao.upsertTotals(
            BalanceTotalsEntity(
                id = 1,
                balance = prev?.balance ?: 0,
                totalIncome = prev?.totalIncome ?: 0,
                totalOutcome = totalOutcome.toLong(),
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}