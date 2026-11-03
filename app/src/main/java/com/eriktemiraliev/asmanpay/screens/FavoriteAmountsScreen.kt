package com.eriktemiraliev.asmanpay.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eriktemiraliev.asmanpay.R
import com.eriktemiraliev.asmanpay.exchange.ExchangeRates
import com.eriktemiraliev.asmanpay.exchange.RateRepository
import com.eriktemiraliev.asmanpay.exchange.RateTable
import com.eriktemiraliev.asmanpay.exchange.calculateExchange
import com.eriktemiraliev.asmanpay.favorites.AddFavoriteResult
import com.eriktemiraliev.asmanpay.favorites.Amounts
import com.eriktemiraliev.asmanpay.favorites.FavoriteAmount
import com.eriktemiraliev.asmanpay.favorites.FavoriteError
import com.eriktemiraliev.asmanpay.favorites.FavoriteList
import com.eriktemiraliev.asmanpay.favorites.FavoriteRepository
import java.text.NumberFormat
import java.util.UUID

/**
 * The user's saved conversions. Tapping one opens it in the converter through [onUse].
 */
@Composable
fun FavoriteAmountsScreen(onUse: (FavoriteAmount) -> Unit) {
    val context = LocalContext.current
    val repository = remember { FavoriteRepository(context) }
    var list by remember { mutableStateOf(repository.load()) }
    fun update(newList: FavoriteList) {
        list = newList
        repository.save(newList)
    }

    val rates = remember { RateRepository(context).cached() ?: ExchangeRates.sample }

    var newAmount by rememberSaveable { mutableStateOf("") }
    var newFrom by rememberSaveable { mutableStateOf("USD") }
    var newTo by rememberSaveable { mutableStateOf("CNY") }
    var error by remember { mutableStateOf<FavoriteError?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground),
        contentPadding = PaddingValues(20.dp)
    ) {
        item {
            Header(
                stringResource(R.string.favorite_amounts_title),
                stringResource(R.string.favorite_amounts_subtitle)
            )
        }

        item {
            SectionCard(title = stringResource(R.string.favorites_add_title)) {
                if (list.isFull) {
                    Text(
                        text = stringResource(R.string.favorites_error_limit, FavoriteList.MAX_FAVORITES),
                        color = MutedText
                    )
                } else {
                    AppTextField(
                        value = newAmount,
                        onValueChange = { newAmount = it; error = null },
                        label = stringResource(R.string.amount_label),
                        keyboardType = KeyboardType.Decimal
                    )

                    Text(stringResource(R.string.from_label), fontWeight = FontWeight.SemiBold, color = NavyBlue)
                    CurrencySelector(selected = newFrom, onSelected = { newFrom = it; error = null })

                    Text(stringResource(R.string.to_label), fontWeight = FontWeight.SemiBold, color = NavyBlue)
                    CurrencySelector(selected = newTo, onSelected = { newTo = it; error = null })

                    error?.let {
                        Text(
                            text = favoriteErrorText(it),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Button(
                        onClick = {
                            when (val result = list.add(
                                amountInput = newAmount,
                                from = newFrom,
                                to = newTo,
                                id = UUID.randomUUID().toString(),
                                nowMillis = System.currentTimeMillis()
                            )) {
                                is AddFavoriteResult.Added -> {
                                    update(result.list)
                                    newAmount = ""
                                    error = null
                                }
                                is AddFavoriteResult.Rejected -> error = result.error
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(stringResource(R.string.add_favorite_amount))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (list.favorites.isEmpty()) {
            item {
                SectionCard(title = stringResource(R.string.favorites_empty_title)) {
                    Text(stringResource(R.string.favorites_empty_message), color = MutedText)
                }
            }
        }

        items(list.favorites, key = { it.id }) { favorite ->
            FavoriteAmountCard(
                favorite = favorite,
                rates = rates,
                onUse = {
                    update(list.recordUse(favorite.id))
                    onUse(favorite)
                },
                onRemove = { update(list.remove(favorite.id)) }
            )
        }
    }
}

@Composable
fun FavoriteAmountCard(
    favorite: FavoriteAmount,
    rates: RateTable,
    onUse: () -> Unit,
    onRemove: () -> Unit
) {
    val conversion = favoriteConversionText(favorite)
    val estimate = calculateExchange(favorite.amountValue, favorite.fromCurrency, favorite.toCurrency, rates)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onUse),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, top = 14.dp, bottom = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(SoftBlue, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = BrightBlue, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = conversion, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NavyBlue)
                Text(
                    text = stringResource(
                        R.string.favorites_estimate,
                        formatAmount(estimate),
                        favorite.toCurrency
                    ),
                    color = MutedText,
                    fontSize = 14.sp
                )
                Text(
                    text = if (favorite.useCount == 0) {
                        stringResource(R.string.favorites_not_used)
                    } else {
                        LocalContext.current.resources.getQuantityString(
                            R.plurals.favorites_used_count, favorite.useCount, favorite.useCount
                        )
                    },
                    color = BrightBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.favorites_remove_description, conversion),
                    tint = MutedText
                )
            }
        }
    }
}

/** "500 USD → CNY", with the amount grouped for the user's locale. */
@Composable
fun favoriteConversionText(favorite: FavoriteAmount): String = stringResource(
    R.string.favorites_conversion,
    formatAmount(favorite.amountValue),
    favorite.fromCurrency,
    favorite.toCurrency
)

/** Formats [value] for display with grouping and up to two decimals, such as "1,000" or "12.5". */
fun formatAmount(value: Double): String =
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = Amounts.MAX_DECIMALS }.format(value)

@Composable
fun favoriteErrorText(error: FavoriteError): String = when (error) {
    FavoriteError.INVALID_AMOUNT -> stringResource(R.string.favorites_error_invalid_amount)
    FavoriteError.TOO_MANY_DECIMALS -> stringResource(R.string.favorites_error_too_many_decimals, Amounts.MAX_DECIMALS)
    FavoriteError.AMOUNT_TOO_LARGE -> stringResource(R.string.favorites_error_too_large, formatAmount(Amounts.MAX_AMOUNT.toDouble()))
    FavoriteError.UNSUPPORTED_CURRENCY -> stringResource(R.string.favorites_error_unsupported_currency)
    FavoriteError.SAME_CURRENCY -> stringResource(R.string.favorites_error_same_currency)
    FavoriteError.DUPLICATE -> stringResource(R.string.favorites_error_duplicate)
    FavoriteError.LIMIT_REACHED -> stringResource(R.string.favorites_error_limit, FavoriteList.MAX_FAVORITES)
}
