package com.eriktemiraliev.asmanpay.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val AsmanPayColorScheme = lightColorScheme(
    primary = AsmanBlue,
    onPrimary = Color.White,
    primaryContainer = AsmanMist,
    onPrimaryContainer = AsmanInk,
    secondary = AsmanInk,
    onSecondary = Color.White,
    background = AsmanCloud,
    onBackground = AsmanInk,
    surface = Color.White,
    onSurface = AsmanInk,
    surfaceVariant = AsmanMist,
    onSurfaceVariant = AsmanSlate,
    outline = Color(0xFFD5DBEE),
    error = AsmanError
)

private val AsmanPayShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun AsmanPayTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AsmanPayColorScheme,
        typography = Typography,
        shapes = AsmanPayShapes,
        content = content
    )
}
