package com.example.spendly.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.SpendlyApplication
import com.example.spendly.data.SpendlyPreferences
import com.example.spendly.ui.components.GlassSegmentedControl
import com.example.spendly.ui.components.ScreenHeader
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.theme.SpendlyTheme
import kotlin.math.roundToInt

/**
 * Settings shared by every Spendly widget. Opens from the app's Settings, and from the
 * launcher when a widget is reconfigured (One UI: long-press the widget, then Settings).
 */
class WidgetSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        // Leaving without Done keeps the widget as it was
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        val prefs = (application as SpendlyApplication).preferences

        enableEdgeToEdge()
        setContent {
            val dark = when (prefs.themeMode) {
                "LIGHT" -> false
                "DARK", "BLACK" -> true
                else -> isSystemInDarkTheme()
            }
            SpendlyTheme(darkTheme = dark, pureBlack = prefs.themeMode == "BLACK") {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WidgetSettingsScreen(
                        prefs = prefs,
                        onBack = { finish() },
                        onDone = {
                            // Saving the preferences updates every widget (see SpendlyApplication)
                            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WidgetSettingsScreen(prefs: SpendlyPreferences, onBack: () -> Unit, onDone: () -> Unit) {
    val colors = SpendlyTheme.colors
    var light by remember { mutableStateOf(prefs.widgetLightBackground) }
    var transparency by remember { mutableFloatStateOf(prefs.widgetTransparency.toFloat()) }
    var hideAmounts by remember { mutableStateOf(prefs.widgetHideAmounts) }
    var colour by remember { mutableStateOf(prefs.widgetColour) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title = "Widgets", onBack = onBack)
        Text(
            text = "These apply to every Spendly widget on your home screen.",
            fontSize = 13.sp,
            color = colors.textSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        SpendlyCard(shape = RoundedCornerShape(24.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Background", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                    GlassSegmentedControl(
                        items = listOf(false to "Dark", true to "Light"),
                        selected = light,
                        onSelect = { light = it; prefs.widgetLightBackground = it }
                    )
                }

                HorizontalDivider(color = colors.divider)

                // How the widgets use colour: a wash per widget, colour only in the data, or coloured figures
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Colour", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                    GlassSegmentedControl(
                        items = listOf(WidgetColour.TINTED to "Tinted", WidgetColour.DATA to "Data", WidgetColour.FIGURES to "Figures"),
                        selected = colour,
                        onSelect = { colour = it; prefs.widgetColour = it }
                    )
                    Text(
                        text = when (colour) {
                            WidgetColour.TINTED -> "Each widget gets a soft wash of its own colour, even at full transparency"
                            WidgetColour.DATA -> "Plain backgrounds, with colour only in the bars, ring and dots"
                            else -> "Plain backgrounds, and each widget's big figure takes its colour"
                        },
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                HorizontalDivider(color = colors.divider)

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Transparency", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        Text("${transparency.roundToInt()}%", fontSize = 13.sp, color = colors.textSecondary)
                    }
                    Slider(
                        value = transparency,
                        onValueChange = {
                            transparency = it
                            // Saved as it moves; the widgets catch up a moment after it settles
                            if (it.roundToInt() != prefs.widgetTransparency) prefs.widgetTransparency = it.roundToInt()
                        },
                        valueRange = 0f..100f,
                        steps = 19,
                        colors = SliderDefaults.colors(
                            thumbColor = colors.primary,
                            activeTrackColor = colors.primary,
                            inactiveTrackColor = colors.containerHigh,
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )
                }

                HorizontalDivider(color = colors.divider)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hide amounts", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = colors.textPrimary)
                        Text(
                            "Shows ₹ •••• instead of amounts on your home screen",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = hideAmounts,
                        onCheckedChange = { hideAmounts = it; prefs.widgetHideAmounts = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = colors.containerHigh,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                // Each setting is already saved and showing on the widgets
                onDone()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = colors.onPrimary)
        ) {
            Text("Done", fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(4.dp))
    }
}
