package com.eriktemiraliev.asmanpay.favorites

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class AmountsTest {

    @Test
    fun acceptsPlainAndGroupedAmounts() {
        assertEquals("500", Amounts.canonical("500"))
        assertEquals("1000", Amounts.canonical("1 000"))
        assertEquals("1000", Amounts.canonical("1 000"))
        assertEquals("1000", Amounts.canonical("1,000"))
        assertEquals("1000.5", Amounts.canonical("1,000.50"))
        assertEquals("1500000", Amounts.canonical("1,500,000"))
    }

    @Test
    fun treatsCommaWithOneOrTwoDigitsAsDecimal() {
        assertEquals("12.5", Amounts.canonical("12,5"))
        assertEquals("12.75", Amounts.canonical("12,75"))
    }

    @Test
    fun stripsTrailingZerosAndSurroundingSpace() {
        assertEquals("100", Amounts.canonical(" 100.00 "))
        assertEquals("0.5", Amounts.canonical("0.50"))
    }

    @Test
    fun rejectsNonPositiveAndMalformedAmounts() {
        for (input in listOf("", "  ", "0", "0.00", "-5", "abc", "1.2.3", "1e3", ".")) {
            assertEquals(ParsedAmount.Invalid(AmountError.INVALID), Amounts.parse(input), input)
        }
    }

    @Test
    fun rejectsMoreThanTwoDecimals() {
        assertEquals(ParsedAmount.Invalid(AmountError.TOO_MANY_DECIMALS), Amounts.parse("1.005"))
        assertEquals("1.1", Amounts.canonical("1.100"))
    }

    @Test
    fun rejectsAmountsAboveTheLimit() {
        assertEquals("10000000", Amounts.canonical("10000000"))
        assertEquals(ParsedAmount.Invalid(AmountError.TOO_LARGE), Amounts.parse("10000000.01"))
        assertNull(Amounts.canonical("99999999999999999999"))
    }
}
