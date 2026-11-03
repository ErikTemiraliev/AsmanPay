package com.eriktemiraliev.asmanpay.platform

import android.content.Context

/** Android storage: each store name is its own SharedPreferences file, as before the iOS port. */
class SharedPreferencesStores(context: Context) : KeyValueStores {

    private val appContext = context.applicationContext

    override fun open(name: String): KeyValueStore = object : KeyValueStore {
        private val prefs = appContext.getSharedPreferences(name, Context.MODE_PRIVATE)

        override fun getString(key: String): String? = prefs.getString(key, null)

        override fun putString(key: String, value: String) {
            prefs.edit().putString(key, value).apply()
        }
    }
}
