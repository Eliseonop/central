package com.tcontur.central.core.utils

/**
 * Returns the current timestamp formatted as "YYYY-MM-DD HH:mm:ss"
 * which is the format the Tcontur backend expects.
 *
 * Implemented per-platform to avoid Clock.System / String.format limitations in K2 commonMain.
 */
expect fun currentFormattedTimestamp(): String

/**
 * Returns the current time in milliseconds since epoch.
 * Used by QrParser to validate QR v1 timestamps without relying on
 * Clock.System (which has K2/commonMain limitations in this project).
 */
expect fun currentTimeMillis(): Long

/**
 * Returns today's date as "yyyy-MM-dd" in the device's local timezone.
 */
expect fun currentDateStr(): String

/**
 * Formats a Double with the given number of decimal places.
 * Pure Kotlin - no String.format (not available in KMP commonMain).
 */
fun Double.toDecimalStr(places: Int = 2): String {
    val factor = powi(10, places)
    val scaled = (this * factor + 0.5).toLong()
    val intPart = scaled / factor
    val decPart = (scaled % factor).coerceAtLeast(0L)
    return "$intPart.${decPart.toString().padStart(places, '0')}"
}

private fun powi(base: Int, exp: Int): Long {
    var r = 1L
    repeat(exp) { r *= base }
    return r
}
