package com.example.himaikfinance.data.remote

import com.example.himaikfinance.data.model.AddIncomeRequest
import com.example.himaikfinance.data.model.AddIncomeResponse
import com.example.himaikfinance.data.model.AddTransactionRequest
import com.example.himaikfinance.data.model.AddTransactionResponse
import com.example.himaikfinance.data.model.BalanceEvidenceResponse
import com.example.himaikfinance.data.model.GetIncomeResponse
import com.example.himaikfinance.data.model.GetTransactionResponse
import com.example.himaikfinance.data.model.LoginRequest
import com.example.himaikfinance.data.model.LoginResponse
import com.example.himaikfinance.data.model.TotalBalanceResponse
import com.example.himaikfinance.data.model.TotalIncomeResponse
import com.example.himaikfinance.data.model.TotalOutcomeResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @GET("api/balance")
    suspend fun totalBalance(): Response<TotalBalanceResponse>

    @GET("api/balance/evidence/latest")
    suspend fun balanceEvidence(): Response<BalanceEvidenceResponse>

    @GET( "api/balance/income")
    suspend fun totalIncome(): Response<TotalIncomeResponse>

    @GET("api/balance/outcome")
    suspend fun totalOutcome(): Response<TotalOutcomeResponse>

    @GET("api/incomes")
    suspend fun incomes(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<GetIncomeResponse>

    @GET("api/transactions")
    suspend fun transactions(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<GetTransactionResponse>

    @POST( "api/incomes")
    suspend fun addIcome(
        @Header("Authorization") bearer: String,
        @Body body: AddIncomeRequest
    ): Response<AddIncomeResponse>

    @POST("api/transactions")
    suspend fun addTransaction(
        @Header("Authorization") bearer: String,
        @Body body: AddTransactionRequest
    ): Response<AddTransactionResponse>

    @Multipart
    @POST("api/balance/evidence")
    suspend fun uploadBalanceEvidence(
        @Header("Authorization") bearer: String,
        @Part file: MultipartBody.Part
    ): Response<BalanceEvidenceResponse>
}