package com.eriktemiraliev.asmanpay.platform

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date

actual suspend fun httpGetText(url: String, timeoutMillis: Long): String? = withContext(Dispatchers.IO) {
    runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = timeoutMillis.toInt()
            connection.readTimeout = timeoutMillis.toInt()
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }.getOrNull()
}

actual fun formatDecimal(
    value: Double,
    minFractionDigits: Int,
    maxFractionDigits: Int,
    useGrouping: Boolean
): String = NumberFormat.getNumberInstance().apply {
    minimumFractionDigits = minFractionDigits
    maximumFractionDigits = maxFractionDigits
    isGroupingUsed = useGrouping
}.format(value)

actual fun formatDate(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))

actual fun formatDateTime(epochMillis: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(epochMillis))
