package com.eriktemiraliev.asmanpay.screens

data class UserAccount(
    val passportNumber: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val verified: Boolean = true
)

data class FavoriteAmount(
    val amount: String,
    val currency: String
)
