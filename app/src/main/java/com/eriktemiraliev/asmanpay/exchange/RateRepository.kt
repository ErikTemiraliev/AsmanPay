package com.eriktemiraliev.asmanpay.exchange

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fetches live reference rates and keeps the last good response on the device,
 * so the app can still show real (if older) rates when it's offline.
 */
class RateRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** The last live rates saved on this device, or null if none have been fetched yet. */
    fun cached(): RateTable? =
        prefs.getString(KEY_LAST_RESPONSE, null)
            ?.let { RateParser.parse(it, RateSource.CACHED) }

    /**
     * Downloads the latest rates. On success, saves them and returns them.
     * Returns null on any network or parsing failure.
     */
    suspend fun fetchLatest(): RateTable? = withContext(Dispatchers.IO) {
        runCatching {
            val connection = URL(RATES_URL).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = TIMEOUT_MILLIS
                connection.readTimeout = TIMEOUT_MILLIS
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@runCatching null

                val body = connection.inputStream.bufferedReader().use { it.readText() }
                val table = RateParser.parse(body, RateSource.LIVE) ?: return@runCatching null
                prefs.edit().putString(KEY_LAST_RESPONSE, body).apply()
                table
            } finally {
                connection.disconnect()
            }
        }.getOrNull()
    }

    private companion object {
        // Free, no API key. Its terms ask apps to credit "Rates By Exchange Rate API".
        const val RATES_URL = "https://open.er-api.com/v6/latest/USD"
        const val PREFS_NAME = "exchange_rates"
        const val KEY_LAST_RESPONSE = "last_response"
        const val TIMEOUT_MILLIS = 10_000
    }
}
