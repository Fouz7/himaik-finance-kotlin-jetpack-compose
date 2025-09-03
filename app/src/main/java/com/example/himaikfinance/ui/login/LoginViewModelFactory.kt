package com.example.himaikfinance.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.himaikfinance.data.repositories.AuthRepository
import com.example.himaikfinance.data.local.TokenManager

class LoginViewModelFactory(private val context: Context): ViewModelProvider.Factory {
    override fun <T: ViewModel> create(modelClass: Class<T>): T {
        val repo = AuthRepository(TokenManager(context), context)
        return LoginViewModel(repo) as T
    }
}