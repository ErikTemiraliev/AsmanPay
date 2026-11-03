package com.eriktemiraliev.asmanpay.platform

import kotlin.time.Clock
import kotlin.uuid.Uuid

/*
 * The few things the shared code needs from the phone's own system. Each one has an
 * Android version (androidMain) and an iOS version (iosMain) with the same name.
 */

/**
 * Downloads [url] as text. Returns the body only for an HTTP 200 response,
 * and null on any network error, timeout or other status.
 */
expect suspend fun httpGetText(url: String, timeoutMillis: Long): String?

/** [value] in the user's locale, such as "1,000.5" or "1 000,5". */
expect fun formatDecimal(
    value: Double,
    minFractionDigits: Int,
    maxFractionDigits: Int,
    useGrouping: Boolean = true
): String

/** A date in the user's locale's medium style, such as "Nov 3, 2026". */
expect fun formatDate(epochMillis: Long): String

/** A date and time in the user's locale, such as "Nov 3, 2026, 10:15 AM". */
expect fun formatDateTime(epochMillis: Long): String

/** A new random id for a saved item. */
fun newId(): String = Uuid.random().toString()

/** The current time in Unix milliseconds. */
fun nowMillis(): Long = Clock.System.now().toEpochMilliseconds()
