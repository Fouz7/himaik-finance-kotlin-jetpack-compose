package com.example.himaikfinance.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import com.example.himaikfinance.data.local.TokenManager
import com.example.himaikfinance.data.local.db.transactionDataToEntity
import com.example.himaikfinance.data.local.db.transactionEntityToModel
import com.example.himaikfinance.data.local.db.dao.TransactionDao
import com.example.himaikfinance.data.model.AddTransactionRequest
import com.example.himaikfinance.data.model.AddTransactionResponse
import com.example.himaikfinance.data.model.GetTransactionResponse
import com.example.himaikfinance.data.model.TotalOutcomeResponse
import com.example.himaikfinance.data.remote.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class TransactionRepsitory(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val transactionDao: TransactionDao
) {

    suspend fun getTotalOutcome(): TotalOutcomeResponse? {
        val resp = api.totalOutcome()
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

    suspend fun refreshTransactions(pageSize: Int = 50) {
        val resp = api.transactions(page = 1, limit = pageSize)
        if (resp.isSuccessful) {
            val body = resp.body()
            val entities = body?.data?.map { d -> transactionDataToEntity(d) } ?: emptyList()
            transactionDao.replaceAll(entities)
            return
        }
        throw HttpException(resp)
    }

    suspend fun getTransactions(): GetTransactionResponse? {
        val resp = api.transactions(page = 1, limit = 10)
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

    suspend fun postTransaction(
        nominal: Int,
        notes: String,
    ) : AddTransactionResponse? {
        val token = tokenManager.tokenFlow.first() ?: throw IllegalStateException("No token")
        val resp = api.addTransaction("Bearer $token", AddTransactionRequest(nominal, notes))
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

    fun transactionPagingFlow(pageSize: Int = 10) =
        Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = pageSize / 2, initialLoadSize = pageSize),
            pagingSourceFactory = { transactionDao.pagingSource() }
        ).flow.map { it.map { entity -> transactionEntityToModel(entity) } }

    suspend fun ensureCached() {
        if (transactionDao.count() == 0) {
            try { refreshTransactions() } catch (_: Exception) {}
        }
    }
}