package com.eriktemiraliev.asmanpay.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.res.stringResource
import com.eriktemiraliev.asmanpay.R
import com.eriktemiraliev.asmanpay.exchange.ExchangeRates
import com.eriktemiraliev.asmanpay.exchange.RateRepository
import com.eriktemiraliev.asmanpay.exchange.RateSource
import com.eriktemiraliev.asmanpay.exchange.RateTable
import com.eriktemiraliev.asmanpay.exchange.calculateExchange
import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DateFormat
import java.util.Date

private val NavyBlue = Color(0xFF0B1F3A)
private val BrightBlue = Color(0xFF1683E8)
private val LightBackground = Color(0xFFF4F7FB)
private val SuccessGreen = Color(0xFF1E9E5A)

data class UserAccount(
    val passportNumber: String,
    val fullName: String,
    val phoneNumber: String,
    val email: String,
    val password: String,
    val verified: Boolean = true
)

data class TrustedContact(
    val name: String,
    val phone: String,
    val rating: Double,
    val verified: Boolean
)

data class FavoriteAmount(
    val amount: String,
    val currency: String
)

@Composable
fun AsmanPayApp() {
    var currentUser by remember { mutableStateOf<UserAccount?>(null) }
    var showLogin by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = LightBackground
    ) {
        if (currentUser == null) {
            if (showLogin) {
                LoginScreen(
                    onLoginSuccess = { currentUser = it },
                    onGoToSignup = { showLogin = false }
                )
            } else {
                SignUpScreen(
                    onSignUpSuccess = { currentUser = it },
                    onGoToLogin = { showLogin = true }
                )
            }
        } else {
            MainAppScreen(
                user = currentUser!!,
                onUserUpdated = { currentUser = it }
            )
        }
    }
}

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

@Composable
fun MainAppScreen(
    user: UserAccount,
    onUserUpdated: (UserAccount) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf("Home", "Contacts", "Favorites", "Profile")
    val tabIcons = listOf(
        Icons.Default.Home,
        Icons.Default.CheckCircle,
        Icons.Default.Star,
        Icons.Default.Person
    )


    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = title
                            )
                        },
                        label = { Text(title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrightBlue,
                            selectedTextColor = BrightBlue,
                            indicatorColor = Color(0xFFE8F2FF)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> TrustedContactsScreen()
                2 -> FavoriteAmountsScreen()
                3 -> ProfileScreen(user = user, onUserUpdated = onUserUpdated)
            }
        }
    }
}

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
fun ProfileScreen(
    user: UserAccount,
    onUserUpdated: (UserAccount) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var email by remember { mutableStateOf(user.email) }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var avatarColorIndex by remember { mutableIntStateOf(0) }

    val profileUpdatedText = stringResource(R.string.profile_updated)

    val avatarColors = listOf(
        BrightBlue,
        Color(0xFF304FFE),
        Color(0xFF00695C),
        Color(0xFF4A148C),
        Color(0xFF1565C0)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.profile_title),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = NavyBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.account_settings),
                color = Color.Gray,
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = NavyBlue)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileAvatar(
                            fullName = user.fullName,
                            backgroundColor = avatarColors[avatarColorIndex]
                        )

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.fullName,
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = stringResource(R.string.verified_account_text),
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            avatarColorIndex = (avatarColorIndex + 1) % avatarColors.size
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = NavyBlue
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.change_picture),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SectionCard(title = stringResource(R.string.account_identity)) {
                Text(
                    text = stringResource(R.string.passport_number_value, user.passportNumber),
                    color = Color(0xFF222222),
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(
                        R.string.verified_status_value,
                        if (user.verified) stringResource(R.string.verified_label)
                        else stringResource(R.string.pending_label)
                    ),
                    color = SuccessGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = stringResource(R.string.contact_information)) {
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = stringResource(R.string.security)) {
                AppTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = stringResource(R.string.new_password_label),
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val updatedPassword = newPassword.ifBlank {
                        user.password
                    }

                    onUserUpdated(
                        user.copy(
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = email,
                            password = updatedPassword
                        )
                    )

                    newPassword = ""
                    message = profileUpdatedText
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(R.string.save_changes),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProfileAvatar(
    fullName: String,
    backgroundColor: Color
) {
    val initials = fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Box(
        modifier = Modifier
            .size(70.dp)
            .background(backgroundColor, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifBlank { "SR" },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = title,
                color = NavyBlue,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
fun Header(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(bottom = 18.dp)) {
        Text(
            text = title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = NavyBlue
        )
        Text(
            text = subtitle,
            color = Color.Gray,
            fontSize = 15.sp
        )
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(14.dp)
    )
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
