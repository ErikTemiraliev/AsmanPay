package com.eriktemiraliev.asmanpay.exchange

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.Test

class RateParserTest {

    private val sampleResponse = """
        {
          "result": "success",
          "provider": "https://www.exchangerate-api.com",
          "time_last_update_unix": 1730592151,
          "base_code": "USD",
          "rates": {
            "USD": 1,
            "AED": 3.6725,
            "CNY": 7.1234,
            "EUR": 0.92,
            "KGS": 86.45,
            "KZT": 495.7
          }
        }
    """.trimIndent()

    @Test
    fun parsesSupportedCurrenciesFromSuccessfulResponse() {
        val table = RateParser.parse(sampleResponse)!!

        assertEquals(
            mapOf("CNY" to 7.1234, "USD" to 1.0, "KGS" to 86.45, "KZT" to 495.7),
            table.unitsPerUsd
        )
        assertEquals(RateSource.LIVE, table.source)
        assertEquals(1730592151L, table.updatedAtEpochSeconds)
    }

    @Test
    fun keepsTheRequestedSource() {
        assertEquals(RateSource.CACHED, RateParser.parse(sampleResponse, RateSource.CACHED)!!.source)
    }

    @Test
    fun parsedTableDrivesConversion() {
        val table = RateParser.parse(sampleResponse)!!
        assertEquals(712.34, calculateExchange(100.0, "USD", "CNY", table), 1e-9)
        assertEquals(495.7 / 86.45, table.rate("KGS", "KZT")!!, 1e-9)
    }

    @Test
    fun parsesCompactJsonAndExponentNotation() {
        val compact = """{"result":"success","base_code":"USD","rates":{"USD":1,"CNY":7.1,"KGS":8.645e1,"KZT":495}}"""
        val table = RateParser.parse(compact)!!
        assertEquals(86.45, table.unitsPerUsd["KGS"]!!, 1e-9)
        assertNull(table.updatedAtEpochSeconds)
    }

    @Test
    fun rejectsErrorResponse() {
        assertNull(RateParser.parse("""{"result":"error","error-type":"unsupported-code"}"""))
    }

    @Test
    fun rejectsNonUsdBase() {
        assertNull(RateParser.parse(sampleResponse.replace("\"base_code\": \"USD\"", "\"base_code\": \"EUR\"")))
    }

    @Test
    fun rejectsResponseMissingASupportedCurrency() {
        assertNull(RateParser.parse(sampleResponse.replace("\"KZT\": 495.7", "\"XYZ\": 1")))
    }

    @Test
    fun rejectsZeroOrNegativeRates() {
        assertNull(RateParser.parse(sampleResponse.replace("\"KGS\": 86.45", "\"KGS\": 0")))
        assertNull(RateParser.parse(sampleResponse.replace("\"KGS\": 86.45", "\"KGS\": -86.45")))
    }

    @Test
    fun rejectsGarbage() {
        assertNull(RateParser.parse(""))
        assertNull(RateParser.parse("<html>Service unavailable</html>"))
    }
}
