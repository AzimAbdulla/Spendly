package com.example.spendly.ui

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
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import com.example.spendly.ui.components.CategoryChartStyle

class SpendlyViewModel(
    private val repository: TransactionRepository,
    private val preferences: SpendlyPreferences
) : ViewModel() {

    private val _defaultCurrency = MutableStateFlow(preferences.defaultCurrency)
    val defaultCurrency: StateFlow<String> = _defaultCurrency.asStateFlow()

    private val _themeMode = MutableStateFlow(preferences.themeMode)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _chartStyle = MutableStateFlow(CategoryChartStyle.fromName(preferences.chartStyle))
    val chartStyle: StateFlow<CategoryChartStyle> = _chartStyle.asStateFlow()

    private val _smsAutoSave = MutableStateFlow(preferences.isSmsAutoSaveEnabled)
    val smsAutoSave: StateFlow<Boolean> = _smsAutoSave.asStateFlow()

    private val _isScanningSms = MutableStateFlow(false)
    val isScanningSms: StateFlow<Boolean> = _isScanningSms.asStateFlow()

    // Month shown on Home and Insights; their month pills change it
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val transactions: StateFlow<List<Transaction>> = repository.getAllTransactions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentTransactions: StateFlow<List<Transaction>> = transactions
        .map { it.take(5) }
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
            list.filter { YearMonth.from(it.date) == month }.sumOf { it.amount }
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
                    .mapValues { (_, txs) -> txs.sumOf { it.amount } }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    val weekDayLabels: List<String> = (6 downTo 0).map { daysAgo ->
        LocalDate.now().minusDays(daysAgo.toLong()).format(DateTimeFormatter.ofPattern("EEE"))
    }

    val weeklySpending: StateFlow<List<Double>> = transactions
        .map { list ->
            (6 downTo 0).map { daysAgo ->
                val date = LocalDate.now().minusDays(daysAgo.toLong())
                list.filter { it.date == date }.sumOf { it.amount }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = List(7) { 0.0 }
        )

    fun addTransaction(
        amount: Double,
        currency: String = "USD",
        merchant: String,
        category: Category,
        notes: String = "",
        status: TransactionStatus = TransactionStatus.CONFIRMED,
        item: String = ""
    ) {
        val now = LocalDateTime.now()
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    currency = currency,
                    merchant = merchant,
                    category = category,
                    date = now.toLocalDate(),
                    time = now.toLocalTime().withNano(0),
                    notes = notes,
                    item = item,
                    source = TransactionSource.MANUAL,
                    status = status
                )
            )
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
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

    fun setChartStyle(style: CategoryChartStyle) {
        preferences.chartStyle = style.name
        _chartStyle.value = style
    }

    fun setSmsAutoSave(enabled: Boolean) {
        preferences.isSmsAutoSaveEnabled = enabled
        _smsAutoSave.value = enabled
    }

    fun scanSmsInbox(context: android.content.Context, onResult: (SmsScanResult) -> Unit = {}) {
        viewModelScope.launch {
            _isScanningSms.value = true
            try {
                val result = SmsReader.scanInbox(
                    context = context,
                    repository = repository,
                    autoSave = _smsAutoSave.value
                )
                onResult(result)
            } finally {
                _isScanningSms.value = false
            }
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
