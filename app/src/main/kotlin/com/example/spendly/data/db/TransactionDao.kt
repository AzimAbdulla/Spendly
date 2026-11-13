package com.example.spendly.data.db

import androidx.room.*
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    // Lists run newest first: latest day, then latest time, then most recently added
    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC, time DESC, id DESC")
    fun getTransactionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY date DESC, time DESC, id DESC")
    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC, time DESC, id DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    fun getTransactionById(id: Long): Flow<Transaction?>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionByIdOnce(id: Long): Transaction?

    // An SMS is already recorded when its key is, or (for imports made before keys existed)
    // when an unkeyed SMS expense has the same payee, amount and day
    @Query("SELECT COUNT(*) FROM transactions WHERE smsKey = :smsKey OR (smsKey IS NULL AND source = 'SMS' AND merchant = :merchant AND amount = :amount AND date = :date)")
    suspend fun countSmsAlreadyRecorded(smsKey: String, merchant: String, amount: Double, date: LocalDate): Int

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

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
