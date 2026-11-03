package com.eriktemiraliev.asmanpay.contacts

import android.content.Context

/**
 * Keeps the user's trusted-contacts directory on the device, so it survives restarts.
 * Contacts are private to the app and are not shared with anyone.
 */
class ContactRepository(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): ContactDirectory =
        ContactDirectory(prefs.getString(KEY_CONTACTS, null)?.let(ContactCodec::decode).orEmpty())

    fun save(directory: ContactDirectory) {
        prefs.edit().putString(KEY_CONTACTS, ContactCodec.encode(directory.contacts)).apply()
    }

    private companion object {
        const val PREFS_NAME = "trusted_contacts"
        const val KEY_CONTACTS = "contacts"
    }
}
