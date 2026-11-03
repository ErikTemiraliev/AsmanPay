package com.eriktemiraliev.asmanpay.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eriktemiraliev.asmanpay.ui.theme.AsmanGradient

/**
 * The Asman Pay mark: a single fluid pen-stroke "A" on the sky gradient. Same
 * shape as the launcher icon (res/drawable/ic_launcher_foreground.xml), drawn on
 * a 100-unit grid.
 */
@Composable
fun AsmanLogo(size: Dp = 44.dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(AsmanGradient)
    ) {
        AsmanMark(color = Color.White, modifier = Modifier.size(size))
    }
}

/** Just the stroke "A", without the tile behind it. */
@Composable
fun AsmanMark(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        // Fit the 100-unit design into the middle 66% like the adaptive icon's safe zone.
        val unit = this.size.minDimension / 100f * 0.66f
        val origin = Offset(
            (this.size.width - 100f * unit) / 2f,
            (this.size.height - 100f * unit) / 2f
        )
        fun p(x: Float, y: Float) = Offset(origin.x + x * unit, origin.y + y * unit)

        val letter = Path().apply {
            p(16f, 74f).let { moveTo(it.x, it.y) }
            cubic(p(24f, 82f), p(30f, 76f), p(33f, 68f))
            p(45f, 32f).let { lineTo(it.x, it.y) }
            cubic(p(47f, 24f), p(53f, 24f), p(55f, 32f))
            p(67f, 68f).let { lineTo(it.x, it.y) }
            cubic(p(70f, 76f), p(76f, 82f), p(84f, 74f))
        }
        val bar = Path().apply {
            p(37f, 57f).let { moveTo(it.x, it.y) }
            cubic(p(45f, 52f), p(55f, 52f), p(63f, 57f))
        }
        val stroke = Stroke(width = 9f * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        drawPath(letter, color, style = stroke)
        drawPath(bar, color, style = stroke)
    }
}

private fun Path.cubic(c1: Offset, c2: Offset, end: Offset) =
    cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)

/** A full-width card filled with the brand gradient, for hero content. */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(AsmanGradient)
    ) {
        // Two soft "clouds" for depth.
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.10f),
                radius = size.width * 0.45f,
                center = Offset(size.width * 0.95f, -size.height * 0.05f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.07f),
                radius = size.width * 0.30f,
                center = Offset(size.width * 0.10f, size.height * 1.05f)
            )
        }
        Box(modifier = Modifier.padding(24.dp), content = content)
    }
}
