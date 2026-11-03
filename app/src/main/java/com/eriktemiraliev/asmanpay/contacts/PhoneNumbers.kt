package com.eriktemiraliev.asmanpay.contacts

/**
 * Phone numbers are stored in E.164 form ("+" then 8 to 15 digits), so the same
 * person typed as "+86 138-0013-8000" and "+8613800138000" is recognised as one contact.
 */
object PhoneNumbers {

    private val separators = Regex("[\\s().\\-]")
    private val e164 = Regex("\\+[1-9]\\d{7,14}")

    /**
     * Returns [input] in E.164 form, or null if it isn't a full international number.
     * Spaces, dashes, dots and brackets are ignored, and a leading "00" counts as "+".
     */
    fun normalize(input: String): String? {
        var digits = input.trim().replace(separators, "")
        if (digits.startsWith("00")) digits = "+" + digits.removePrefix("00")
        return digits.takeIf { e164.matches(it) }
    }

    /**
     * Formats an E.164 number for display with its country code split off,
     * for example "+86 138 0013 8000". Unusual lengths are grouped in fours.
     */
    fun format(e164Number: String): String {
        if (!e164Number.startsWith("+")) return e164Number
        val digits = e164Number.drop(1)
        val codeLength = countryCodeLength(digits)
        val code = digits.take(codeLength)
        val rest = digits.drop(codeLength)
        val groups = when (rest.length) {
            11 -> listOf(rest.take(3), rest.substring(3, 7), rest.drop(7)) // China: 138 0013 8000
            10 -> listOf(rest.take(3), rest.substring(3, 6), rest.drop(6)) // Kazakhstan, US: 701 123 4567
            9 -> rest.chunked(3) // Kyrgyzstan: 555 123 456
            else -> rest.chunked(4)
        }
        return "+$code " + groups.joinToString(" ")
    }

    /** Country-code length for the corridor's codes; other codes are assumed to be 2 digits. */
    private fun countryCodeLength(digits: String): Int = when {
        digits.startsWith("1") || digits.startsWith("7") -> 1 // US/Canada, Kazakhstan/Russia
        digits.startsWith("86") || digits.startsWith("44") -> 2 // China, UK
        digits.startsWith("99") -> 3 // Kyrgyzstan (996), Uzbekistan (998), Tajikistan (992), Turkmenistan (993)
        else -> minOf(2, digits.length)
    }
}
