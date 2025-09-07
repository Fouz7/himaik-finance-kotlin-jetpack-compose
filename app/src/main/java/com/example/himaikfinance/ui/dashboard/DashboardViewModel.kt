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
import kotlinx.coroutines.flow.collectLatest

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
        incomeRepository.incomePagingFlow().cachedIn(viewModelScope)

    val transactionPaging: Flow<PagingData<TransactionData>> =
        transactionRepository.transactionPagingFlow().cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            balanceRepository.observeTotals().collectLatest { totals ->
                if (totals != null) {
                    _totalBalanceText.value = formatRupiah(totals.balance)
                    _totalIncomeText.value = formatRupiah(totals.totalIncome)
                    _totalOutcomeText.value = formatRupiah(totals.totalOutcome)
                }
            }
        }
        viewModelScope.launch {
            balanceRepository.observeEvidence().collectLatest { ev ->
                _balanceEvidenceUrl.value = ev?.url
            }
        }
        viewModelScope.launch {
            incomeRepository.ensureCached()
            transactionRepository.ensureCached()
        }
        loadTotalBalance()
        loadBalanceEvidence()
        loadTotalIncome()
        loadTotalOutcome()
    }

    fun setUsername(name: String) { _username.value = name }

    fun loadTotalBalance() {
        viewModelScope.launch {
            try {
                balanceRepository.refreshTotalBalance()
            } catch (_: Exception) {
                try { balanceRepository.recalcBalanceFromTotals() } catch (_: Exception) {}
            }
        }
    }

    fun loadBalanceEvidence() {
        viewModelScope.launch {
            try { balanceRepository.refreshBalanceEvidence() } catch (_: Exception) {}
        }
    }

    fun forceRefreshBalanceEvidence() {
        viewModelScope.launch {
            try { balanceRepository.refreshBalanceEvidence() } catch (_: Exception) {}
        }
    }

    fun loadTotalIncome() {
        viewModelScope.launch {
            try {
                val body = incomeRepository.getTotalIncome()
                val amount = body?.totalIncome ?: return@launch
                balanceRepository.refreshTotalIncome(amount)
                try { balanceRepository.recalcBalanceFromTotals() } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
    }

    fun loadTotalOutcome() {
        viewModelScope.launch {
            try {
                val body = transactionRepository.getTotalOutcome()
                val amount = body?.totalOutcome ?: return@launch
                balanceRepository.refreshTotalOutcome(amount)
                try { balanceRepository.recalcBalanceFromTotals() } catch (_: Exception) {}
            } catch (_: Exception) {}
        }
    }

    suspend fun postIncome(name: String, nominal: Int, transferDate: String): Boolean {
        return try {
            incomeRepository.postIncome(nominal, name, transferDate)
            incomeRepository.refreshIncomes()
            transactionRepository.refreshTransactions()
            val ti = incomeRepository.getTotalIncome()?.totalIncome
            if (ti != null) balanceRepository.refreshTotalIncome(ti)
            balanceRepository.refreshTotalBalance()
            try { balanceRepository.recalcBalanceFromTotals() } catch (_: Exception) {}
            _refreshLists.tryEmit(Unit)
            true
        } catch (_: Exception) { false }
    }

    suspend fun postTransaction(nominal: Int, notes: String): Boolean {
        return try {
            transactionRepository.postTransaction(nominal, notes)
            transactionRepository.refreshTransactions()
            val to = transactionRepository.getTotalOutcome()?.totalOutcome
            if (to != null) balanceRepository.refreshTotalOutcome(to)
            balanceRepository.refreshTotalBalance()
            try { balanceRepository.recalcBalanceFromTotals() } catch (_: Exception) {}
            _refreshLists.tryEmit(Unit)
            true
        } catch (_: Exception) { false }
    }

    suspend fun refreshAll() {
        try { incomeRepository.refreshIncomes() } catch (_: Exception) {}
        try { transactionRepository.refreshTransactions() } catch (_: Exception) {}
        try {
            incomeRepository.getTotalIncome()?.totalIncome?.let { balanceRepository.refreshTotalIncome(it) }
        } catch (_: Exception) {}
        try {
            transactionRepository.getTotalOutcome()?.totalOutcome?.let { balanceRepository.refreshTotalOutcome(it) }
        } catch (_: Exception) {}
        try { balanceRepository.refreshTotalBalance() } catch (_: Exception) { try { balanceRepository.recalcBalanceFromTotals() } catch (_: Exception) {} }
        try { balanceRepository.refreshBalanceEvidence() } catch (_: Exception) {}
        _refreshLists.tryEmit(Unit)
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