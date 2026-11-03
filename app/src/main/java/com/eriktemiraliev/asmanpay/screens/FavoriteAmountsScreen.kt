package com.eriktemiraliev.asmanpay.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun FavoriteAmountsScreen() {
    var favorites by remember {
        mutableStateOf(
            listOf(
                FavoriteAmount("100", "CNY"),
                FavoriteAmount("500", "CNY"),
                FavoriteAmount("1000", "CNY"),
                FavoriteAmount("100", "USD")
            )
        )
    }

    var newAmount by remember { mutableStateOf("") }
    var newCurrency by remember { mutableStateOf("CNY") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
    ) {
        item {
            Header("Favorite Amounts", "Save common exchange amounts")

            AppTextField(
                value = newAmount,
                onValueChange = { newAmount = it },
                label = "Amount",
                keyboardType = KeyboardType.Number
            )

            CurrencySelector(
                selected = newCurrency,
                onSelected = { newCurrency = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (newAmount.isNotBlank()) {
                        favorites = favorites + FavoriteAmount(newAmount, newCurrency)
                        newAmount = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Add Favorite Amount")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        items(favorites) { item ->
            FavoriteAmountCard(item)
        }
    }
}

@Composable
fun FavoriteAmountCard(item: FavoriteAmount) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFFE8F2FF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "FX",
                    color = BrightBlue,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "${item.amount} ${item.currency}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
