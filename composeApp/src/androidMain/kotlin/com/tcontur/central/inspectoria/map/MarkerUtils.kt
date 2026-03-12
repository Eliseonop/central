package com.tcontur.central.inspectoria.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.tcontur.central.R

private const val TAG = "[TCONTUR][MARKER_UTILS]"

/**
 * Creates a blue-dot drawable for the inspector's current GPS position.
 *
 * Visual: white outer ring + Google-blue inner circle + subtle drop shadow.
 * Size is 36×36 px (device-independent visual weight; osmdroid scales by density).
 */
fun createGpsLocationDrawable(context: Context): Drawable {
    val size = 36
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val center = size / 2f
    val outerRadius = size / 2f - 2f
    val innerRadius = outerRadius * 0.55f

    // Drop shadow
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(60, 0, 0, 0)
        maskFilter = android.graphics.BlurMaskFilter(4f, android.graphics.BlurMaskFilter.Blur.NORMAL)
    }
    canvas.drawCircle(center, center + 2f, outerRadius * 0.85f, shadowPaint)

    // White ring
    val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, outerRadius, whitePaint)

    // Google-blue fill
    val bluePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(255, 66, 133, 244)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(center, center, innerRadius, bluePaint)

    return bitmap.toDrawable(context.resources)
}

/**
 * Creates a bus-icon drawable with the [padron] number painted on top.
 *
 * @param isNearest  `true` → green bus (bus_verde.png), `false` → blue bus (bus_azul.png)
 */
fun createCustomMarkerDrawable(
    context: Context,
    padron: String,
    isNearest: Boolean = false
): Drawable? {
    return try {
        val drawableRes = if (isNearest) R.drawable.bus_verde else R.drawable.bus_azul
        val baseDrawable = ContextCompat.getDrawable(context, drawableRes) ?: return null

        val markerWidth  = 54
        val markerHeight = 64
        val baseBitmap   = baseDrawable.toBitmap(markerWidth, markerHeight, Bitmap.Config.ARGB_8888)
        val mutableBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val textPaint = Paint().apply {
            color     = Color.BLACK
            textSize  = (minOf(markerWidth, markerHeight) * 0.43f).coerceIn(16f, 32f)
            typeface  = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        // Draw padron text centred at 78 % of the marker height
        canvas.drawText(padron, mutableBitmap.width / 2f, mutableBitmap.height * 0.78f, textPaint)
        mutableBitmap.toDrawable(context.resources)
    } catch (e: Exception) {
        Log.e(TAG, "Error creando marker drawable: ${e.message}")
        null
    }
}
