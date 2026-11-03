package com.eriktemiraliev.asmanpay.favorites

import android.content.Context

/**
 * Keeps the user's favorite amounts on the device, so they survive restarts.
 * They are private to the app and are not shared with anyone.
 */
class FavoriteRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): FavoriteList =
        FavoriteList(prefs.getString(KEY_FAVORITES, null)?.let(FavoriteCodec::decode).orEmpty())

    fun save(list: FavoriteList) {
        prefs.edit().putString(KEY_FAVORITES, FavoriteCodec.encode(list.favorites)).apply()
    }

    private companion object {
        const val PREFS_NAME = "favorite_amounts"
        const val KEY_FAVORITES = "favorites"
    }
}
