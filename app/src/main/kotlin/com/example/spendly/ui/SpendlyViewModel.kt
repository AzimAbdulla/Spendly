package com.example.spendly.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spendly.data.SampleData
import com.example.spendly.data.TransactionRepository
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SpendlyViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

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

    val monthlyTotal: StateFlow<Double> = transactions
        .map { list ->
            val now = LocalDate.now()
            list.filter { it.date.month == now.month && it.date.year == now.year }
                .sumOf { it.amount }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val categoryTotals: StateFlow<Map<Category, Double>> = transactions
        .map { list ->
            val now = LocalDate.now()
            val currentMonth = list.filter { it.date.month == now.month && it.date.year == now.year }
            if (currentMonth.isEmpty()) {
                emptyMap()
            } else {
                currentMonth.groupBy { it.category }
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
        status: TransactionStatus = TransactionStatus.CONFIRMED
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                Transaction(
                    amount = amount,
                    currency = currency,
                    merchant = merchant,
                    category = category,
                    date = LocalDate.now(),
                    notes = notes,
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

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun loadSampleData() {
        viewModelScope.launch {
            repository.insertAll(SampleData.transactions)
        }
    }

    companion object {
        fun provideFactory(repository: TransactionRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SpendlyViewModel(repository) as T
                }
            }
    }
}
