package com.eriktemiraliev.asmanpay.exchange

import com.eriktemiraliev.asmanpay.platform.KeyValueStores
import com.eriktemiraliev.asmanpay.platform.httpGetText

/**
 * Fetches live reference rates and keeps the last good response on the device,
 * so the app can still show real (if older) rates when it's offline.
 */
class RateRepository(stores: KeyValueStores) {

    private val store = stores.open(STORE_NAME)

    /** The last live rates saved on this device, or null if none have been fetched yet. */
    fun cached(): RateTable? =
        store.getString(KEY_LAST_RESPONSE)
            ?.let { RateParser.parse(it, RateSource.CACHED) }

    /**
     * Downloads the latest rates. On success, saves them and returns them.
     * Returns null on any network or parsing failure.
     */
    suspend fun fetchLatest(): RateTable? {
        val body = httpGetText(RATES_URL, TIMEOUT_MILLIS) ?: return null
        val table = RateParser.parse(body, RateSource.LIVE) ?: return null
        store.putString(KEY_LAST_RESPONSE, body)
        return table
    }

    private companion object {
        // Free, no API key. Its terms ask apps to credit "Rates By Exchange Rate API".
        const val RATES_URL = "https://open.er-api.com/v6/latest/USD"
        const val STORE_NAME = "exchange_rates"
        const val KEY_LAST_RESPONSE = "last_response"
        const val TIMEOUT_MILLIS = 10_000L
    }
}
