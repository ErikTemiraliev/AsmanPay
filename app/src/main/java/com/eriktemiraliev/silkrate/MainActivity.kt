package com.eriktemiraliev.silkrate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.eriktemiraliev.silkrate.screens.SilkRateApp
import com.eriktemiraliev.silkrate.ui.theme.SilkRateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SilkRateTheme {
                SilkRateApp()
            }
        }
    }
}