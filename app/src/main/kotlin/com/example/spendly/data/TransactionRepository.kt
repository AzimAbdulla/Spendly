package com.example.spendly.data

import com.example.spendly.data.db.TransactionDao
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TransactionRepository(private val dao: TransactionDao) {
    fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactions()
    fun getTransactionsBetween(start: LocalDate, end: LocalDate) = dao.getTransactionsBetween(start, end)
    fun getTransactionsNeedingReview() = dao.getTransactionsByStatus(TransactionStatus.NEEDS_REVIEW)
    fun getRecentTransactions(limit: Int = 10) = dao.getRecentTransactions(limit)
    fun getTransactionById(id: Long): Flow<Transaction?> = dao.getTransactionById(id)
    suspend fun getTransactionByIdOnce(id: Long): Transaction? = dao.getTransactionByIdOnce(id)
    suspend fun addTransaction(transaction: Transaction) = dao.insertTransaction(transaction)
    suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions)
    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = dao.deleteTransaction(transaction)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun clearAll() = dao.deleteAll()
}
