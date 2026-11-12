package com.example.spendly.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.spendly.SpendlyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

// Nothing here is logged: an SMS can hold a one-time password or a balance
class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        val app = context.applicationContext as? SpendlyApplication ?: return
        // "Read bank SMS" in Settings is the off switch
        if (!app.preferences.isSmsAutoSaveEnabled) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)?.takeIf { it.isNotEmpty() } ?: return
        val first = messages.first()
        if (!SmsParser.isBankSender(first.originatingAddress)) return

        val fullBody = messages.joinToString("") { it.displayMessageBody ?: "" }
        val parsed = SmsParser.parse(fullBody) ?: return

        val sent = first.timestampMillis.takeIf { it > 0 } ?: System.currentTimeMillis()
        val sentAt = Instant.ofEpochMilli(sent).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val key = SmsParser.smsKey(sent, fullBody)
        val transaction = parsed.toTransaction(
            autoSave = true,
            date = sentAt.toLocalDate(),
            time = sentAt.toLocalTime().withNano(0),
            smsKey = key,
            defaultCurrency = app.preferences.defaultCurrency,
            learnedCategory = app.preferences.learnedCategory(parsed.merchant)
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!app.repository.isSmsRecorded(key, transaction.merchant, transaction.amount, transaction.date)) {
                    val id = app.repository.addTransaction(transaction)
                    SmsNotifier.notifyNewExpense(app, transaction.copy(id = id), app.preferences.defaultCurrency)
                }
            } catch (e: Exception) {
                // A failed save must not crash the receiver; the next inbox scan picks it up
            } finally {
                pendingResult.finish()
            }
        }
    }
}
