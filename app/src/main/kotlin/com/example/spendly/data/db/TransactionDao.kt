package com.example.spendly.data.db

import androidx.room.*
import com.example.spendly.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    // Lists run newest first: latest day, then latest time, then most recently added
    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // An SMS is already recorded when its key is, or (for imports made before keys existed)
    // when an unkeyed SMS expense has the same payee, amount and day
    @Query("SELECT COUNT(*) FROM transactions WHERE smsKey = :smsKey OR (smsKey IS NULL AND source = 'SMS' AND merchant = :merchant AND amount = :amount AND date = :date)")
    suspend fun countSmsAlreadyRecorded(smsKey: String, merchant: String, amount: Double, date: LocalDate): Int

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Transaction?

    // Expenses whose rate for [display] is still a guess: saved offline, or in another currency
    // from before rates were kept
    @Query("SELECT * FROM transactions WHERE (rateSource = 'ESTIMATED' AND rateCurrency = :display) OR (rate IS NULL AND currency != :display)")
    suspend fun withUnsettledRate(display: String): List<Transaction>

    @Query("UPDATE transactions SET rate = :rate, rateCurrency = :display, rateSource = 'FETCHED' WHERE id = :id")
    suspend fun settleRate(id: Long, rate: Double, display: String)

    @Query("UPDATE transactions SET status = 'CONFIRMED' WHERE id = :id")
    suspend fun confirm(id: Long)

    // The money came back: a split keeps its share, anything else becomes paid back
    @Query("UPDATE transactions SET owed = 0, paidBack = CASE WHEN myShare IS NULL THEN 1 ELSE paidBack END WHERE id = :id")
    suspend fun markPaid(id: Long)

    @Query("UPDATE transactions SET status = 'CONFIRMED' WHERE status = 'NEEDS_REVIEW'")
    suspend fun confirmAll()

    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC, id DESC")
    suspend fun getAllOnce(): List<Transaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
