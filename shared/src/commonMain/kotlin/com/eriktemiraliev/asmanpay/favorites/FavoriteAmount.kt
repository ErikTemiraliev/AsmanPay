package com.eriktemiraliev.asmanpay.favorites

/**
 * A conversion the user does often, saved so it can be repeated with one tap,
 * for example "500 USD → CNY".
 *
 * @param amount The amount in canonical form (see [Amounts.parse]), such as "500" or "12.5".
 * @param useCount How many times the user has applied this favorite to the converter.
 */
data class FavoriteAmount(
    val id: String,
    val amount: String,
    val fromCurrency: String,
    val toCurrency: String,
    val useCount: Int = 0,
    val addedAtMillis: Long = 0L
) {
    val amountValue: Double get() = amount.toDouble()
}
