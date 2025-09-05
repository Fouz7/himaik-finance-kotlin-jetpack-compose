package com.example.himaikfinance.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.himaikfinance.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    object Success : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val repo: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val state: StateFlow<LoginUiState> = _state

    fun login(username: String, password: String) {
        _state.value = LoginUiState.Loading
        viewModelScope.launch {
            val result = repo.login(username, password)
            _state.value = result.fold(
                onSuccess = { LoginUiState.Success },
                onFailure = { e ->
                    val message = when (e) {
                        is HttpException -> parseHttpError(e)
                        is IOException -> "Network error. Please check your connection."
                        else -> e.message ?: "Unexpected error"
                    }
                    LoginUiState.Error(message)
                }
            )
        }
    }

    private fun parseHttpError(e: HttpException): String {
        val code = e.code()
        val raw = try {
            e.response()?.errorBody()?.string()
        } catch (_: Exception) {
            null
        }
        if (raw.isNullOrBlank()) return "Login failed (code $code)"
        return try {
            val json = JSONObject(raw)
            when {
                json.optString("message").isNotBlank() -> json.optString("message")
                json.optString("error").isNotBlank() -> json.optString("error")
                json.has("errors") -> {
                    val errors = json.opt("errors")
                    when (errors) {
                        is JSONObject -> errors.keys().asSequence().mapNotNull { k ->
                            val v = errors.optJSONArray(k) ?: return@mapNotNull null
                            val first = v.takeIf { it.length() > 0 }?.opt(0) ?: return@mapNotNull null
                            first as? String ?: first.toString()
                        }.joinToString("\n")

                        else -> errors?.toString() ?: "Login failed (code $code)"
                    }
                }

                json.optString("detail").isNotBlank() -> json.optString("detail")
                else -> "Login failed (code $code)"
            }
        } catch (_: Exception) {
            e.message() ?: "Login failed (code $code)"
        }
    }
}