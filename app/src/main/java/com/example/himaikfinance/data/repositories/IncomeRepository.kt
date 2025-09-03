package com.example.himaikfinance.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.himaikfinance.data.local.TokenManager
import com.example.himaikfinance.data.model.AddIncomeRequest
import com.example.himaikfinance.data.model.AddIncomeResponse
import com.example.himaikfinance.data.model.GetIncomeResponse
import com.example.himaikfinance.data.model.IncomeData
import com.example.himaikfinance.data.model.TotalIncomeResponse
import com.example.himaikfinance.data.paging.IncomePagingSource
import com.example.himaikfinance.data.remote.ApiService
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

class IncomeRepository(private val api: ApiService, private val tokenManager: TokenManager) {

    suspend fun getTotalIncome(): TotalIncomeResponse? {
        val resp = api.totalIncome()
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

    suspend fun getIncomes(): GetIncomeResponse? {
        val resp = api.incomes(page = 1, limit = 10)
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

    suspend fun postIncome(nominal: Int, name: String, transferDate: String): AddIncomeResponse? {
        val token = tokenManager.tokenFlow.first() ?: throw IllegalStateException("No token")
        val resp = api.addIcome("Bearer $token", AddIncomeRequest(name, nominal, transferDate))
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

    fun incomePager(pageSize: Int = 10): Pager<Int, IncomeData> {
        return Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = pageSize / 2, initialLoadSize = pageSize),
            pagingSourceFactory = { IncomePagingSource(api, pageSize) }
        )
    }

}