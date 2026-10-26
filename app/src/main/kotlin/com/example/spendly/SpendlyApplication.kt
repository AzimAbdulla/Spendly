package com.example.spendly

import android.app.Application
import com.example.spendly.data.SpendlyPreferences
import com.example.spendly.data.TransactionRepository
import com.example.spendly.data.db.SpendlyDatabase

class SpendlyApplication : Application() {
    val database by lazy { SpendlyDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
    val preferences by lazy { SpendlyPreferences(this) }
}
