package za.co.woolworths.financial.services.android.ui.views.card_swipe.internal

import android.content.Context
import android.graphics.Point

class SwipeUtils {
    companion object {
        fun toPx(context: Context, dp: Float): Float {
            val scale = context.resources.displayMetrics.density
            return dp * scale + 0.5f
        }

        fun getRadian(x1: Float, y1: Float, x2: Float, y2: Float): Double {
            val width = x2 - x1
            val height = y1 - y2
            return Math.atan(Math.abs(height) / Math.abs(width).toDouble())
        }

        fun getTargetPoint(x1: Float, y1: Float, x2: Float, y2: Float): Point {
            val radius = 2000f
            var radian =
                    getRadian(x1, y1, x2, y2)
            val quadrant =
                    getQuadrant(x1, y1, x2, y2)
            if (quadrant === Quadrant.TopLeft) {
                var degree = Math.toDegrees(radian)
                degree = 180 - degree
                radian = Math.toRadians(degree)
            } else if (quadrant === Quadrant.BottomLeft) {
                var degree = Math.toDegrees(radian)
                degree = 180 + degree
                radian = Math.toRadians(degree)
            } else if (quadrant === Quadrant.BottomRight) {
                var degree = Math.toDegrees(radian)
                degree = 360 - degree
                radian = Math.toRadians(degree)
            } else {
                val degree = Math.toDegrees(radian)
                radian = Math.toRadians(degree)
            }
            val x = radius * Math.cos(radian)
            val y = radius * Math.sin(radian)
            return Point(x.toInt(), y.toInt())
        }

        fun getQuadrant(x1: Float, y1: Float, x2: Float, y2: Float): Quadrant {
            return if (x2 > x1) { // Right
                if (y2 > y1) { // Bottom
                    Quadrant.BottomRight
                } else { // Top
                    Quadrant.TopRight
                }
            } else { // Left
                if (y2 > y1) { // Bottom
                    Quadrant.BottomLeft
                } else { // Top
                    Quadrant.TopLeft
                }
            }
        }
    }
}