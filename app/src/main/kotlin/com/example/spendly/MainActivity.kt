package com.example.spendly

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.spendly.sms.SmsNotifier
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.ui.theme.SpendlyTheme

class MainActivity : ComponentActivity() {
    // Expense to open, set when an SMS expense notification is tapped
    private var openTransactionId by mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) openTransactionId = intent.transactionIdExtra()

        enableEdgeToEdge()
        setContent {
            val app = LocalContext.current.applicationContext as SpendlyApplication
            val viewModel: SpendlyViewModel = viewModel(
                factory = SpendlyViewModel.provideFactory(app.repository, app.preferences)
            )
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            // Once SMS reading is allowed, ask (once per launch) so new SMS expenses can notify
            val notificationPermission = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { }
            var askedForNotifications by rememberSaveable { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                if (!askedForNotifications && SmsNotifier.needsPermission(this@MainActivity)) {
                    askedForNotifications = true
                    notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            SpendlyTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        viewModel = viewModel,
                        openTransactionId = openTransactionId,
                        onTransactionOpened = { openTransactionId = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.transactionIdExtra()?.let { openTransactionId = it }
    }
}

// Ignore the extra when Android replays the launching intent from Recents
private fun Intent.transactionIdExtra(): Long? =
    getLongExtra(SmsNotifier.EXTRA_TRANSACTION_ID, 0L)
        .takeIf { it > 0 && flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0 }
