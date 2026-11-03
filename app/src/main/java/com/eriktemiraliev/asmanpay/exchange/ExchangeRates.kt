package com.eriktemiraliev.asmanpay.exchange

/** Where a [RateTable] came from. */
enum class RateSource {
    /** Built-in demo values, used before any live rates have been fetched. */
    SAMPLE,

    /** The last live rates that were fetched, loaded from the device. */
    CACHED,

    /** Rates fetched from the network during this session. */
    LIVE
}

/**
 * A set of reference rates, stored as "units of this currency per 1 USD".
 *
 * Every pair is derived through USD, so both directions of a pair stay
 * consistent (A→B→A always returns the original amount).
 */
data class RateTable(
    val unitsPerUsd: Map<String, Double>,
    val source: RateSource = RateSource.SAMPLE,
    /** When the provider last updated these rates, in Unix seconds. Null for sample rates. */
    val updatedAtEpochSeconds: Long? = null
) {
    /** Units of [to] you get for 1 unit of [from], or null if either is missing. */
    fun rate(from: String, to: String): Double? {
        val fromPerUsd = unitsPerUsd[from] ?: return null
        val toPerUsd = unitsPerUsd[to] ?: return null
        return toPerUsd / fromPerUsd
    }
}

object ExchangeRates {

    /** Supported currency codes, in the order the UI shows them. */
    val supportedCurrencies: List<String> = listOf("CNY", "USD", "KGS", "KZT")

    /** Fixed demo values, used until live rates are available. */
    val sample = RateTable(
        unitsPerUsd = mapOf(
            "CNY" to 7.20,
            "USD" to 1.0,
            "KGS" to 87.80,
            "KZT" to 520.0
        )
    )

    fun isSupported(currency: String): Boolean = currency in supportedCurrencies

    /** Units of [to] per 1 unit of [from] at the sample rates, or null if either is unsupported. */
    fun rate(from: String, to: String): Double? = sample.rate(from, to)
}

/**
 * Converts [amount] from one currency to another using [table].
 * Returns 0.0 when either currency is missing from the table.
 */
fun calculateExchange(
    amount: Double,
    from: String,
    to: String,
    table: RateTable = ExchangeRates.sample
): Double {
    if (from == to) return amount
    val rate = table.rate(from, to) ?: return 0.0
    return amount * rate
}
