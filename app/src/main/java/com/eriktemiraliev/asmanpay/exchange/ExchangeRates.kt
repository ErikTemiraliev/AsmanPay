package com.eriktemiraliev.asmanpay.exchange

/**
 * Sample reference rates for the currencies AsmanPay supports.
 *
 * Every rate is stored once, as "units of this currency per 1 USD", and any
 * pair is derived through USD. That keeps the two directions of a pair
 * consistent (A→B→A always returns the original amount) and means adding a
 * currency is a single new entry instead of a new case for every pair.
 *
 * These are fixed demo values, not live market rates.
 */
object ExchangeRates {

    private val unitsPerUsd: Map<String, Double> = linkedMapOf(
        "CNY" to 7.20,
        "USD" to 1.0,
        "KGS" to 87.80,
        "KZT" to 520.0
    )

    /** Supported currency codes, in the order the UI shows them. */
    val supportedCurrencies: List<String> = unitsPerUsd.keys.toList()

    fun isSupported(currency: String): Boolean = currency in unitsPerUsd

    /** Units of [to] you get for 1 unit of [from], or null if either is unsupported. */
    fun rate(from: String, to: String): Double? {
        val fromPerUsd = unitsPerUsd[from] ?: return null
        val toPerUsd = unitsPerUsd[to] ?: return null
        return toPerUsd / fromPerUsd
    }
}

/**
 * Converts [amount] from one currency to another using [ExchangeRates].
 * Returns 0.0 when either currency is unsupported.
 */
fun calculateExchange(amount: Double, from: String, to: String): Double {
    if (from == to) return amount
    val rate = ExchangeRates.rate(from, to) ?: return 0.0
    return amount * rate
}
