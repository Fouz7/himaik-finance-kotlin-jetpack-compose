package com.example.himaikfinance.data.repositories

import com.example.himaikfinance.data.local.TokenManager
import com.example.himaikfinance.data.model.BalanceEvidenceResponse
import com.example.himaikfinance.data.remote.RetrofitClient
import com.example.himaikfinance.data.remote.helper.buildEvidenceFilePart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

class EvidenceRepository(
    private val tokenManager: TokenManager
) {
    suspend fun uploadEvidence(file: File): Result<BalanceEvidenceResponse> =
        withContext(Dispatchers.IO) {
            try {
                val token = tokenManager.tokenFlow.first() ?: return@withContext Result.failure(Exception("No token"))
                val part = buildEvidenceFilePart(file)
                val resp = RetrofitClient.api.uploadBalanceEvidence("Bearer $token", part)
                if (resp.isSuccessful) {
                    resp.body()?.let { Result.success(it) } ?: Result.failure(Exception("Empty body"))
                } else {
                    Result.failure(Exception(resp.errorBody()?.string() ?: resp.message()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}