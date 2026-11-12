package com.example.spendly.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String = "USD",
    val merchant: String,
    val category: Category,
    val date: LocalDate,
    // Time of day it was spent (the SMS time for imports); null for rows saved before v3
    val time: LocalTime? = null,
    val notes: String = "",
    // What was bought, e.g. "Masala dosa". Blank for SMS imports, which only know the merchant
    @ColumnInfo(defaultValue = "") val item: String = "",
    val source: TransactionSource = TransactionSource.MANUAL,
    val status: TransactionStatus = TransactionStatus.CONFIRMED,
    // Identifies the SMS an import came from (when it was sent and its text), so the same
    // message is never recorded twice; null for manual expenses and imports before v4
    val smsKey: String? = null
)

enum class Category(val displayName: String, val shortName: String) {
    FOOD("Food & Dining", "Dining"),
    GROCERIES("Groceries", "Groceries"),
    TRANSPORT("Transport", "Transit"),
    SHOPPING("Shopping", "Shopping"),
    ENTERTAINMENT("Entertainment", "Fun"),
    UTILITIES("Utilities", "Utility"),
    HEALTH("Health", "Health"),
    SUBSCRIPTIONS("Subscriptions", "Subs"),
    TRAVEL("Travel", "Travel"),
    OTHER("Other", "Other")
}

enum class TransactionSource { MANUAL, SMS }
enum class TransactionStatus { CONFIRMED, NEEDS_REVIEW }
