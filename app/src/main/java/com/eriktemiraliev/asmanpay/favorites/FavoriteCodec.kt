package com.eriktemiraliev.asmanpay.favorites

import com.eriktemiraliev.asmanpay.exchange.ExchangeRates

/**
 * Turns favorites into text for storage and back.
 *
 * The format is a version line, then one favorite per line with tab-separated fields:
 * id, amount, from, to, use count, added-at millis. None of the fields can contain a
 * tab or line break, so no escaping is needed. Like the contacts codec, it's hand-rolled
 * so it can be unit tested on the JVM, and a damaged line is skipped instead of losing
 * every favorite.
 */
object FavoriteCodec {

    private const val HEADER = "asmanpay-favorites/1"
    private const val FIELD_COUNT = 6
    private val safeId = Regex("[A-Za-z0-9-]+")

    fun encode(favorites: List<FavoriteAmount>): String = buildString {
        append(HEADER)
        for (favorite in favorites) {
            append('\n')
            append(
                listOf(
                    favorite.id,
                    favorite.amount,
                    favorite.fromCurrency,
                    favorite.toCurrency,
                    favorite.useCount.toString(),
                    favorite.addedAtMillis.toString()
                ).joinToString("\t")
            )
        }
    }

    /** Returns the stored favorites, or an empty list if [text] isn't in this format. */
    fun decode(text: String): List<FavoriteAmount> {
        val lines = text.split('\n')
        if (lines.firstOrNull() != HEADER) return emptyList()
        return lines.drop(1).mapNotNull(::decodeLine).distinctBy { it.id }
    }

    private fun decodeLine(line: String): FavoriteAmount? {
        val fields = line.split('\t')
        if (fields.size != FIELD_COUNT) return null
        val (id, amount, from, to, useCount) = fields

        if (!safeId.matches(id)) return null
        if (Amounts.canonical(amount) != amount) return null
        if (!ExchangeRates.isSupported(from) || !ExchangeRates.isSupported(to) || from == to) return null

        return FavoriteAmount(
            id = id,
            amount = amount,
            fromCurrency = from,
            toCurrency = to,
            useCount = useCount.toIntOrNull()?.takeIf { it >= 0 } ?: return null,
            addedAtMillis = fields[5].toLongOrNull() ?: return null
        )
    }
}
