package com.eriktemiraliev.asmanpay.favorites

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoriteListTest {

    private fun FavoriteList.added(amount: String, from: String = "USD", to: String = "CNY", id: String = amount, now: Long = 0L) =
        (add(amount, from, to, id, now) as AddFavoriteResult.Added).list

    private fun FavoriteList.rejection(amount: String, from: String = "USD", to: String = "CNY") =
        (add(amount, from, to, "new", 0L) as AddFavoriteResult.Rejected).error

    @Test
    fun addsAFavoriteInCanonicalForm() {
        val result = FavoriteList().add("1 000", "USD", "CNY", "a", 42L) as AddFavoriteResult.Added
        assertEquals(FavoriteAmount("a", "1000", "USD", "CNY", 0, 42L), result.favorite)
        assertEquals(listOf(result.favorite), result.list.favorites)
    }

    @Test
    fun rejectsInvalidInput() {
        val list = FavoriteList()
        assertEquals(FavoriteError.INVALID_AMOUNT, list.rejection("abc"))
        assertEquals(FavoriteError.TOO_MANY_DECIMALS, list.rejection("1.234"))
        assertEquals(FavoriteError.AMOUNT_TOO_LARGE, list.rejection("20000000"))
        assertEquals(FavoriteError.UNSUPPORTED_CURRENCY, list.rejection("100", from = "EUR"))
        assertEquals(FavoriteError.SAME_CURRENCY, list.rejection("100", from = "KGS", to = "KGS"))
    }

    @Test
    fun rejectsTheSameConversionTwiceButAllowsOtherPairs() {
        val list = FavoriteList().added("500")
        assertEquals(FavoriteError.DUPLICATE, list.rejection("500.00"))
        assertEquals(FavoriteError.DUPLICATE, list.rejection("500"))
        assertEquals(2, list.added("500", from = "CNY", to = "USD", id = "b").favorites.size)
        assertEquals(2, list.added("500", from = "USD", to = "KZT", id = "c").favorites.size)
    }

    @Test
    fun stopsAtTheLimit() {
        var list = FavoriteList()
        repeat(FavoriteList.MAX_FAVORITES) { list = list.added((it + 1).toString()) }
        assertTrue(list.isFull)
        assertEquals(FavoriteError.LIMIT_REACHED, list.rejection("999"))
        assertFalse(list.remove("1").isFull)
    }

    @Test
    fun ordersMostUsedFirstThenOldestFirst() {
        val list = FavoriteList()
            .added("100", id = "a", now = 1)
            .added("200", id = "b", now = 2)
            .added("300", id = "c", now = 3)
        assertEquals(listOf("a", "b", "c"), list.favorites.map { it.id })

        val used = list.recordUse("c").recordUse("c").recordUse("b")
        assertEquals(listOf("c", "b", "a"), used.favorites.map { it.id })
        assertEquals(2, used.find("c")?.useCount)
        assertEquals(listOf("c", "b"), used.shortcuts(limit = 2).map { it.id })
    }

    @Test
    fun ignoresUnknownIdsAndRemoves() {
        val list = FavoriteList().added("100", id = "a").added("200", id = "b")
        assertEquals(list, list.recordUse("missing"))
        assertEquals(listOf("b"), list.remove("a").favorites.map { it.id })
    }

    @Test
    fun containsMatchesTheSameAmountTypedDifferently() {
        val list = FavoriteList().added("1000")
        assertTrue(list.contains("1,000", "USD", "CNY"))
        assertFalse(list.contains("1000", "CNY", "USD"))
        assertFalse(list.contains("", "USD", "CNY"))
    }
}
