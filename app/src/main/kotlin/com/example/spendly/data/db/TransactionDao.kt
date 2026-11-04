package com.example.spendly.data.db

import androidx.room.*
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
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

    @Query("SELECT COUNT(*) FROM transactions WHERE source = :source AND merchant = :merchant AND amount = :amount AND date = :date")
    suspend fun countMatchingTransaction(source: TransactionSource, merchant: String, amount: Double, date: LocalDate): Int

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
