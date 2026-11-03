package com.eriktemiraliev.asmanpay.favorites

import java.math.BigDecimal

/** Why an entered amount can't be saved. */
enum class AmountError {
    INVALID,
    TOO_MANY_DECIMALS,
    TOO_LARGE
}

sealed interface ParsedAmount {
    /** [canonical] has no grouping and no trailing zeros, such as "1000" or "12.5". */
    data class Valid(val canonical: String) : ParsedAmount
    data class Invalid(val error: AmountError) : ParsedAmount
}

/**
 * Reads amounts the way people type them in the corridor's locales:
 * "1000", "1 000", "1,000", "1,000.50", or "12,5" with a decimal comma.
 */
object Amounts {

    const val MAX_DECIMALS = 2
    val MAX_AMOUNT: BigDecimal = BigDecimal("10000000")

    private val ignored = Regex("[\\s  _']")
    private val decimalComma = Regex("\\d+,\\d{1,2}")
    private val plainNumber = Regex("\\d+(\\.\\d+)?")

    fun parse(input: String): ParsedAmount {
        var text = input.replace(ignored, "")
        text = if (decimalComma.matches(text)) text.replace(',', '.') else text.replace(",", "")
        if (!plainNumber.matches(text)) return ParsedAmount.Invalid(AmountError.INVALID)

        val value = BigDecimal(text).stripTrailingZeros()
        return when {
            value.signum() <= 0 -> ParsedAmount.Invalid(AmountError.INVALID)
            value.scale() > MAX_DECIMALS -> ParsedAmount.Invalid(AmountError.TOO_MANY_DECIMALS)
            value > MAX_AMOUNT -> ParsedAmount.Invalid(AmountError.TOO_LARGE)
            else -> ParsedAmount.Valid(value.toPlainString())
        }
    }

    /** The canonical form of [input], or null if it isn't a valid amount. */
    fun canonical(input: String): String? = (parse(input) as? ParsedAmount.Valid)?.canonical
}
