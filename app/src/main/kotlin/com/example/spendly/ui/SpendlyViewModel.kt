package com.example.spendly.ui

import com.example.spendly.data.model.owedAmount
import com.example.spendly.data.model.Sharing
import com.example.spendly.data.model.spend
import com.example.spendly.data.model.isForeign
import com.example.spendly.data.model.ownSpend
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spendly.data.Fx
import com.example.spendly.data.RateSource
import com.example.spendly.data.Rates
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

/** What was spent in one currency: in that currency, and what it comes to in the display currency. */
data class CurrencySpend(val currency: String, val own: Double, val converted: Double, val count: Int)

class SpendlyViewModel(
    private val repository: TransactionRepository,
    private val preferences: SpendlyPreferences,
    private val rates: Rates,
    // Refreshes rates and settles guessed ones in the background; safe to call offline
    private val syncRates: () -> Unit
) : ViewModel() {

    // The display currency: what every total is shown in
    private val _defaultCurrency = MutableStateFlow(preferences.defaultCurrency)
    val defaultCurrency: StateFlow<String> = _defaultCurrency.asStateFlow()

    // The currency a new expense starts in: the last one used, so it follows the user abroad
    private val _spendingCurrency = MutableStateFlow(preferences.spendingCurrency)
    val spendingCurrency: StateFlow<String> = _spendingCurrency.asStateFlow()

    val fx: StateFlow<Fx> = rates.fx

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

    // Everything still to come back, whichever month it was spent in
    val owedTotal: StateFlow<Double> = transactions
        .map { list -> list.sumOf { it.owedAmount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
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

    // The shown month's spending in currencies other than the display one, biggest first
    val monthAbroad: StateFlow<List<CurrencySpend>> = combine(transactions, _selectedMonth) { list, month ->
            list.filter { YearMonth.from(it.date) == month && it.isForeign }
                .groupBy { it.currency }
                .map { (code, txs) -> CurrencySpend(code, txs.sumOf { it.ownSpend }, txs.sumOf { it.spend }, txs.count { it.spend > 0 }) }
                .filter { it.own > 0 }
                .sortedByDescending { it.converted }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
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
    /**
     * Saves an expense on [date] in [currency]; one for today gets the time too, a backdated one
     * has none. [manualRate] is a rate the user typed, kept as it is.
     */
    fun addTransaction(
        amount: Double,
        category: Category,
        item: String,
        date: LocalDate,
        sharing: Sharing = Sharing(),
        currency: String = preferences.defaultCurrency,
        manualRate: Double? = null
    ) {
        val now = LocalDateTime.now()
        preferences.spendingCurrency = currency
        _spendingCurrency.value = currency
        viewModelScope.launch {
            repository.addTransaction(
                withRate(
                    Transaction(
                        amount = amount,
                        currency = currency,
                        merchant = "",
                        category = category,
                        date = date,
                        time = if (date == now.toLocalDate()) now.toLocalTime().withNano(0) else null,
                        item = item,
                        source = TransactionSource.MANUAL,
                        paidBack = sharing.paidBack,
                        owed = sharing.owed,
                        myShare = sharing.myShare
                    ),
                    manualRate,
                    stored = null
                )
            )
            syncRates()
        }
    }

    /** Saving an expense confirms it, and teaches Spendly that payee's category for next time. */
    fun updateTransaction(transaction: Transaction, manualRate: Double? = null) {
        preferences.learnCategory(transaction.merchant, transaction.category)
        viewModelScope.launch {
            repository.updateTransaction(withRate(transaction, manualRate, repository.getStored(transaction.id)))
            syncRates()
        }
    }

    /**
     * [t] with the rate it should be stored with. An edit that leaves the currency and the day
     * alone keeps the rate it was first saved with; otherwise the rate is the one the user typed,
     * or the latest known, marked as a guess until that day's rate can be fetched.
     */
    private fun withRate(t: Transaction, manualRate: Double?, stored: Transaction?): Transaction {
        val fx = rates.fx.value
        val estimate = fx.rate(t.currency)
        return when {
            manualRate != null && t.currency != fx.display ->
                t.copy(rate = manualRate, rateCurrency = fx.display, rateSource = RateSource.MANUAL)
            stored != null && stored.currency == t.currency && stored.date == t.date ->
                t.copy(rate = stored.rate, rateCurrency = stored.rateCurrency, rateSource = stored.rateSource)
            t.currency == fx.display || estimate == null ->
                t.copy(rate = null, rateCurrency = null, rateSource = RateSource.NONE)
            else -> t.copy(rate = estimate, rateCurrency = fx.display, rateSource = RateSource.ESTIMATED)
        }
    }

    // The row as it was stored, kept so Undo puts back exactly that
    private var lastDeleted: Transaction? = null

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            lastDeleted = repository.getStored(id)
            repository.deleteById(id)
        }
    }

    /** Puts back an expense that was just deleted, under its old id. */
    fun restoreTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.addTransaction(lastDeleted?.takeIf { it.id == transaction.id } ?: transaction)
        }
    }

    fun confirmTransaction(id: Long) {
        viewModelScope.launch { repository.confirm(id) }
    }

    /** The money for an owed expense has come back. */
    fun markPaid(id: Long) {
        viewModelScope.launch { repository.markPaid(id) }
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
        // Totals re-add in the new currency; no expense is rewritten
        rates.setDisplay(currency)
        syncRates()
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
                val result = SmsReader.scanInbox(context, repository, preferences, rates.fx.value)
                syncRates()
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
                    val all = repository.getAllStored()
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
                    val have = repository.getAllStored().mapTo(HashSet()) { CsvBackup.sameExpenseKey(it) }
                    val fresh = read.filter { have.add(CsvBackup.sameExpenseKey(it)) }
                    repository.insertAll(fresh)
                    syncRates()
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
            preferences: SpendlyPreferences,
            rates: Rates,
            syncRates: () -> Unit
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SpendlyViewModel(repository, preferences, rates, syncRates) as T
                }
            }
    }
}
