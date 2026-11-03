package com.eriktemiraliev.asmanpay.favorites

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
    const val MAX_AMOUNT: Long = 10_000_000

    private val ignored = Regex("[\\s  _']")
    private val decimalComma = Regex("\\d+,\\d{1,2}")
    private val plainNumber = Regex("\\d+(\\.\\d+)?")

    fun parse(input: String): ParsedAmount {
        var text = input.replace(ignored, "")
        text = if (decimalComma.matches(text)) text.replace(',', '.') else text.replace(",", "")
        if (!plainNumber.matches(text)) return ParsedAmount.Invalid(AmountError.INVALID)

        // Work on the digits directly (no BigDecimal, which only exists on the JVM),
        // so huge or very precise inputs are judged exactly.
        val whole = text.substringBefore('.').trimStart('0')
        val fraction = text.substringAfter('.', "").trimEnd('0')
        val maxWhole = MAX_AMOUNT.toString()
        return when {
            whole.isEmpty() && fraction.isEmpty() -> ParsedAmount.Invalid(AmountError.INVALID)
            fraction.length > MAX_DECIMALS -> ParsedAmount.Invalid(AmountError.TOO_MANY_DECIMALS)
            whole.length > maxWhole.length ||
                (whole.length == maxWhole.length && (whole > maxWhole || (whole == maxWhole && fraction.isNotEmpty()))) ->
                ParsedAmount.Invalid(AmountError.TOO_LARGE)
            else -> ParsedAmount.Valid(
                (whole.ifEmpty { "0" }) + if (fraction.isEmpty()) "" else ".$fraction"
            )
        }
    }

    /** The canonical form of [input], or null if it isn't a valid amount. */
    fun canonical(input: String): String? = (parse(input) as? ParsedAmount.Valid)?.canonical
}
