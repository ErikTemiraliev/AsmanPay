package com.eriktemiraliev.asmanpay.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eriktemiraliev.asmanpay.favorites.FavoriteAmount
import com.eriktemiraliev.asmanpay.platform.KeyValueStores
import com.eriktemiraliev.asmanpay.platform.LocalKeyValueStores

/**
 * The whole app, shared by Android and iOS.
 *
 * @param stores Where the app saves contacts, favorites and rates on this device.
 *   Android passes SharedPreferences, iOS passes NSUserDefaults.
 */
@Composable
fun AsmanPayApp(stores: KeyValueStores) {
    CompositionLocalProvider(LocalKeyValueStores provides stores) {
        AsmanPayContent()
    }
}

@Composable
private fun AsmanPayContent() {
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
fun MainAppScreen(
    user: UserAccount,
    onUserUpdated: (UserAccount) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    // A favorite picked on the Favorites tab, to open in the converter.
    var pendingConversion by remember { mutableStateOf<FavoriteAmount?>(null) }

    val tabs = listOf("Home", "Contacts", "Favorites", "Profile")
    val tabIcons = listOf(
        Icons.Default.Home,
        Icons.Default.CheckCircle,
        Icons.Default.Star,
        Icons.Default.Person
    )


    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = {
                            pendingConversion = null
                            selectedTab = index
                        },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = title
                            )
                        },
                        label = { Text(title, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrightBlue,
                            selectedTextColor = BrightBlue,
                            indicatorColor = SoftBlue,
                            unselectedIconColor = MutedText,
                            unselectedTextColor = MutedText
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> HomeScreen(initialConversion = pendingConversion)
                1 -> TrustedContactsScreen()
                2 -> FavoriteAmountsScreen(onUse = {
                    pendingConversion = it
                    selectedTab = 0
                })
                3 -> ProfileScreen(user = user, onUserUpdated = onUserUpdated)
            }
        }
    }
}
