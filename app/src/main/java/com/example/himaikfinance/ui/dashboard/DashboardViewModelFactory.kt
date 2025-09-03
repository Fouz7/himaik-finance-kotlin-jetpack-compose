package com.example.himaikfinance.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.himaikfinance.data.repositories.BalanceRepository
import com.example.himaikfinance.data.repositories.IncomeRepository
import com.example.himaikfinance.data.repositories.TransactionRepsitory

class DashboardViewModelFactory(
    private val usernameProvider: () -> String,
    private val balanceRepository: BalanceRepository,
    private val incomeRepository: IncomeRepository,
    private val transactionRepository: TransactionRepsitory
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(
                usernameProvider(),
                balanceRepository,
                incomeRepository,
                transactionRepository
                ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}