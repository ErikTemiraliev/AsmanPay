package com.eriktemiraliev.asmanpay.exchange

/**
 * Parses a response from ExchangeRate-API's open endpoint
 * (https://open.er-api.com/v6/latest/USD), which looks like:
 *
 * {"result":"success","time_last_update_unix":1730592000,"base_code":"USD",
 *  "rates":{"USD":1,"CNY":7.12,"KGS":86.5,"KZT":495.3, ...}}
 *
 * Only the supported currencies are kept. Returns null unless the response
 * is a USD-based success that has a positive rate for every supported currency,
 * so the app never mixes live rates with gaps.
 *
 * This is a small hand-rolled parser so it can be unit tested on the JVM,
 * where Android's org.json is only a stub.
 */
object RateParser {

    private val resultRegex = Regex("\"result\"\\s*:\\s*\"([^\"]*)\"")
    private val baseRegex = Regex("\"base_code\"\\s*:\\s*\"([^\"]*)\"")
    private val updatedRegex = Regex("\"time_last_update_unix\"\\s*:\\s*(\\d+)")
    private val ratesBlockRegex = Regex("\"rates\"\\s*:\\s*\\{([^}]*)\\}")

    fun parse(json: String, source: RateSource = RateSource.LIVE): RateTable? {
        if (resultRegex.find(json)?.groupValues?.get(1) != "success") return null
        if (baseRegex.find(json)?.groupValues?.get(1) != "USD") return null
        val ratesBlock = ratesBlockRegex.find(json)?.groupValues?.get(1) ?: return null

        val rates = mutableMapOf<String, Double>()
        for (code in ExchangeRates.supportedCurrencies) {
            val value = Regex("\"$code\"\\s*:\\s*([-+0-9.eE]+)")
                .find(ratesBlock)
                ?.groupValues?.get(1)
                ?.toDoubleOrNull()
            if (value == null || value <= 0.0 || value.isNaN() || value.isInfinite()) return null
            rates[code] = value
        }

        return RateTable(
            unitsPerUsd = rates,
            source = source,
            updatedAtEpochSeconds = updatedRegex.find(json)?.groupValues?.get(1)?.toLongOrNull()
        )
    }
}
