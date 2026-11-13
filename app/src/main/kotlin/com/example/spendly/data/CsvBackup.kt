package com.example.spendly.data

import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import java.time.LocalDate
import java.time.LocalTime

/**
 * Expenses as a CSV file, one per row: the export in Settings, and the import that reads it back.
 * Any spreadsheet opens it. Ids aren't written, so importing never overwrites an expense.
 */
object CsvBackup {
    private val header = listOf("date", "time", "amount", "currency", "category", "item", "merchant", "notes", "source", "status", "paid_back")

    fun write(transactions: List<Transaction>): String = buildString {
        appendLine(header.joinToString(","))
        for (t in transactions) {
            appendLine(
                listOf(
                    t.date.toString(), t.time?.toString().orEmpty(), t.amount.toString(), t.currency,
                    t.category.name, t.item, t.merchant, t.notes, t.source.name, t.status.name, t.paidBack.toString()
                ).joinToString(",") { quote(it) }
            )
        }
    }

    /** Rows that can't be read (no date, no amount) are skipped; null means it isn't a Spendly file. */
    fun read(text: String): List<Transaction>? {
        val rows = parseRows(text.removePrefix("\uFEFF"))
        val columns = rows.firstOrNull()?.map { it.trim().lowercase() } ?: return null
        if ("date" !in columns || "amount" !in columns) return null
        return rows.drop(1).mapNotNull { row ->
            fun cell(name: String) = row.getOrNull(columns.indexOf(name))?.trim().orEmpty()
            val date = runCatching { LocalDate.parse(cell("date")) }.getOrNull() ?: return@mapNotNull null
            val amount = cell("amount").toDoubleOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
            Transaction(
                amount = amount,
                currency = cell("currency").ifEmpty { "INR" },
                merchant = cell("merchant"),
                category = Category.entries.firstOrNull { it.name == cell("category") } ?: Category.OTHER,
                date = date,
                time = runCatching { LocalTime.parse(cell("time")) }.getOrNull(),
                notes = cell("notes"),
                item = cell("item"),
                source = TransactionSource.entries.firstOrNull { it.name == cell("source") } ?: TransactionSource.MANUAL,
                status = TransactionStatus.entries.firstOrNull { it.name == cell("status") } ?: TransactionStatus.CONFIRMED,
                paidBack = cell("paid_back") == "true"
            )
        }
    }

    /** What makes two expenses the same one, so importing a file twice adds nothing. */
    fun sameExpenseKey(t: Transaction): String =
        listOf(t.date, t.time, t.amount, t.category, t.item, t.merchant).joinToString("|")

    private fun quote(field: String): String =
        if (field.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + field.replace("\"", "\"\"") + "\"" else field

    private fun parseRows(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var quoted = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                quoted && c == '"' && text.getOrNull(i + 1) == '"' -> { field.append('"'); i++ }
                c == '"' -> quoted = !quoted
                !quoted && c == ',' -> { row.add(field.toString()); field.clear() }
                !quoted && (c == '\n' || c == '\r') -> {
                    if (c == '\r' && text.getOrNull(i + 1) == '\n') i++
                    row.add(field.toString()); field.clear()
                    if (row.any { it.isNotEmpty() }) rows.add(row)
                    row = mutableListOf()
                }
                else -> field.append(c)
            }
            i++
        }
        row.add(field.toString())
        if (row.any { it.isNotEmpty() }) rows.add(row)
        return rows
    }
}
