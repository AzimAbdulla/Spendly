package com.example.spendly.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.spendly.SpendlyApplication
import com.example.spendly.data.model.TransactionSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        android.util.Log.d("SpendlySms", "onReceive: action=${intent.action}")
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        android.util.Log.d("SpendlySms", "messages count=${messages.size}")
        if (messages.isEmpty()) return

        val fullBody = messages.joinToString("") { it.displayMessageBody ?: "" }
        android.util.Log.d("SpendlySms", "fullBody=$fullBody")
        val parsed = SmsParser.parse(fullBody)
        android.util.Log.d("SpendlySms", "parsed=$parsed")
        if (parsed == null) return

        val sentAt = messages.firstOrNull()?.timestampMillis?.let {
            if (it > 0) {
                try {
                    Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
                } catch (e: Exception) {
                    LocalDateTime.now()
                }
            } else {
                LocalDateTime.now()
            }
        } ?: LocalDateTime.now()

        val app = context.applicationContext as? SpendlyApplication ?: return
        val autoSave = app.preferences.isSmsAutoSaveEnabled
        val transaction = parsed.toTransaction(
            autoSave = autoSave,
            date = sentAt.toLocalDate(),
            time = sentAt.toLocalTime().withNano(0)
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val exists = app.repository.countMatchingTransaction(
                    source = TransactionSource.SMS,
                    merchant = transaction.merchant,
                    amount = transaction.amount,
                    date = transaction.date
                ) > 0
                android.util.Log.d("SpendlySms", "Transaction exists=$exists, adding: $transaction")

                if (!exists) {
                    val id = app.repository.addTransaction(transaction)
                    android.util.Log.d("SpendlySms", "Transaction successfully added to repository!")
                    SmsNotifier.notifyNewExpense(app, transaction.copy(id = id))
                }
            } catch (e: Exception) {
                android.util.Log.e("SpendlySms", "Error adding transaction", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
