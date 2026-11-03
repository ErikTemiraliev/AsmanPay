package com.eriktemiraliev.asmanpay.platform

import platform.Foundation.NSUserDefaults

/** iOS storage: every store lives in the app's NSUserDefaults, with keys prefixed by the store name. */
class UserDefaultsStores : KeyValueStores {

    override fun open(name: String): KeyValueStore = object : KeyValueStore {
        private val defaults = NSUserDefaults.standardUserDefaults

        override fun getString(key: String): String? = defaults.stringForKey("$name.$key")

        override fun putString(key: String, value: String) {
            defaults.setObject(value, forKey = "$name.$key")
        }
    }
}
