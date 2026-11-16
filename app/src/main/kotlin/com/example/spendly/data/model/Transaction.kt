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
    val smsKey: String? = null,
    // Someone else paid this back, so the record stays but it isn't the user's own spending
    @ColumnInfo(defaultValue = "0") val paidBack: Boolean = false,
    // Money still to come back: the whole amount, or for a split everything but myShare
    @ColumnInfo(defaultValue = "0") val owed: Boolean = false,
    // A split bill: the user's own part of the amount. Null when the expense isn't split
    val myShare: Double? = null,
    // Another currency than the display currency: what one unit of `currency` was worth in
    // `rateCurrency` (the display currency when it was saved). Null when they were the same
    val rate: Double? = null,
    val rateCurrency: String? = null,
    // Where that rate came from: see RateSource
    @ColumnInfo(defaultValue = "NONE") val rateSource: String = "NONE"
)

/**
 * Who an expense is for, the answer on Add / Edit expense:
 * - just me: all of it is my spending
 * - split: only [myShare] is, and the rest is owed until the others pay
 * - owed: all of it counts until it's paid back
 * - paid back: kept on record, none of it counts
 */
data class Sharing(val paidBack: Boolean = false, val owed: Boolean = false, val myShare: Double? = null) {
    val isSplit get() = myShare != null
    val isJustMe get() = !paidBack && !owed && myShare == null

    /** What it becomes once the money has come back. */
    fun settled() = copy(owed = false, paidBack = myShare == null)
}

val Transaction.sharing get() = Sharing(paidBack, owed, myShare)

/** What an expense cost the user in its own currency: their share of a split, nothing once paid back. */
val Transaction.ownSpend: Double get() = when {
    myShare != null -> myShare.coerceIn(0.0, amount)
    paidBack -> 0.0
    else -> amount
}

/** Money someone still has to give back for this expense, in its own currency. */
val Transaction.ownOwed: Double get() = when {
    !owed -> 0.0
    myShare != null -> amount - ownSpend
    else -> amount
}

// The three below are in the display currency. They rely on `rate` meaning "display currency per
// one of its own", which is how every expense comes out of TransactionRepository (Fx.inDisplay).
// Nothing is rounded here: totals add full-precision values and round only when shown

/** What an expense cost the user, in the display currency. Every total sums this. */
val Transaction.spend: Double get() = ownSpend * (rate ?: 1.0)

/** Money still to come back, in the display currency. */
val Transaction.owedAmount: Double get() = ownOwed * (rate ?: 1.0)

/** The whole amount in the display currency. */
val Transaction.converted: Double get() = amount * (rate ?: 1.0)

/** True when the expense is in another currency than the one totals are shown in. */
val Transaction.isForeign: Boolean get() = rate != null

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
