package com.example.himaikfinance.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.himaikfinance.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException

sealed interface LoginUiState {
    object Idle: LoginUiState
    object Loading: LoginUiState
    object Success: LoginUiState
    data class Error(val message: String): LoginUiState
}

class LoginViewModel(
    private val repo: AuthRepository
): ViewModel() {

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
                        is HttpException -> {
                            val raw = e.response()?.errorBody()?.string()
                            try {
                                JSONObject(raw ?: "").optString("message", e.message())
                            } catch (_: Exception) {
                                e.message() ?: "Unexpected error"
                            }
                        }
                        else -> e.message ?: "Unexpected error"
                    }
                    LoginUiState.Error(message)
                }
            )
        }
    }
}

