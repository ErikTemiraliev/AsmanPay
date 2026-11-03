package com.eriktemiraliev.asmanpay

import androidx.compose.ui.window.ComposeUIViewController
import com.eriktemiraliev.asmanpay.platform.UserDefaultsStores
import com.eriktemiraliev.asmanpay.screens.AsmanPayApp
import com.eriktemiraliev.asmanpay.ui.theme.AsmanPayTheme
import platform.UIKit.UIViewController

/** The iOS entry point. The Swift app in iosApp/ shows this screen. */
@Suppress("FunctionName", "unused") // Called from Swift as MainViewControllerKt.MainViewController()
fun MainViewController(): UIViewController = ComposeUIViewController {
    AsmanPayTheme {
        AsmanPayApp(stores = UserDefaultsStores())
    }
}
