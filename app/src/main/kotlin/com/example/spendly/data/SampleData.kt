package com.example.spendly.data

import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import java.time.LocalDate

object SampleData {
    val transactions = listOf(
        Transaction(1, 45.50, "USD", "Whole Foods", Category.FOOD, LocalDate.now().minusDays(0)),
        Transaction(2, 12.99, "USD", "Netflix", Category.SUBSCRIPTIONS, LocalDate.now().minusDays(1)),
        Transaction(3, 8.75, "USD", "Uber", Category.TRANSPORT, LocalDate.now().minusDays(1)),
        Transaction(4, 89.00, "USD", "Zara", Category.SHOPPING, LocalDate.now().minusDays(2)),
        Transaction(5, 34.20, "USD", "Restaurant Bella", Category.FOOD, LocalDate.now().minusDays(2)),
        Transaction(6, 22.40, "USD", "Shell Gas", Category.TRANSPORT, LocalDate.now().minusDays(3)),
        Transaction(7, 15.00, "USD", "Spotify", Category.SUBSCRIPTIONS, LocalDate.now().minusDays(3)),
        Transaction(8, 120.00, "USD", "Doctor Visit", Category.HEALTH, LocalDate.now().minusDays(5)),
        Transaction(9, 67.30, "USD", "Amazon", Category.SHOPPING, LocalDate.now().minusDays(6)),
        Transaction(10, 9.99, "USD", "Coffee Ritual", Category.FOOD, LocalDate.now().minusDays(7)),
        Transaction(11, 55.00, "USD", "Electricity Bill", Category.UTILITIES, LocalDate.now().minusDays(8)),
        Transaction(12, 200.00, "EUR", "Hotel Booking", Category.TRAVEL, LocalDate.now().minusDays(10),
            status = TransactionStatus.NEEDS_REVIEW, source = TransactionSource.SMS),
        Transaction(13, 18.50, "USD", "Chipotle", Category.FOOD, LocalDate.now().minusDays(11)),
        Transaction(14, 35.00, "USD", "Cinema", Category.ENTERTAINMENT, LocalDate.now().minusDays(12)),
        Transaction(15, 150.00, "GBP", "Flight EasyJet", Category.TRAVEL, LocalDate.now().minusDays(14),
            status = TransactionStatus.NEEDS_REVIEW, source = TransactionSource.SMS),
    )

    // Weekly spending for bar chart (last 7 days, Mon-Sun)
    val weeklySpending = listOf(42.0, 78.5, 15.0, 123.0, 56.75, 89.30, 45.50)
    val weekDayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Category totals for donut chart
    val categoryTotals: Map<Category, Double> = transactions
        .groupBy { it.category }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }

    val monthlyTotal: Double = transactions.sumOf { it.amount }
    val reviewCount: Int = transactions.count { it.status == TransactionStatus.NEEDS_REVIEW }
}
