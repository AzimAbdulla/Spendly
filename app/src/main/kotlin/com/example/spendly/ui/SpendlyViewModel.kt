package com.example.spendly.ui

import com.example.spendly.data.model.spend
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spendly.data.SampleData
import com.example.spendly.data.SpendlyPreferences
import com.example.spendly.data.TransactionRepository
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.sms.SmsReader
import com.example.spendly.sms.SmsScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import android.net.Uri
import com.example.spendly.data.CsvBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpendlyViewModel(
    private val repository: TransactionRepository,
    private val preferences: SpendlyPreferences
) : ViewModel() {

    private val _defaultCurrency = MutableStateFlow(preferences.defaultCurrency)
    val defaultCurrency: StateFlow<String> = _defaultCurrency.asStateFlow()

    private val _themeMode = MutableStateFlow(preferences.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(preferences.monthlyBudget)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    // Today's date, refreshed whenever the app comes back to the front, so "today" and the last
    // seven days don't go stale when the app is left open past midnight
    private val _today = MutableStateFlow(LocalDate.now())
    val today: StateFlow<LocalDate> = _today.asStateFlow()

    fun refreshToday() {
        _today.value = LocalDate.now()
    }

    private val _smsAutoSave = MutableStateFlow(preferences.isSmsAutoSaveEnabled)
    val smsAutoSave: StateFlow<Boolean> = _smsAutoSave.asStateFlow()

    private val _isScanningSms = MutableStateFlow(false)
    val isScanningSms: StateFlow<Boolean> = _isScanningSms.asStateFlow()

    // Month shown on Home, Calendar, Insights and Categories; changing it on one changes them all
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // The shown month's latest expenses
    val recentTransactions: StateFlow<List<Transaction>> = combine(transactions, _selectedMonth) { list, month ->
            list.filter { YearMonth.from(it.date) == month }.take(5)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val reviewCount: StateFlow<Int> = transactions
        .map { list -> list.count { it.status == TransactionStatus.NEEDS_REVIEW } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val monthlyTotal: StateFlow<Double> = combine(transactions, _selectedMonth) { list, month ->
            list.filter { YearMonth.from(it.date) == month }.sumOf { it.spend }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val categoryTotals: StateFlow<Map<Category, Double>> = combine(transactions, _selectedMonth) { list, month ->
            val inMonth = list.filter { YearMonth.from(it.date) == month }
            if (inMonth.isEmpty()) {
                emptyMap()
            } else {
                inMonth.groupBy { it.category }
                    .mapValues { (_, txs) -> txs.sumOf { it.spend } }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val weeklySpending: StateFlow<List<Double>> = combine(transactions, _today) { list, today ->
            (6 downTo 0).map { daysAgo ->
                val date = today.minusDays(daysAgo.toLong())
                list.filter { it.date == date }.sumOf { it.spend }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = List(7) { 0.0 }
        )

    /** Saves an expense on [date]; one for today gets the time too, a backdated one has none. */
    fun addTransaction(amount: Double, category: Category, item: String, date: LocalDate, paidBack: Boolean = false) {
        val now = LocalDateTime.now()
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    currency = preferences.defaultCurrency,
                    merchant = "",
                    category = category,
                    date = date,
                    time = if (date == now.toLocalDate()) now.toLocalTime().withNano(0) else null,
                    item = item,
                    source = TransactionSource.MANUAL,
                    paidBack = paidBack
                )
            )
        }
    }

    /** Saving an expense confirms it, and teaches Spendly that payee's category for next time. */
    fun updateTransaction(transaction: Transaction) {
        preferences.learnCategory(transaction.merchant, transaction.category)
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    /** Puts back an expense that was just deleted, under its old id. */
    fun restoreTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(transaction)
        }
    }

    fun confirmTransaction(id: Long) {
        viewModelScope.launch { repository.confirm(id) }
    }

    fun confirmAllReviews() {
        viewModelScope.launch { repository.confirmAll() }
    }

    fun setSelectedMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun setDefaultCurrency(currency: String) {
        preferences.defaultCurrency = currency
        _defaultCurrency.value = currency
    }

    fun setThemeMode(mode: String) {
        preferences.themeMode = mode
        _themeMode.value = mode
    }

    fun setMonthlyBudget(amount: Double) {
        preferences.monthlyBudget = amount
        _monthlyBudget.value = amount
    }

    fun setSmsAutoSave(enabled: Boolean) {
        preferences.isSmsAutoSaveEnabled = enabled
        _smsAutoSave.value = enabled
    }

    fun scanSmsInbox(context: android.content.Context, onResult: (SmsScanResult) -> Unit = {}) {
        viewModelScope.launch {
            _isScanningSms.value = true
            try {
                val result = SmsReader.scanInbox(context, repository, preferences)
                onResult(result)
            } finally {
                _isScanningSms.value = false
            }
        }
    }

    /** Writes every expense to [uri] as CSV; [onDone] gets how many, or null if it failed. */
    fun exportTo(context: android.content.Context, uri: Uri, onDone: (Int?) -> Unit) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                runCatching {
                    val all = repository.getAllOnce()
                    context.contentResolver.openOutputStream(uri, "wt")!!.use { it.write(CsvBackup.write(all).toByteArray()) }
                    all.size
                }.getOrNull()
            }
            onDone(count)
        }
    }

    /** Adds the expenses in the CSV at [uri] that aren't here already; null if it couldn't be read. */
    fun importFrom(context: android.content.Context, uri: Uri, onDone: (Int?) -> Unit) {
        viewModelScope.launch {
            val count = withContext(Dispatchers.IO) {
                runCatching {
                    val text = context.contentResolver.openInputStream(uri)!!.use { it.readBytes().decodeToString() }
                    val read = CsvBackup.read(text) ?: return@runCatching null
                    val have = repository.getAllOnce().mapTo(HashSet()) { CsvBackup.sameExpenseKey(it) }
                    val fresh = read.filter { have.add(CsvBackup.sameExpenseKey(it)) }
                    repository.insertAll(fresh)
                    fresh.size
                }.getOrNull()
            }
            onDone(count)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            val freshData = SampleData.transactions.map { it.copy(id = 0) }
            repository.insertAll(freshData)
        }
    }

    companion object {
        fun provideFactory(
            repository: TransactionRepository,
            preferences: SpendlyPreferences
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SpendlyViewModel(repository, preferences) as T
                }
            }
    }
}
