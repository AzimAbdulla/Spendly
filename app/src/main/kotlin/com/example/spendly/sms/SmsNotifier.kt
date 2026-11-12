package com.example.spendly.sms

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.spendly.MainActivity
import com.example.spendly.R
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.data.currencySymbol

/** One notification per expense read from an incoming bank SMS; tapping it opens that expense. */
object SmsNotifier {
    const val EXTRA_TRANSACTION_ID = "com.example.spendly.TRANSACTION_ID"
    private const val CHANNEL_ID = "sms_expenses"

    /** Android 13+ asks separately; only worth asking once Spendly may read SMS. */
    fun needsPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    fun notifyNewExpense(context: Context, transaction: Transaction, defaultCurrency: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = NotificationManagerCompat.from(context)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName("SMS expenses")
                .setDescription("A new expense read from a bank SMS")
                .build()
        )

        val amount = "${currencySymbol(transaction.currency)}${String.format("%,.2f", transaction.amount)}"
        val merchant = transaction.merchant.takeIf { it.isNotBlank() && it != "Unknown Merchant" }
        val title = if (merchant != null) "$amount at $merchant" else "$amount from a bank SMS"
        val text = when {
            transaction.currency != defaultCurrency -> "In another currency. Tap to enter what it cost you"
            transaction.category == Category.OTHER -> "Tap to choose a category"
            transaction.status == TransactionStatus.NEEDS_REVIEW -> "Looks like ${transaction.category.displayName}. Tap to check it"
            else -> "Saved under ${transaction.category.displayName}. Tap to change it"
        }

        val open = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_TRANSACTION_ID, transaction.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val contentIntent = PendingIntent.getActivity(
            context,
            transaction.id.toInt(),
            open,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Amounts and merchants stay off the lock screen
        val lockScreenVersion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_spendly)
            .setContentTitle("New expense from a bank SMS")
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_spendly)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setPublicVersion(lockScreenVersion)
            .build()

        manager.notify(transaction.id.toInt(), notification)
    }
}
