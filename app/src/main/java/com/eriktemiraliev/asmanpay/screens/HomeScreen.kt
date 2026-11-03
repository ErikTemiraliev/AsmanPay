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
import java.text.DateFormat
import java.util.Date

@Composable
fun HomeScreen() {
    var amount by remember { mutableStateOf("") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("CNY") }

    // Start from the rates saved on this device (or the samples), then refresh from the network.
    val context = LocalContext.current
    val rateRepository = remember { RateRepository(context) }
    var rates by remember { mutableStateOf(rateRepository.cached() ?: ExchangeRates.sample) }
    LaunchedEffect(rateRepository) {
        rateRepository.fetchLatest()?.let { rates = it }
    }

    val numericAmount = amount.toDoubleOrNull() ?: 0.0

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

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(R.string.quick_amounts),
                        color = NavyBlue,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        QuickAmountButton("100") { amount = "100" }
                        QuickAmountButton("500") { amount = "500" }
                        QuickAmountButton("1000") { amount = "1000" }
                        QuickAmountButton("5000") { amount = "5000" }
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
