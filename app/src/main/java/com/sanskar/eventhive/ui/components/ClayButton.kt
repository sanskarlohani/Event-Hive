package com.sanskar.eventhive.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ClayButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    cornerRadius: Dp = 24.dp,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null
) {
    val alpha = if (enabled) 1f else 0.5f
    val shadowColor = Color.Black.copy(alpha = 0.15f * alpha)
    val highlightColor = Color.White.copy(alpha = 0.4f * alpha)

    Box(
        modifier = modifier
            .drawBehind {
                val cornerRadiusPx = cornerRadius.toPx()
                val elevationPx = 8.dp.toPx()

                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        style = PaintingStyle.Fill
                    }
                    val frameworkPaint = paint.asFrameworkPaint()

                    // Outer Shadow
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
                                radiusY = cornerRadiusPx
                            )
                        )
                    }
                    canvas.drawPath(path, paint)
                }
            }
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor.copy(alpha = alpha))
            .drawBehind {
                val strokeWidth = 3.dp.toPx()
                val cornerRadiusPx = cornerRadius.toPx()
                
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = highlightColor
                        style = PaintingStyle.Stroke
                        this.strokeWidth = strokeWidth
                    }
                    
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
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                icon()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = textColor.copy(alpha = alpha)
            )
        }
    }
}
