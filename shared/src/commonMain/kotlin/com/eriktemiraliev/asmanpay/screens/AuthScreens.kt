package com.eriktemiraliev.asmanpay.screens

import org.jetbrains.compose.resources.stringResource
import com.eriktemiraliev.asmanpay.resources.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
            Spacer(modifier = Modifier.height(32.dp))

            AsmanLogo(size = 72.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(Res.string.app_name),
                style = MaterialTheme.typography.headlineLarge,
                color = NavyBlue
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(Res.string.app_subtitle),
                fontSize = 15.sp,
                color = MutedText
            )

            Spacer(modifier = Modifier.height(28.dp))

            AppTextField(
                value = passportNumber,
                onValueChange = { passportNumber = it },
                label = stringResource(Res.string.passport_number_label)
            )

            AppTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = stringResource(Res.string.full_real_name_label)
            )

            AppTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = stringResource(Res.string.chinese_phone_label),
                keyboardType = KeyboardType.Phone
            )

            AppTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(Res.string.email_address_label),
                keyboardType = KeyboardType.Email
            )

            AppTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(Res.string.password_label),
                keyboardType = KeyboardType.Password,
                isPassword = true
            )

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = ErrorRed,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            PrimaryButton(
                text = stringResource(Res.string.create_account_button),
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
                }
            )

            TextButton(onClick = onGoToLogin) {
                Text(stringResource(Res.string.already_have_account), color = BrightBlue, fontWeight = FontWeight.SemiBold)
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
        Spacer(modifier = Modifier.height(56.dp))

        AsmanLogo(size = 72.dp)

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = stringResource(Res.string.welcome_back),
            style = MaterialTheme.typography.headlineLarge,
            color = NavyBlue
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(Res.string.login_subtitle),
            color = MutedText
        )

        Spacer(modifier = Modifier.height(28.dp))

        AppTextField(
            value = loginInput,
            onValueChange = { loginInput = it },
            label = stringResource(Res.string.email_or_phone_label)
        )

        AppTextField(
            value = password,
            onValueChange = { password = it },
            label = stringResource(Res.string.password_label),
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = ErrorRed,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        PrimaryButton(
            text = stringResource(Res.string.log_in_button),
            onClick = {
                val loginOk = loginInput == demoUser.email ||
                        loginInput == demoUser.phoneNumber ||
                        loginInput == "13800138000"

                if (loginOk && password == demoUser.password) {
                    onLoginSuccess(demoUser)
                } else {
                    errorMessage = "Demo login: erik@example.com / 123456"
                }
            }
        )

        TextButton(onClick = onGoToSignup) {
            Text(stringResource(Res.string.create_new_account), color = BrightBlue, fontWeight = FontWeight.SemiBold)
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
    if (!EMAIL_ADDRESS.matches(email)) return "Please enter a valid email address."
    if (password.length < 6) return "Password must be at least 6 characters."
    return ""
}

/** The same email check Android's Patterns.EMAIL_ADDRESS uses, so it works on iOS too. */
private val EMAIL_ADDRESS = Regex(
    "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
)

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
