package com.example.himaikfinance.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.himaikfinance.data.model.IncomeData
import com.example.himaikfinance.data.model.TransactionData
import com.example.himaikfinance.data.repositories.BalanceRepository
import com.example.himaikfinance.data.repositories.IncomeRepository
import com.example.himaikfinance.data.repositories.TransactionRepsitory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class DashboardViewModel(
    initialUsername: String,
    private val balanceRepository: BalanceRepository,
    private val incomeRepository: IncomeRepository,
    private val transactionRepository: TransactionRepsitory
) : ViewModel() {
    private val _username = MutableStateFlow(initialUsername)
    val username: StateFlow<String> = _username.asStateFlow()
    private val _totalBalanceText = MutableStateFlow("-")
    val totalBalanceText: StateFlow<String> = _totalBalanceText.asStateFlow()
    private val _balanceEvidenceUrl = MutableStateFlow<String?>(null)
    val balanceEvidenceUrl: StateFlow<String?> = _balanceEvidenceUrl.asStateFlow()
    private val _totalIncomeText = MutableStateFlow("-")
    val totalIncomeText: StateFlow<String> = _totalIncomeText.asStateFlow()
    private val _totalOutcomeText = MutableStateFlow("-")
    val totalOutcomeText: StateFlow<String> = _totalOutcomeText.asStateFlow()

    private val _refreshLists = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val refreshLists = _refreshLists.asSharedFlow()

    val incomePaging: Flow<PagingData<IncomeData>> =
        incomeRepository.incomePager().flow.cachedIn(viewModelScope)

    val transactionPaging: Flow<PagingData<TransactionData>> =
        transactionRepository.transactionPager().flow.cachedIn(viewModelScope)

    init {
        loadTotalBalance()
        loadBalanceEvidence()
    }

    fun setUsername(name: String) {
        _username.value = name
    }

    fun loadTotalBalance() {
        viewModelScope.launch {
            try {
                val body = balanceRepository.getTotalBalance()
                val amount = body?.balance
                _totalBalanceText.value = if (amount != null) {
                    formatRupiah(amount.toLong())
                } else {
                    "-"
                }
            } catch (_: Exception) {
                _totalBalanceText.value = "-"
            }
        }
    }

    fun loadBalanceEvidence() {
        viewModelScope.launch {
            try {
                val body = balanceRepository.getBalanceEvidence()
                val url = body?.url
                _balanceEvidenceUrl.value = url
            } catch (_: Exception) {
                _balanceEvidenceUrl.value = null
            }
        }
    }

    fun loadTotalIncome() {
        viewModelScope.launch {
            try {
                val body = incomeRepository.getTotalIncome()
                val amount = body?.totalIncome
                _totalIncomeText.value = if (amount != null) {
                    formatRupiah(amount.toLong())
                } else {
                    "-"
                }
            } catch (_: Exception) {
                _totalIncomeText.value = "-"
            }
        }
    }

    fun loadTotalOutcome() {
        viewModelScope.launch {
            try {
                val body = transactionRepository.getTotalOutcome()
                val amount = body?.totalOutcome
                _totalOutcomeText.value = if (amount != null) {
                    formatRupiah(amount.toLong())
                } else {
                    "-"
                }
            } catch (_: Exception) {
                _totalOutcomeText.value = "-"
            }
        }
    }

    suspend fun postIncome(name: String, nominal: Int, transferDate: String): Boolean {
        return try {
            incomeRepository.postIncome(nominal, name, transferDate)
            loadTotalIncome()
            loadTotalBalance()
            _refreshLists.tryEmit(Unit)
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun postTransaction(nominal: Int, notes: String): Boolean {
        return try {
            transactionRepository.postTransaction(nominal, notes)
            loadTotalOutcome()
            loadTotalBalance()
            _refreshLists.tryEmit(Unit)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun formatRupiah(amount: Long): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("id-ID")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        return "Rp ${formatter.format(amount)}"
    }
}