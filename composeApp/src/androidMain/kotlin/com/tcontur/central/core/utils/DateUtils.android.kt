package com.tcontur.central.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

//actual fun currentFormattedTimestamp(): String =
//    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

//actual fun currentDateStr(): String =
//    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

actual fun currentTimeMillis(): Long = System.currentTimeMillis()
actual fun currentDateStr(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        .apply { timeZone = TimeZone.getDefault() }
        .format(Date())

actual fun currentFormattedTimestamp(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        .apply { timeZone = TimeZone.getDefault() }
        .format(Date())