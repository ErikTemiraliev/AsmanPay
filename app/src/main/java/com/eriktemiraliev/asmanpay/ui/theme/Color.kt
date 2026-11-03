package com.eriktemiraliev.asmanpay.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Asman Pay palette: a sky blue brand ("asman" means sky) on soft, airy surfaces,
// in the spirit of Meta's Muse: one confident blue, rounded shapes, friendly type.

/** Deep blue-black used for text and dark accents. */
val AsmanInk = Color(0xFF0E1A3D)

/** The brand blue. */
val AsmanBlue = Color(0xFF2F5BFF)

/** A lighter sky blue, used where the gradient ends. */
val AsmanSky = Color(0xFF7FAEFF)

/** App background: a very light, slightly blue grey. */
val AsmanCloud = Color(0xFFF4F6FC)

/** Soft blue tint for chips, inputs and selected states. */
val AsmanMist = Color(0xFFE9EEFF)

/** Secondary text. */
val AsmanSlate = Color(0xFF6B7391)

val AsmanError = Color(0xFFE5484D)

/** The signature gradient, used on hero cards and the launcher icon. */
val AsmanGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1F3DFF), Color(0xFF3F6BFF), AsmanSky)
)
