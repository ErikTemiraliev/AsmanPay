package com.eriktemiraliev.asmanpay.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.eriktemiraliev.asmanpay.resources.*
import org.jetbrains.compose.resources.Font

/**
 * Manrope, bundled (SIL Open Font License, see licenses/Manrope-OFL.txt).
 * The .ttf files are in shared/src/commonMain/composeResources/font so both
 * Android and iOS ship them. Loading them needs a composable, hence the getter.
 */
val Manrope: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.manrope_regular, FontWeight.Normal),
        Font(Res.font.manrope_medium, FontWeight.Medium),
        Font(Res.font.manrope_semibold, FontWeight.SemiBold),
        Font(Res.font.manrope_bold, FontWeight.Bold),
        Font(Res.font.manrope_extrabold, FontWeight.ExtraBold)
    )

private val base = Typography()

/** The app's text styles, all in [Manrope]. */
@Composable
fun asmanTypography(): Typography {
    val manrope = Manrope
    fun TextStyle.manrope() = copy(fontFamily = manrope)
    return Typography(
        displayLarge = base.displayLarge.manrope(),
        displayMedium = base.displayMedium.manrope(),
        displaySmall = base.displaySmall.manrope(),
        headlineLarge = base.headlineLarge.manrope().copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        headlineMedium = base.headlineMedium.manrope().copy(fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
        headlineSmall = base.headlineSmall.manrope().copy(fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.manrope().copy(fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.manrope().copy(fontWeight = FontWeight.Bold),
        titleSmall = base.titleSmall.manrope().copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.manrope(),
        bodyMedium = base.bodyMedium.manrope(),
        bodySmall = base.bodySmall.manrope(),
        labelLarge = base.labelLarge.manrope().copy(fontWeight = FontWeight.Bold),
        labelMedium = base.labelMedium.manrope().copy(fontWeight = FontWeight.SemiBold),
        labelSmall = base.labelSmall.manrope().copy(fontWeight = FontWeight.SemiBold)
    )
}
