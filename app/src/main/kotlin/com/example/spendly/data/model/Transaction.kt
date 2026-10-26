package com.example.spendly.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val currency: String = "USD",
    val merchant: String,
    val category: Category,
    val date: LocalDate,
    val notes: String = "",
    val source: TransactionSource = TransactionSource.MANUAL,
    val status: TransactionStatus = TransactionStatus.CONFIRMED
)

enum class Category(val displayName: String, val emoji: String) {
    FOOD("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    SHOPPING("Shopping", "🛍️"),
    ENTERTAINMENT("Entertainment", "🎬"),
    HEALTH("Health", "💊"),
    UTILITIES("Utilities", "💡"),
    SUBSCRIPTIONS("Subscriptions", "📱"),
    TRAVEL("Travel", "✈️"),
    OTHER("Other", "💸")
}

enum class TransactionSource { MANUAL, SMS }
enum class TransactionStatus { CONFIRMED, NEEDS_REVIEW }
