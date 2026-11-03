package com.eriktemiraliev.asmanpay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.eriktemiraliev.asmanpay.screens.AsmanPayApp
import com.eriktemiraliev.asmanpay.ui.theme.AsmanPayTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AsmanPayTheme {
                AsmanPayApp()
            }
        }
    }
}