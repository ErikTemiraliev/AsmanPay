package com.eriktemiraliev.silkrate.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SilkRateColorScheme = lightColorScheme(
    primary = SilkNavy,
    secondary = SilkBlue,
    background = SilkLight,
    surface = Color.White
)

@Composable
fun SilkRateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SilkRateColorScheme,
        typography = Typography,
        content = content
    )
}