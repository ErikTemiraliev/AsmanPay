package com.eriktemiraliev.asmanpay.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AsmanPayColorScheme = lightColorScheme(
    primary = AsmanNavy,
    secondary = AsmanBlue,
    background = AsmanLight,
    surface = Color.White
)

@Composable
fun AsmanPayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AsmanPayColorScheme,
        typography = Typography,
        content = content
    )
}