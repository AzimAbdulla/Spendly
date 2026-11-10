package com.example.spendly.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.spendly.ui.theme.SpendlyTheme
import androidx.compose.runtime.CompositionLocalProvider
import com.example.spendly.ui.theme.LocalSpendlyColors
import com.example.spendly.ui.theme.TileTint
import com.example.spendly.ui.theme.inTile

@Composable
fun SpendlyCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SpendlyTheme.colors.cardBackground,
    borderBrush: Brush = glassRimBrush(),
    shape: Shape = RoundedCornerShape(24.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, borderBrush),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        content = content
    )
}

/** A card in a tile tint. Content that reads SpendlyTheme.colors inside it gets the tinted text, tracks and accent. */
@Composable
fun TintedTile(
    tint: TileTint,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = SpendlyTheme.colors
    CompositionLocalProvider(LocalSpendlyColors provides colors.inTile(tint)) {
        SpendlyCard(modifier = modifier, backgroundColor = tint.background, shape = shape, content = content)
    }
}
