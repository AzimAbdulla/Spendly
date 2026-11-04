package com.example.spendly.sms

import android.content.Context
import android.provider.Telephony
import com.example.spendly.data.TransactionRepository
import com.example.spendly.data.model.TransactionSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

data class SmsScanResult(
    val scannedCount: Int,
    val importedCount: Int,
    val skippedDuplicates: Int
)

object SmsReader {

    suspend fun scanInbox(
        context: Context,
        repository: TransactionRepository,
        autoSave: Boolean,
        limit: Int = 100
    ): SmsScanResult = withContext(Dispatchers.IO) {
        var scanned = 0
        var imported = 0
        var duplicates = 0

        val contentResolver = context.contentResolver
        val projection = arrayOf(
            Telephony.Sms.Inbox.BODY,
            Telephony.Sms.Inbox.DATE
        )

        try {
            contentResolver.query(
                Telephony.Sms.Inbox.CONTENT_URI,
                projection,
                null,
                null,
                "${Telephony.Sms.Inbox.DATE} DESC LIMIT $limit"
            )?.use { cursor ->
                val bodyIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.BODY)
                val dateIndex = cursor.getColumnIndex(Telephony.Sms.Inbox.DATE)

                while (cursor.moveToNext()) {
                    scanned++
                    val body = if (bodyIndex != -1) cursor.getString(bodyIndex) ?: "" else ""
                    val epochMs = if (dateIndex != -1) cursor.getLong(dateIndex) else 0L

                    val parsed = SmsParser.parse(body) ?: continue

                    val sentAt = if (epochMs > 0) {
                        try {
                            Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault()).toLocalDateTime()
                        } catch (e: Exception) {
                            LocalDateTime.now()
                        }
                    } else {
                        LocalDateTime.now()
                    }
                    val date = sentAt.toLocalDate()

                    val existingCount = repository.countMatchingTransaction(
                        source = TransactionSource.SMS,
                        merchant = parsed.merchant,
                        amount = parsed.amount,
                        date = date
                    )

                    if (existingCount > 0) {
                        duplicates++
                    } else {
                        val transaction = parsed.toTransaction(
                            autoSave = autoSave,
                            date = date,
                            time = sentAt.toLocalTime().withNano(0)
                        )
                        repository.addTransaction(transaction)
                        imported++
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        SmsScanResult(
            scannedCount = scanned,
            importedCount = imported,
            skippedDuplicates = duplicates
        )
    }
}
