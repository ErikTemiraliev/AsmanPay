package com.eriktemiraliev.asmanpay.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TrustedContactsScreen() {
    val contacts = listOf(
        TrustedContact("Erik Temiraliev", "+86 138 0013 8000", 4.9, true),
        TrustedContact("Alina Zhang", "+86 139 2244 5108", 4.8, true),
        TrustedContact("Timur Bekov", "+86 136 8821 3345", 4.6, true),
        TrustedContact("Aibek Nurmatov", "+86 135 7712 9044", 4.5, false)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
    ) {
        item {
            Header("Trusted Contacts", "Safe exchange contacts")
        }

        items(contacts) { contact ->
            ContactCard(contact)
        }
    }
}

@Composable
fun ContactCard(contact: TrustedContact) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(NavyBlue, RoundedCornerShape(50)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    if (contact.verified) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(text = contact.phone, color = Color.Gray)
                Text(
                    text = "Rating: ${contact.rating}",
                    color = BrightBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = if (contact.verified) "Verified" else "Pending",
                color = if (contact.verified) SuccessGreen else Color.Gray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
