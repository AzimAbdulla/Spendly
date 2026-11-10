package com.example.spendly

import android.app.Application
import com.example.spendly.data.SpendlyPreferences
import com.example.spendly.data.TransactionRepository
import com.example.spendly.data.db.SpendlyDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.spendly.widget.updateSpendlyWidgets

class SpendlyApplication : Application() {
    val database by lazy { SpendlyDatabase.getDatabase(this) }
    val repository by lazy { TransactionRepository(database.transactionDao()) }
    val preferences by lazy { SpendlyPreferences(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Home screen widgets follow every change: adds, edits, deletes and SMS imports,
        // plus settings such as currency and the widget options
        appScope.launch {
            repository.getAllTransactions().conflate().collect { refreshWidgets() }
        }
        preferences.onChange { refreshWidgets() }
    }

    private var widgetRefresh: Job? = null

    /**
     * Redraws every widget once a burst of changes settles: a settings change or a slider drag
     * becomes one refresh, not one per saved value.
     */
    private fun refreshWidgets() {
        widgetRefresh?.cancel()
        widgetRefresh = appScope.launch {
            delay(100)
            updateSpendlyWidgets(this@SpendlyApplication)
        }
    }
}
