package com.eriktemiraliev.asmanpay.screens

import org.jetbrains.compose.resources.stringResource
import com.eriktemiraliev.asmanpay.resources.*
import com.eriktemiraliev.asmanpay.exchange.ExchangeRates
import com.eriktemiraliev.asmanpay.exchange.RateRepository
import com.eriktemiraliev.asmanpay.exchange.RateSource
import com.eriktemiraliev.asmanpay.exchange.RateTable
import com.eriktemiraliev.asmanpay.exchange.calculateExchange
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import com.eriktemiraliev.asmanpay.favorites.AddFavoriteResult
import com.eriktemiraliev.asmanpay.favorites.Amounts
import com.eriktemiraliev.asmanpay.favorites.FavoriteAmount
import com.eriktemiraliev.asmanpay.favorites.FavoriteError
import com.eriktemiraliev.asmanpay.favorites.FavoriteList
import com.eriktemiraliev.asmanpay.favorites.FavoriteRepository
import com.eriktemiraliev.asmanpay.platform.LocalKeyValueStores
import com.eriktemiraliev.asmanpay.platform.newId
import com.eriktemiraliev.asmanpay.platform.nowMillis
import com.eriktemiraliev.asmanpay.platform.formatDateTime
import com.eriktemiraliev.asmanpay.platform.formatDecimal

/**
 * The converter. [initialConversion], when set, pre-fills it, such as a favorite
 * the user picked on the Favorites tab.
 */
