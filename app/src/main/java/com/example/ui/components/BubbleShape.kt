package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Natural, proportional messenger speech bubble with a smooth tail at the bottom-right.
 */
class SayItBubbleShape(
    private val cornerRadiusDp: Dp = 22.dp,
    private val sharpCornerRadiusDp: Dp = 8.dp,
    private val tailWidthDp: Dp = 14.dp,
    private val tailHeightDp: Dp = 7.dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val r = with(density) { cornerRadiusDp.toPx() }
        val sharpR = with(density) { sharpCornerRadiusDp.toPx() }
        val tailW = with(density) { tailWidthDp.toPx() }
        val tailH = with(density) { tailHeightDp.toPx() }

        val bodyHeight = (size.height - tailH).coerceAtLeast(10f)
        val bodyWidth = size.width

        val path = Path().apply {
            // Main message bubble body with balanced, natural rounded corners
            addRoundRect(
                RoundRect(
                    rect = Rect(0f, 0f, bodyWidth, bodyHeight),
                    topLeft = CornerRadius(r, r),
                    topRight = CornerRadius(r, r),
                    bottomLeft = CornerRadius(r, r),
                    bottomRight = CornerRadius(sharpR, sharpR)
                )
            )

            // Natural, proportional messenger tail at bottom-right
            val tail = Path().apply {
                moveTo(bodyWidth - sharpR - tailW, bodyHeight)
                quadraticBezierTo(
                    bodyWidth - tailW * 0.35f, bodyHeight,
                    bodyWidth - 1f, bodyHeight + tailH
                )
                quadraticBezierTo(
                    bodyWidth, bodyHeight + tailH * 0.4f,
                    bodyWidth, bodyHeight - sharpR
                )
                close()
            }
            op(this, tail, PathOperation.Union)
        }

        return Outline.Generic(path)
    }
}
