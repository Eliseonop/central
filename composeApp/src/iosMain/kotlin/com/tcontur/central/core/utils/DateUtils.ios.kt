package com.tcontur.central.core.utils

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun currentFormattedTimestamp(): String {
    val fmt = NSDateFormatter()
    fmt.dateFormat = "yyyy-MM-dd HH:mm:ss"
    return fmt.stringFromDate(NSDate())
}

actual fun currentDateStr(): String {
    val fmt = NSDateFormatter()
    fmt.dateFormat = "yyyy-MM-dd"
    return fmt.stringFromDate(NSDate())
}

actual fun currentTimeMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000).toLong()
