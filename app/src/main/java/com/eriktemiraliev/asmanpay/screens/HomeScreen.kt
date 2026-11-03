package com.eriktemiraliev.asmanpay.screens

import androidx.compose.ui.res.stringResource
import com.eriktemiraliev.asmanpay.R
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
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
import java.text.DateFormat
import java.util.Date
import java.util.UUID

/**
 * The converter. [initialConversion], when set, pre-fills it, such as a favorite
 * the user picked on the Favorites tab.
 */
@Composable
fun HomeScreen(initialConversion: FavoriteAmount? = null) {
    var amount by remember { mutableStateOf(initialConversion?.amount ?: "") }
    var fromCurrency by remember { mutableStateOf(initialConversion?.fromCurrency ?: "USD") }
    var toCurrency by remember { mutableStateOf(initialConversion?.toCurrency ?: "CNY") }

    val context = LocalContext.current
    val favoriteRepository = remember { FavoriteRepository(context) }
    var favorites by remember { mutableStateOf(favoriteRepository.load()) }
    fun updateFavorites(newList: FavoriteList) {
        favorites = newList
        favoriteRepository.save(newList)
    }

    // Start from the rates saved on this device (or the samples), then refresh from the network.
    val rateRepository = remember { RateRepository(context) }
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
            HomeHeroCard()

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = stringResource(R.string.convert_card_title),
                        color = NavyBlue,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.exchange_pair),
                        color = Color.Gray,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = stringResource(R.string.from_label),
                        fontWeight = FontWeight.SemiBold,
                        color = NavyBlue
                    )

                    CurrencySelector(
                        selected = fromCurrency,
                        onSelected = { fromCurrency = it }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = stringResource(R.string.to_label),
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
                        label = stringResource(R.string.enter_amount_label),
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
                            if (shortcuts.isEmpty()) R.string.quick_amounts else R.string.favorites_shortcuts_title
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
                            text = stringResource(R.string.favorites_shortcuts_hint),
                            color = Color.Gray,
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

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = NavyBlue)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = stringResource(R.string.estimated_result_label),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(
                            R.string.estimated_result_value,
                            result,
                            toCurrency
                        ),
                        color = Color.White,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .background(
                                Color.White.copy(alpha = 0.12f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${stringResource(R.string.rate_label)}: 1 $fromCurrency = %.4f $toCurrency".format(currentRate),
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    RateStatusText(rates)
                }
            }
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
    val context = LocalContext.current
    val alreadySaved = favorites.contains(amount, from, to)
    val canSave = !alreadySaved && Amounts.canonical(amount) != null && from != to
    val limitMessage = stringResource(R.string.favorites_error_limit, FavoriteList.MAX_FAVORITES)

    TextButton(
        onClick = {
            val result = favorites.add(
                amountInput = amount,
                from = from,
                to = to,
                id = UUID.randomUUID().toString(),
                nowMillis = System.currentTimeMillis()
            )
            if (result is AddFavoriteResult.Rejected && result.error == FavoriteError.LIMIT_REACHED) {
                Toast.makeText(context, limitMessage, Toast.LENGTH_LONG).show()
            }
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
            text = stringResource(if (alreadySaved) R.string.favorites_saved else R.string.favorites_save_current),
            color = if (alreadySaved) NavyBlue else Color.Unspecified
        )
    }
}

@Composable
private fun FavoriteShortcutButton(favorite: FavoriteAmount, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE8F2FF),
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

@Composable
fun HomeHeroCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = NavyBlue)
    ) {
        Column(modifier = Modifier.padding(26.dp)) {
            Text(
                text = stringResource(R.string.home_hero_title),
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.home_hero_subtitle),
                color = Color.White.copy(alpha = 0.82f),
                fontSize = 16.sp
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
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE8F2FF),
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
                    containerColor = if (selectedCurrency) NavyBlue else Color.White,
                    contentColor = if (selectedCurrency) Color.White else NavyBlue
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(currency)
            }
        }
    }
}

@Composable
fun RateStatusText(rates: RateTable) {
    val updatedAt = rates.updatedAtEpochSeconds?.let {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(it * 1000))
    }

    val status = when {
        rates.source == RateSource.LIVE && updatedAt != null ->
            stringResource(R.string.rates_live_updated, updatedAt)
        rates.source == RateSource.LIVE ->
            stringResource(R.string.rates_live)
        rates.source == RateSource.CACHED && updatedAt != null ->
            stringResource(R.string.rates_cached, updatedAt)
        rates.source == RateSource.CACHED ->
            stringResource(R.string.rates_cached_unknown_time)
        else ->
            stringResource(R.string.sample_rates_notice)
    }

    Text(
        text = status,
        color = Color.White.copy(alpha = 0.7f),
        fontSize = 12.sp
    )

    if (rates.source != RateSource.SAMPLE) {
        Text(
            text = stringResource(R.string.rates_attribution),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp
        )
    }
}