@Composable
fun HomeScreen(initialConversion: FavoriteAmount? = null) {
    var amount by remember { mutableStateOf(initialConversion?.amount ?: "") }
    var fromCurrency by remember { mutableStateOf(initialConversion?.fromCurrency ?: "USD") }
    var toCurrency by remember { mutableStateOf(initialConversion?.toCurrency ?: "CNY") }

    val stores = LocalKeyValueStores.current
    val favoriteRepository = remember { FavoriteRepository(stores) }
    var favorites by remember { mutableStateOf(favoriteRepository.load()) }
    fun updateFavorites(newList: FavoriteList) {
        favorites = newList
        favoriteRepository.save(newList)
    }

    // Start from the rates saved on this device (or the samples), then refresh from the network.
    val rateRepository = remember { RateRepository(stores) }
    var rates by remember { mutableStateOf(rateRepository.cached() ?: ExchangeRates.sample) }
    LaunchedEffect(rateRepository) {
        rateRepository.fetchLatest()?.let { rates = it }
    }

    // Accept "1 000" and "12,5" like favorites do, but convert any amount, however large or precise.
    val numericAmount = Amounts.canonical(amount)?.toDouble() ?: amount.toDoubleOrNull() ?: 0.0

    val result = calculateExchange(
        amount = numericAmount,
        from = fromCurrency,
        to = toCurrency,
        table = rates
    )

    val currentRate = calculateExchange(
        amount = 1.0,
        from = fromCurrency,
        to = toCurrency,
        table = rates
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
    ) {
        item {
            HomeTopBar()

            Spacer(modifier = Modifier.height(16.dp))

            GradientCard {
                Column {
                    Text(
                        text = stringResource(Res.string.estimated_result_label),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(
                            Res.string.estimated_result_value,
                            formatDecimal(result, 2, 2, useGrouping = false),
                            toCurrency
                        ),
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.18f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${stringResource(Res.string.rate_label)}: 1 $fromCurrency = " +
                                "${formatDecimal(currentRate, 4, 4, useGrouping = false)} $toCurrency",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    RateStatusText(rates)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = stringResource(Res.string.convert_card_title),
                        color = NavyBlue,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(Res.string.exchange_pair),
                        color = MutedText,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = stringResource(Res.string.from_label),
                        fontWeight = FontWeight.SemiBold,
                        color = NavyBlue
                    )

                    CurrencySelector(
                        selected = fromCurrency,
                        onSelected = { fromCurrency = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(Res.string.to_label),
                        fontWeight = FontWeight.SemiBold,
                        color = NavyBlue
                    )

                    CurrencySelector(
                        selected = toCurrency,
                        onSelected = { toCurrency = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = stringResource(Res.string.enter_amount_label),
                        keyboardType = KeyboardType.Number
                    )

                    SaveFavoriteButton(
                        amount = amount,
                        from = fromCurrency,
                        to = toCurrency,
                        favorites = favorites,
                        onSave = { result ->
                            when (result) {
                                is AddFavoriteResult.Added -> updateFavorites(result.list)
                                is AddFavoriteResult.Rejected -> Unit
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val shortcuts = favorites.shortcuts()
                    Text(
                        text = stringResource(
                            if (shortcuts.isEmpty()) Res.string.quick_amounts else Res.string.favorites_shortcuts_title
                        ),
                        color = NavyBlue,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (shortcuts.isEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DEFAULT_QUICK_AMOUNTS.forEach { quick ->
                                QuickAmountButton(quick) { amount = quick }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.favorites_shortcuts_hint),
                            color = MutedText,
                            fontSize = 12.sp
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            shortcuts.forEach { favorite ->
                                FavoriteShortcutButton(favorite) {
                                    amount = favorite.amount
                                    fromCurrency = favorite.fromCurrency
                                    toCurrency = favorite.toCurrency
                                    updateFavorites(favorites.recordUse(favorite.id))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/** Amount-only shortcuts offered until the user saves favorites of their own. */
private val DEFAULT_QUICK_AMOUNTS = listOf("100", "500", "1000", "5000")

/**
 * Saves the converter's current amount and pair as a favorite. Shows a filled star
 * once that conversion is saved, and is disabled until there's something valid to save.
 */
@Composable
private fun SaveFavoriteButton(
    amount: String,
    from: String,
    to: String,
    favorites: FavoriteList,
    onSave: (AddFavoriteResult) -> Unit
) {
    val alreadySaved = favorites.contains(amount, from, to)
    val canSave = !alreadySaved && Amounts.canonical(amount) != null && from != to
    val limitMessage = stringResource(Res.string.favorites_error_limit, FavoriteList.MAX_FAVORITES)
    // Shown under the button when the list is full; cleared when the conversion changes.
    var limitReached by remember(amount, from, to) { mutableStateOf(false) }

    TextButton(
        onClick = {
            val result = favorites.add(
                amountInput = amount,
                from = from,
                to = to,
                id = newId(),
                nowMillis = nowMillis()
            )
            limitReached = result is AddFavoriteResult.Rejected && result.error == FavoriteError.LIMIT_REACHED
            onSave(result)
        },
        enabled = canSave,
        contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = if (alreadySaved) StarGold else if (canSave) BrightBlue else Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = stringResource(if (alreadySaved) Res.string.favorites_saved else Res.string.favorites_save_current),
            color = if (alreadySaved) NavyBlue else Color.Unspecified
        )
    }

    if (limitReached) {
        Text(
            text = limitMessage,
            color = MaterialTheme.colorScheme.error,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun FavoriteShortcutButton(favorite: FavoriteAmount, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = SoftBlue,
            contentColor = NavyBlue
        ),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = favoriteConversionText(favorite),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/** The brand row at the top of Home: the mark, the name and a short promise. */
@Composable
fun HomeTopBar() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        AsmanLogo(size = 44.dp)

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = stringResource(Res.string.home_hero_title),
                color = NavyBlue,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp
            )
            Text(
                text = stringResource(Res.string.home_hero_subtitle),
                color = MutedText,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
fun RowScope.QuickAmountButton(
    amount: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = SoftBlue,
            contentColor = NavyBlue
        ),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(
            text = amount,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CurrencySelector(
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        ExchangeRates.supportedCurrencies.forEach { currency ->
            val selectedCurrency = selected == currency

            Button(
                onClick = { onSelected(currency) },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedCurrency) BrightBlue else SoftBlue,
                    contentColor = if (selectedCurrency) Color.White else NavyBlue
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(50)
            ) {
                Text(currency, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RateStatusText(rates: RateTable) {
    val updatedAt = rates.updatedAtEpochSeconds?.let {
        formatDateTime(it * 1000)
    }

    val status = when {
        rates.source == RateSource.LIVE && updatedAt != null ->
            stringResource(Res.string.rates_live_updated, updatedAt)
        rates.source == RateSource.LIVE ->
            stringResource(Res.string.rates_live)
        rates.source == RateSource.CACHED && updatedAt != null ->
            stringResource(Res.string.rates_cached, updatedAt)
        rates.source == RateSource.CACHED ->
            stringResource(Res.string.rates_cached_unknown_time)
        else ->
            stringResource(Res.string.sample_rates_notice)
    }

    Text(
        text = status,
        color = Color.White.copy(alpha = 0.85f),
        fontSize = 12.sp
    )

    if (rates.source != RateSource.SAMPLE) {
        Text(
            text = stringResource(Res.string.rates_attribution),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 11.sp
        )
    }
}
