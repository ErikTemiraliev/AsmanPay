package com.eriktemiraliev.asmanpay.favorites

import com.eriktemiraliev.asmanpay.exchange.ExchangeRates

/** Why a new favorite was rejected. */
enum class FavoriteError {
    INVALID_AMOUNT,
    TOO_MANY_DECIMALS,
    AMOUNT_TOO_LARGE,
    UNSUPPORTED_CURRENCY,
    SAME_CURRENCY,
    DUPLICATE,
    LIMIT_REACHED
}

sealed interface AddFavoriteResult {
    data class Added(val list: FavoriteList, val favorite: FavoriteAmount) : AddFavoriteResult
    data class Rejected(val error: FavoriteError) : AddFavoriteResult
}

/**
 * The user's saved favorite amounts. Immutable: every change returns a new list,
 * which the screen saves through [FavoriteRepository].
 *
 * Favorites are ordered most used first, so the shortcuts on the converter are the
 * ones the user actually repeats; ties keep the order they were added in.
 */
class FavoriteList(favorites: List<FavoriteAmount> = emptyList()) {

    val favorites: List<FavoriteAmount> = favorites.sortedWith(usageOrder)

    val isFull: Boolean get() = favorites.size >= MAX_FAVORITES

    fun find(id: String): FavoriteAmount? = favorites.firstOrNull { it.id == id }

    /** The favorites to offer as one-tap shortcuts on the converter. */
    fun shortcuts(limit: Int = SHORTCUT_COUNT): List<FavoriteAmount> = favorites.take(limit)

    /** Saves "[amountInput] [from] → [to]". The same conversion can't be saved twice. */
    fun add(
        amountInput: String,
        from: String,
        to: String,
        id: String,
        nowMillis: Long
    ): AddFavoriteResult {
        val parsed = Amounts.parse(amountInput)
        val error = when {
            parsed is ParsedAmount.Invalid -> when (parsed.error) {
                AmountError.INVALID -> FavoriteError.INVALID_AMOUNT
                AmountError.TOO_MANY_DECIMALS -> FavoriteError.TOO_MANY_DECIMALS
                AmountError.TOO_LARGE -> FavoriteError.AMOUNT_TOO_LARGE
            }
            !ExchangeRates.isSupported(from) || !ExchangeRates.isSupported(to) -> FavoriteError.UNSUPPORTED_CURRENCY
            from == to -> FavoriteError.SAME_CURRENCY
            contains((parsed as ParsedAmount.Valid).canonical, from, to) -> FavoriteError.DUPLICATE
            isFull -> FavoriteError.LIMIT_REACHED
            else -> null
        }
        if (error != null) return AddFavoriteResult.Rejected(error)

        val favorite = FavoriteAmount(
            id = id,
            amount = (parsed as ParsedAmount.Valid).canonical,
            fromCurrency = from,
            toCurrency = to,
            addedAtMillis = nowMillis
        )
        return AddFavoriteResult.Added(FavoriteList(favorites + favorite), favorite)
    }

    /** Whether "[amountInput] [from] → [to]" is already saved. */
    fun contains(amountInput: String, from: String, to: String): Boolean {
        val amount = Amounts.canonical(amountInput) ?: return false
        return favorites.any { it.amount == amount && it.fromCurrency == from && it.toCurrency == to }
    }

    fun remove(id: String): FavoriteList = FavoriteList(favorites.filterNot { it.id == id })

    /** Counts one more use of [id], which moves it up the list. Unknown ids are ignored. */
    fun recordUse(id: String): FavoriteList =
        FavoriteList(favorites.map { if (it.id == id) it.copy(useCount = it.useCount + 1) else it })

    override fun equals(other: Any?): Boolean = other is FavoriteList && other.favorites == favorites

    override fun hashCode(): Int = favorites.hashCode()

    companion object {
        const val MAX_FAVORITES = 12
        const val SHORTCUT_COUNT = 4

        private val usageOrder: Comparator<FavoriteAmount> =
            compareByDescending<FavoriteAmount> { it.useCount }
                .thenBy { it.addedAtMillis }
                .thenBy { it.id }
    }
}
