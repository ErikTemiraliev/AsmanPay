package com.eriktemiraliev.asmanpay.screens

import androidx.compose.ui.res.stringResource
import com.eriktemiraliev.asmanpay.R
import android.util.Patterns
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

@Composable
fun SignUpScreen(
    onSignUpSuccess: (UserAccount) -> Unit,
    onGoToLogin: () -> Unit
) {
    var passportNumber by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )

            Text(
                text = stringResource(R.string.app_subtitle),
                fontSize = 15.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppTextField(
                value = passportNumber,
                onValueChange = { passportNumber = it },
                label = stringResource(R.string.passport_number_label)
            )

            AppTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = stringResource(R.string.full_real_name_label)
            )

            AppTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = stringResource(R.string.chinese_phone_label),
                keyboardType = KeyboardType.Phone
            )

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email_address_label),
                keyboardType = KeyboardType.Email
            )

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password_label),
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = {
                    errorMessage = validateSignUp(
                        passportNumber,
                        fullName,
                        phoneNumber,
                        email,
                        password
                    )

                    if (errorMessage.isEmpty()) {
                        onSignUpSuccess(
                            UserAccount(
                                passportNumber = passportNumber,
                                fullName = fullName,
                                phoneNumber = phoneNumber,
                                email = email,
                                password = password,
                                verified = true
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(stringResource(R.string.create_account_button))
            }

            TextButton(onClick = onGoToLogin) {
                Text(stringResource(R.string.already_have_account))
            }


        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: (UserAccount) -> Unit,
    onGoToSignup: () -> Unit
) {
    var loginInput by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val demoUser = UserAccount(
        passportNumber = "KG1234567",
        fullName = "Erik Temiraliev",
        phoneNumber = "+86 138 0013 8000",
        email = "erik@example.com",
        password = "123456",
        verified = true
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = stringResource(R.string.welcome_back),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )

        Text(
            text = stringResource(R.string.login_subtitle),
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(28.dp))

        AppTextField(
            value = loginInput,
            onValueChange = { loginInput = it },
            label = stringResource(R.string.email_or_phone_label)
        )

        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(R.string.password_label),
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                val loginOk = loginInput == demoUser.email ||
                        loginInput == demoUser.phoneNumber ||
                        loginInput == "13800138000"

                if (loginOk && password == demoUser.password) {
                    onLoginSuccess(demoUser)
                } else {
                    errorMessage = "Demo login: erik@example.com / 123456"
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(stringResource(R.string.log_in_button))
        }

        TextButton(onClick = onGoToSignup) {
            Text(stringResource(R.string.create_new_account))
        }

        Spacer(modifier = Modifier.height(16.dp))

    }
}

fun validateSignUp(
    passport: String,
    fullName: String,
    phone: String,
    email: String,
    password: String
): String {
    if (passport.isBlank()) return "Passport number is required."
    if (fullName.trim().split(" ").size < 2) return "Please use your real full name."
    if (!isValidChinesePhone(phone)) return "Please enter a valid Chinese phone number."
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Please enter a valid email address."
    if (password.length < 6) return "Password must be at least 6 characters."
    return ""
}

fun isValidChinesePhone(phone: String): Boolean {
    val cleaned = phone
        .replace(" ", "")
        .replace("-", "")
        .replace("(", "")
        .replace(")", "")

    // International phone format:
    // Must start with +, then country code and phone number.
    // Total digits after + should be 7 to 15 digits.
    // Examples: +8613800138000, +996555123456, +77771234567
    val internationalPhone = Regex("^\\+[1-9]\\d{6,14}$")

    return internationalPhone.matches(cleaned)
}
