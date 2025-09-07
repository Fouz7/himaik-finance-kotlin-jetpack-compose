package com.example.himaikfinance.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.himaikfinance.data.local.TokenManager
import com.example.himaikfinance.data.local.db.dao.IncomeDao
import com.example.himaikfinance.data.local.db.incomeDataToEntity
import com.example.himaikfinance.data.local.db.incomeEntityToModel
import com.example.himaikfinance.data.model.AddIncomeRequest
import com.example.himaikfinance.data.model.AddIncomeResponse
import com.example.himaikfinance.data.model.GetIncomeResponse
import com.example.himaikfinance.data.model.IncomeData
import com.example.himaikfinance.data.model.TotalIncomeResponse
import com.example.himaikfinance.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class IncomeRepository(
    private val api: ApiService,
    private val tokenManager: TokenManager,
    private val incomeDao: IncomeDao
) {

    suspend fun getTotalIncome(): TotalIncomeResponse? {
        val resp = api.totalIncome()
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

    private fun isDirtyIncome(i: IncomeData): Boolean {
        val d = i.transfer_date
        if (d.isBlank()) return true
        return d.contains("8888") || d.startsWith("8888-") || d == "01/01/8888"
    }

    suspend fun refreshIncomes(pageSize: Int = 50) {
        val resp = api.incomes(page = 1, limit = pageSize)
        if (resp.isSuccessful) {
            val body = resp.body()
            val clean = body?.data?.filterNot { isDirtyIncome(it) } ?: emptyList()
            val entities = clean.map { d -> incomeDataToEntity(d) }
            incomeDao.replaceAll(entities)
            return
        }
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

    fun incomePagingFlow(pageSize: Int = 10): Flow<PagingData<IncomeData>> =
        Pager(
            config = PagingConfig(pageSize = pageSize, prefetchDistance = pageSize / 2, initialLoadSize = pageSize),
            pagingSourceFactory = { incomeDao.pagingSource() }
        ).flow.map { paging -> paging.map { entity -> incomeEntityToModel(entity) } }

    suspend fun ensureCached() {
        if (incomeDao.count() == 0) {
            try { refreshIncomes() } catch (_: Exception) {}
        }
    }

}