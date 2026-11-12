package com.example.spendly.sms

import android.content.Context
import android.provider.Telephony
import com.example.spendly.data.SpendlyPreferences
import com.example.spendly.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

data class SmsScanResult(
    val scannedCount: Int,
    val importedCount: Int,
    val skippedDuplicates: Int,
    // False when the inbox couldn't be read at all (no permission, or the phone refused)
    val readable: Boolean = true
)

object SmsReader {
    /** How far back "Scan past SMS" looks. */
    const val ScanDays = 90L

    suspend fun scanInbox(
        context: Context,
        repository: TransactionRepository,
        preferences: SpendlyPreferences
    ): SmsScanResult = withContext(Dispatchers.IO) {
        var scanned = 0
        var imported = 0
        var duplicates = 0
        var readable = true
        val since = System.currentTimeMillis() - ScanDays * 24 * 60 * 60 * 1000
        val defaultCurrency = preferences.defaultCurrency

        try {
            context.contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                arrayOf(
                    Telephony.Sms.Inbox.ADDRESS,
                    Telephony.Sms.Inbox.BODY,
                    Telephony.Sms.Inbox.DATE,
                    Telephony.Sms.Inbox.DATE_SENT
                ),
                "${Telephony.Sms.Inbox.DATE} >= ?",
                arrayOf(since.toString()),
                "${Telephony.Sms.Inbox.DATE} DESC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    scanned++
                    if (!SmsParser.isBankSender(cursor.getString(0))) continue
                    val body = cursor.getString(1) ?: continue
                    val parsed = SmsParser.parse(body) ?: continue

                    // The time the bank sent it, which is what the receiver sees as it arrives
                    val received = cursor.getLong(2)
                    val sent = cursor.getLong(3).takeIf { it > 0 } ?: received
                    val sentAt = Instant.ofEpochMilli(sent).atZone(ZoneId.systemDefault()).toLocalDateTime()
                    val key = SmsParser.smsKey(sent, body)

                    if (repository.isSmsRecorded(key, parsed.merchant, parsed.amount, sentAt.toLocalDate())) {
                        duplicates++
                    } else {
                        repository.addTransaction(
                            parsed.toTransaction(
                                autoSave = true,
                                date = sentAt.toLocalDate(),
                                time = sentAt.toLocalTime().withNano(0),
                                smsKey = key,
                                defaultCurrency = defaultCurrency,
                                learnedCategory = preferences.learnedCategory(parsed.merchant)
                            )
                        )
                        imported++
                    }
                }
            } ?: run { readable = false }
        } catch (e: Exception) {
            readable = false
        }

        SmsScanResult(scanned, imported, duplicates, readable)
    }
}
