package com.example.himaikfinance.data.repositories

import com.example.himaikfinance.data.model.BalanceEvidenceResponse
import com.example.himaikfinance.data.model.TotalBalanceResponse
import com.example.himaikfinance.data.remote.ApiService
import retrofit2.HttpException

class BalanceRepository(private val api: ApiService) {
    suspend fun getTotalBalance(): TotalBalanceResponse? {
        val resp = api.totalBalance()
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

    suspend fun getBalanceEvidence(): BalanceEvidenceResponse? {
        val resp = api.balanceEvidence()
        if (resp.isSuccessful) return resp.body()
        throw HttpException(resp)
    }

}