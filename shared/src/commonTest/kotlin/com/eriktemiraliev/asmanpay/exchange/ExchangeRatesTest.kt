package com.eriktemiraliev.asmanpay.exchange

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.Test

class ExchangeRatesTest {

    private val delta = 1e-9

    @Test
    fun supportsAllCorridorCurrencies() {
        assertEquals(listOf("CNY", "USD", "KGS", "KZT"), ExchangeRates.supportedCurrencies)
    }

    @Test
    fun sameCurrencyReturnsAmountUnchanged() {
        ExchangeRates.supportedCurrencies.forEach { code ->
            assertEquals(250.0, calculateExchange(250.0, code, code), delta)
        }
    }

    @Test
    fun convertsFromUsd() {
        assertEquals(720.0, calculateExchange(100.0, "USD", "CNY"), delta)
        assertEquals(8780.0, calculateExchange(100.0, "USD", "KGS"), delta)
        assertEquals(52000.0, calculateExchange(100.0, "USD", "KZT"), delta)
    }

    @Test
    fun convertsBetweenNonUsdCurrenciesViaUsd() {
        assertEquals(520.0 / 7.20, ExchangeRates.rate("CNY", "KZT")!!, delta)
        assertEquals(87.80 / 520.0, ExchangeRates.rate("KZT", "KGS")!!, delta)
    }

    @Test
    fun everyPairRoundTripsToTheOriginalAmount() {
        val codes = ExchangeRates.supportedCurrencies
        for (from in codes) for (to in codes) {
            val there = calculateExchange(1000.0, from, to)
            assertEquals(1000.0, calculateExchange(there, to, from), 1e-6, "$from -> $to -> $from")
        }
    }

    @Test
    fun unsupportedCurrencyYieldsZeroAndNullRate() {
        assertFalse(ExchangeRates.isSupported("EUR"))
        assertTrue(ExchangeRates.isSupported("KZT"))
        assertNull(ExchangeRates.rate("EUR", "CNY"))
        assertEquals(0.0, calculateExchange(100.0, "EUR", "CNY"), delta)
    }

    @Test
    fun conversionUsesTheGivenTable() {
        val table = RateTable(mapOf("USD" to 1.0, "CNY" to 7.0, "KGS" to 70.0, "KZT" to 500.0))
        assertEquals(10.0, calculateExchange(1.0, "CNY", "KGS", table), delta)
        assertEquals(700.0, calculateExchange(100.0, "USD", "CNY", table), delta)
    }

    @Test
    fun missingCurrencyInTableYieldsZero() {
        val partial = RateTable(mapOf("USD" to 1.0, "CNY" to 7.0))
        assertEquals(0.0, calculateExchange(100.0, "USD", "KZT", partial), delta)
    }
}
