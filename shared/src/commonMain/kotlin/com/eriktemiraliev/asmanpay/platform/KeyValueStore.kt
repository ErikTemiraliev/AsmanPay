package com.eriktemiraliev.asmanpay.platform

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * A small text store that is private to the app and survives restarts.
 * On Android it's SharedPreferences; on iOS it's NSUserDefaults.
 */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

/** Opens a named [KeyValueStore]. Each name is kept separately, like a SharedPreferences file. */
fun interface KeyValueStores {
    fun open(name: String): KeyValueStore
}

/** The device storage the screens save to. Each platform's entry point provides it. */
val LocalKeyValueStores = staticCompositionLocalOf<KeyValueStores> {
    error("No KeyValueStores provided. Wrap the UI in AsmanPayApp(stores = ...).")
}
