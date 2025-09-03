package com.example.himaikfinance.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.himaikfinance.data.local.TokenManager
import com.example.himaikfinance.data.model.AddTransactionRequest
import com.example.himaikfinance.data.model.AddTransactionResponse
import com.example.himaikfinance.data.model.GetIncomeResponse
import com.example.himaikfinance.data.model.GetTransactionResponse
import com.example.himaikfinance.data.model.TotalOutcomeResponse
import com.example.himaikfinance.data.paging.TransactionPagingSource
import com.example.himaikfinance.data.remote.ApiService
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class TransactionRepsitory(private val api: ApiService, private val tokenManager: TokenManager) {

    suspend fun getTotalOutcome(): TotalOutcomeResponse? {
        val resp = api.totalOutcome()
        if (resp.isSuccessful) return resp.body()
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

    fun transactionPager(pageSize: Int = 10): Pager<Int, com.example.himaikfinance.data.model.TransactionData> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = pageSize / 2, initialLoadSize = pageSize),
            pagingSourceFactory = { TransactionPagingSource(api, pageSize) }
        )
    }
}