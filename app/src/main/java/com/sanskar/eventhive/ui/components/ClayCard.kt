package com.sanskar.eventhive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A Card with Claymorphism effect (soft shadows and inner highlights).
 */
@Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    cornerRadius: Dp = 28.dp,
    elevation: Dp = 12.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shadowColor = Color.Black.copy(alpha = 0.1f)
    val highlightColor = Color.White.copy(alpha = 0.8f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val cornerRadiusPx = cornerRadius.toPx()
                val elevationPx = elevation.toPx()

                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        style = PaintingStyle.Fill
                    }
                    val frameworkPaint = paint.asFrameworkPaint()

                    // Outer Shadow (Bottom Right)
                    frameworkPaint.color = shadowColor.toArgb()
                    frameworkPaint.setShadowLayer(
                        elevationPx,
                        elevationPx / 2,
                        elevationPx / 2,
                        shadowColor.toArgb()
                    )
                    
                    val path = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(0f, 0f, size.width, size.height),
                                radiusX = cornerRadiusPx,
                                radiusY = cornerRadiusPx,
                            )
                        )
                    }
                    canvas.drawPath(path, paint)
                }
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
            .drawBehind {
                // Inner highlight (Top Left)
                val strokeWidth = 4.dp.toPx()
                val cornerRadiusPx = cornerRadius.toPx()
                
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = highlightColor
                        style = PaintingStyle.Stroke
                        this.strokeWidth = strokeWidth
                    }
                    
                    // Simple top-left highlight arc
                    val highlightPath = Path().apply {
                        moveTo(0f, size.height / 2)
                        lineTo(0f, cornerRadiusPx)
                        arcTo(
                            rect = Rect(0f, 0f, cornerRadiusPx * 2, cornerRadiusPx * 2),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        lineTo(size.width / 2, 0f)
                    }
                    canvas.drawPath(highlightPath, paint)
                }
            }
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp)
    ) {
        content()
    }
}
