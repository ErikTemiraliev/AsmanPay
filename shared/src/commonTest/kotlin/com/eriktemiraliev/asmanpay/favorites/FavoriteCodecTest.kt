package com.eriktemiraliev.asmanpay.favorites

import kotlin.test.assertEquals
import kotlin.test.Test

class FavoriteCodecTest {

    private val favorites = listOf(
        FavoriteAmount("6f1c2a9e-1b2d-4c3e-9f00-123456789abc", "500", "USD", "CNY", 3, 1730592151000L),
        FavoriteAmount("b", "12.5", "KGS", "KZT")
    )

    @Test
    fun roundTripsFavorites() {
        assertEquals(favorites, FavoriteCodec.decode(FavoriteCodec.encode(favorites)))
    }

    @Test
    fun roundTripsAnEmptyList() {
        assertEquals(emptyList<FavoriteAmount>(), FavoriteCodec.decode(FavoriteCodec.encode(emptyList())))
    }

    @Test
    fun returnsNothingForUnknownFormats() {
        assertEquals(emptyList<FavoriteAmount>(), FavoriteCodec.decode(""))
        assertEquals(emptyList<FavoriteAmount>(), FavoriteCodec.decode("asmanpay-favorites/2\na\t1\tUSD\tCNY\t0\t0"))
    }

    @Test
    fun skipsDamagedLinesAndKeepsTheRest() {
        val text = listOf(
            "asmanpay-favorites/1",
            "ok\t100\tUSD\tCNY\t1\t5",
            "too-few\t100\tUSD",
            "bad-amount\t1,000\tUSD\tCNY\t0\t0",
            "bad-currency\t100\tEUR\tCNY\t0\t0",
            "same\t100\tCNY\tCNY\t0\t0",
            "negative-uses\t100\tUSD\tCNY\t-1\t0",
            "bad time\t100\tUSD\tCNY\t0\tsoon",
            "ok\t200\tUSD\tKGS\t0\t0"
        ).joinToString("\n")
        assertEquals(listOf(FavoriteAmount("ok", "100", "USD", "CNY", 1, 5)), FavoriteCodec.decode(text))
    }
}
