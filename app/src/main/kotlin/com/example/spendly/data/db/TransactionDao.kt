package com.example.spendly.data.db

import androidx.room.*
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getTransactionsBetween(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE status = :status ORDER BY date DESC")
    fun getTransactionsByStatus(status: TransactionStatus): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long

    @Update
    suspend fun updateTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}
