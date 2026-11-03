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
