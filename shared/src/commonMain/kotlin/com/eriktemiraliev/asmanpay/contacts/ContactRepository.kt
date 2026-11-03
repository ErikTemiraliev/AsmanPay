package com.eriktemiraliev.asmanpay.contacts

import com.eriktemiraliev.asmanpay.platform.KeyValueStores

/**
 * Keeps the user's trusted-contacts directory on the device, so it survives restarts.
 * Contacts are private to the app and are not shared with anyone.
 */
class ContactRepository(stores: KeyValueStores) {

    private val store = stores.open(STORE_NAME)

    fun load(): ContactDirectory =
        ContactDirectory(store.getString(KEY_CONTACTS)?.let(ContactCodec::decode).orEmpty())

    fun save(directory: ContactDirectory) {
        store.putString(KEY_CONTACTS, ContactCodec.encode(directory.contacts))
    }

    private companion object {
        const val STORE_NAME = "trusted_contacts"
        const val KEY_CONTACTS = "contacts"
    }
}
