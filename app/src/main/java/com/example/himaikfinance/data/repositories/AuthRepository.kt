package com.example.himaikfinance.data.repositories

import android.content.Context
import com.example.himaikfinance.data.local.TokenManager
import com.example.himaikfinance.data.model.LoginRequest
import com.example.himaikfinance.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val tokenManager: TokenManager,
    private val context: Context
) {
    suspend fun login(username: String, password: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val resp = RetrofitClient.api.login(LoginRequest(username, password))
                if (resp.isSuccessful) {
                    val body = resp.body() ?: return@withContext Result.failure(Exception("Empty body"))

                    // Save token
                    tokenManager.saveToken(body.data.token)

                    // Save user info to SharedPreferences
                    val user = body.data.user
                    val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("username", user.username)
                        .putString("fullname", user.fullname)
                        .putString("nim", user.nim)
                        .apply()

                    Result.success(Unit)
                } else {
                    Result.failure(Exception(resp.body()?.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}