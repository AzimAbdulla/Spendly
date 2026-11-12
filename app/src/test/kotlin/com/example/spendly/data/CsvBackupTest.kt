package com.example.spendly.data

import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class CsvBackupTest {

    private val expenses = listOf(
        Transaction(
            id = 7, amount = 450.5, currency = "INR", merchant = "", category = Category.FOOD,
            date = LocalDate.of(2026, 11, 10), time = LocalTime.of(8, 15), item = "Masala dosa, \"extra\" chutney"
        ),
        Transaction(
            id = 8, amount = 140.0, currency = "INR", merchant = "Arun K", category = Category.OTHER,
            date = LocalDate.of(2026, 11, 4), time = null, notes = "From SMS: Debited Rs 140.00\nvia UPI",
            source = TransactionSource.SMS, status = TransactionStatus.NEEDS_REVIEW
        )
    )

    @Test
    fun writeThenRead_returnsTheSameExpenses() {
        val read = CsvBackup.read(CsvBackup.write(expenses))!!

        // Ids aren't exported, so an import never overwrites an existing expense
        assertEquals(expenses.map { it.copy(id = 0) }, read)
    }

    @Test
    fun read_skipsRowsWithoutADateOrAmount() {
        val csv = "date,amount,category\n2026-11-01,120,FOOD\nnot a date,50,FOOD\n2026-11-02,,FOOD\n2026-11-03,0,FOOD\n"

        val read = CsvBackup.read(csv)!!
        assertEquals(1, read.size)
        assertEquals(120.0, read[0].amount, 0.001)
        assertEquals(Category.FOOD, read[0].category)
    }

    @Test
    fun read_rejectsAFileThatIsNotAnExport() {
        assertNull(CsvBackup.read("hello,world\n1,2\n"))
        assertNull(CsvBackup.read(""))
    }

    @Test
    fun sameExpenseKey_matchesAnExportedCopy() {
        val copy = CsvBackup.read(CsvBackup.write(expenses))!!
        assertEquals(expenses.map(CsvBackup::sameExpenseKey), copy.map(CsvBackup::sameExpenseKey))
    }
}
