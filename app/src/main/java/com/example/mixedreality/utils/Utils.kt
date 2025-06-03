package com.example.mixedreality.utils

import android.graphics.Bitmap
import kotlin.math.abs
import kotlin.math.roundToInt

class Utils {

    companion object {
        fun acceptOffset(bitmap: Bitmap, offset: Int): Bitmap {
            val width = bitmap.width
            val height = bitmap.height
            val newEdge = minOf(width, height)

            var xOffset = 0
            var yOffset = 0

            val diff = abs(width - height) / 200.0
            val kOffset = (diff * offset).roundToInt()
            if (newEdge == width) {
                yOffset = (height - newEdge) / 2 - kOffset
            } else if (newEdge == height) {
                xOffset = (width - newEdge) / 2 - kOffset
            }
            xOffset = xOffset.coerceIn(0, width - newEdge)
            yOffset = yOffset.coerceIn(0, height - newEdge)

            return Bitmap.createBitmap(bitmap, xOffset, yOffset, newEdge, newEdge)
        }
    }
}