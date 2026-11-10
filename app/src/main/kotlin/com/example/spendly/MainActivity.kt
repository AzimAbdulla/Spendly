package com.example.spendly

import android.Manifest
import android.content.Intent
import android.os.Build
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
import com.example.spendly.ui.components.LaunchGate
import com.example.spendly.ui.theme.Motion
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.widget.WidgetTarget
import com.example.spendly.widget.widgetTarget

class MainActivity : ComponentActivity() {
    // Expense to open, set when an SMS expense notification or a widget row is tapped
    private var openTransactionId by mutableStateOf<Long?>(null)

    // Screen to open, set when another part of a home screen widget is tapped
    private var widgetTarget by mutableStateOf<WidgetTarget?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The system's splash fades out slowly enough to hide Home's opening animations (totals
        // counting up, charts growing), so it gets a quick fade instead
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splash ->
                splash.animate().alpha(0f).setDuration(Motion.Short.toLong()).withEndAction {
                    splash.remove()
                    LaunchGate.open = true
                }.start()
            }
        }
        if (savedInstanceState == null) {
            openTransactionId = intent.transactionIdExtra()
            widgetTarget = intent.widgetTarget()
        }

        enableEdgeToEdge()
        setContent {
            val app = LocalContext.current.applicationContext as SpendlyApplication
            val viewModel: SpendlyViewModel = viewModel(
                factory = SpendlyViewModel.provideFactory(app.repository, app.preferences)
            )
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK", "BLACK" -> true
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

            SpendlyTheme(darkTheme = isDark, pureBlack = themeMode == "BLACK") {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        viewModel = viewModel,
                        openTransactionId = openTransactionId,
                        onTransactionOpened = { openTransactionId = null },
                        widgetTarget = widgetTarget,
                        onWidgetTargetOpened = { widgetTarget = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.transactionIdExtra()?.let { openTransactionId = it }
        intent.widgetTarget()?.let { widgetTarget = it }
    }
}

// Ignore the extra when Android replays the launching intent from Recents
private fun Intent.transactionIdExtra(): Long? =
    getLongExtra(SmsNotifier.EXTRA_TRANSACTION_ID, 0L)
        .takeIf { it > 0 && flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0 }
