package com.eriktemiraliev.asmanpay.favorites

import com.eriktemiraliev.asmanpay.platform.KeyValueStores

/**
 * Keeps the user's favorite amounts on the device, so they survive restarts.
 * They are private to the app and are not shared with anyone.
 */
class FavoriteRepository(stores: KeyValueStores) {

    private val store = stores.open(STORE_NAME)

    fun load(): FavoriteList =
        FavoriteList(store.getString(KEY_FAVORITES)?.let(FavoriteCodec::decode).orEmpty())

    fun save(list: FavoriteList) {
        store.putString(KEY_FAVORITES, FavoriteCodec.encode(list.favorites))
    }

    private companion object {
        const val STORE_NAME = "favorite_amounts"
        const val KEY_FAVORITES = "favorites"
    }
}
