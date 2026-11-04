package com.example.spendly.data

import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import java.time.LocalDate
import java.time.LocalTime

object SampleData {
    val transactions = listOf(
        Transaction(1, 450.00, "INR", "Blue Tokai Coffee", Category.FOOD, LocalDate.now().minusDays(0), LocalTime.of(8, 15)),
        Transaction(2, 1420.00, "INR", "Swiggy Instamart", Category.GROCERIES, LocalDate.now().minusDays(1), LocalTime.of(19, 40)),
        Transaction(3, 380.00, "INR", "Uber Ride to BKC", Category.TRANSPORT, LocalDate.now().minusDays(1), LocalTime.of(9, 5)),
        Transaction(4, 1200.00, "INR", "HP Petrol Pump", Category.TRANSPORT, LocalDate.now().minusDays(2), LocalTime.of(18, 20)),
        Transaction(5, 649.00, "INR", "Netflix", Category.SUBSCRIPTIONS, LocalDate.now().minusDays(3), LocalTime.of(6, 0)),
        Transaction(6, 3490.00, "INR", "Zara Fashion", Category.SHOPPING, LocalDate.now().minusDays(4), LocalTime.of(17, 45)),
        Transaction(7, 8500.00, "INR", "Whole Foods Grocery", Category.GROCERIES, LocalDate.now().minusDays(5), LocalTime.of(11, 30)),
        Transaction(8, 12000.00, "INR", "Fine Dine Bombay Canteen", Category.FOOD, LocalDate.now().minusDays(6), LocalTime.of(21, 10)),
        Transaction(9, 3640.00, "INR", "Apollo Pharmacy", Category.HEALTH, LocalDate.now().minusDays(7), LocalTime.of(13, 25)),
        Transaction(10, 4200.00, "INR", "Tata Power Electricity", Category.UTILITIES, LocalDate.now().minusDays(8), LocalTime.of(10, 0)),
        Transaction(11, 4320.00, "INR", "PVR Cinemas IMAX", Category.ENTERTAINMENT, LocalDate.now().minusDays(9), LocalTime.of(20, 30)),
        Transaction(12, 1850.00, "INR", "Unknown Merchant VPA", Category.OTHER, LocalDate.now().minusDays(10), LocalTime.of(15, 12),
            notes = "Auto-parsed: Sent Rs.1,850.00 from HDFC Bank to VPA on 15-Jan.",
            status = TransactionStatus.NEEDS_REVIEW, source = TransactionSource.SMS),
        Transaction(13, 2400.00, "INR", "Card POS Outflow", Category.OTHER, LocalDate.now().minusDays(12), LocalTime.of(12, 48),
            notes = "Auto-parsed: Debited Rs.2,400.00 at Retail POS on 12-Jan.",
            status = TransactionStatus.NEEDS_REVIEW, source = TransactionSource.SMS),
        Transaction(14, 2100.00, "INR", "Blinkit Quick Delivery", Category.GROCERIES, LocalDate.now().minusDays(14), LocalTime.of(22, 5)),
        Transaction(15, 1200.00, "INR", "Starbucks Reserve", Category.FOOD, LocalDate.now().minusDays(15), LocalTime.of(16, 35))
    )

    // Weekly spending for bar chart (last 7 days, Mon-Sun)
    val weeklySpending = listOf(1450.0, 2100.0, 3480.0, 890.0, 1240.0, 2600.0, 1820.0)
    val weekDayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    // Category totals for donut chart
    val categoryTotals: Map<Category, Double> = transactions
        .groupBy { it.category }
        .mapValues { (_, txns) -> txns.sumOf { it.amount } }

    val monthlyTotal: Double = transactions.sumOf { it.amount }
    val reviewCount: Int = transactions.count { it.status == TransactionStatus.NEEDS_REVIEW }
}
