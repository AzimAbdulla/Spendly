package com.example.spendly.data

import com.example.spendly.data.db.TransactionDao
import com.example.spendly.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class TransactionRepository(private val dao: TransactionDao, private val rates: Rates) {
    /** Every expense, newest first, with its rate turned to the display currency (see Fx.inDisplay). */
    fun getAllTransactions(): Flow<List<Transaction>> =
        combine(dao.getAllTransactions(), rates.fx) { list, fx -> list.map(fx::inDisplay) }

    /** The rows exactly as stored, for export and for comparing against an import. */
    suspend fun getAllStored(): List<Transaction> = dao.getAllOnce()
    suspend fun getStored(id: Long): Transaction? = dao.getById(id)

    suspend fun isSmsRecorded(smsKey: String, merchant: String, amount: Double, date: LocalDate): Boolean =
        dao.countSmsAlreadyRecorded(smsKey, merchant, amount, date) > 0
    suspend fun confirm(id: Long) = dao.confirm(id)
    suspend fun confirmAll() = dao.confirmAll()
    suspend fun markPaid(id: Long) = dao.markPaid(id)
    suspend fun addTransaction(transaction: Transaction) = dao.insertTransaction(transaction)
    suspend fun insertAll(transactions: List<Transaction>) = dao.insertAll(transactions)
    suspend fun updateTransaction(transaction: Transaction) = dao.updateTransaction(transaction)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun clearAll() = dao.deleteAll()

    /**
     * Replaces guessed rates with the rate of each expense's own day, when there's a connection.
     * A few days at a time; whatever can't be fetched now stays as it is for the next run.
     */
    suspend fun settleRates() {
        val display = rates.fx.first().display
        dao.withUnsettledRate(display)
            .filter { it.rateSource != RateSource.MANUAL }
            .groupBy { it.currency to it.date }
            .entries.take(15)
            .forEach { (day, rows) ->
                val rate = rates.onDate(day.first, display, day.second) ?: return@forEach
                rows.forEach { dao.settleRate(it.id, rate, display) }
            }
    }
}
