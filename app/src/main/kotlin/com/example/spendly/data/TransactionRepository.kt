package com.example.spendly.data

import com.example.spendly.data.db.TransactionDao
import com.example.spendly.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TransactionRepository(private val dao: TransactionDao) {
    fun getAllTransactions(): Flow<List<Transaction>> = dao.getAllTransactions()
    suspend fun isSmsRecorded(smsKey: String, merchant: String, amount: Double, date: LocalDate): Boolean =
        dao.countSmsAlreadyRecorded(smsKey, merchant, amount, date) > 0
    suspend fun confirm(id: Long) = dao.confirm(id)
    suspend fun confirmAll() = dao.confirmAll()
    suspend fun markPaid(id: Long) = dao.markPaid(id)
    suspend fun getAllOnce(): List<Transaction> = dao.getAllOnce()
    suspend fun addTransaction(transaction: Transaction) = dao.insertTransaction(transaction)
    suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions)
    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun clearAll() = dao.deleteAll()
}
