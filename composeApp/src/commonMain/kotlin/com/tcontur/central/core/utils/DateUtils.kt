package com.tcontur.central.core.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Returns the current timestamp formatted as "YYYY-MM-DD HH:mm:ss"
 * which is the format the Tcontur backend expects.
 */
fun currentFormattedTimestamp(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return "%04d-%02d-%02d %02d:%02d:%02d".format(
        now.year, now.monthNumber, now.dayOfMonth,
        now.hour, now.minute, now.second
    )
}
