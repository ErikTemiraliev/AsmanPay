package com.eriktemiraliev.asmanpay.contacts

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactCodecTest {

    private val contacts = listOf(
        TrustedContact(
            id = "1",
            name = "Alina Zhang",
            phone = "+8613922445108",
            verification = VerificationStatus.VERIFIED,
            ratings = listOf(5, 4, 5),
            note = "Line one\nTabbed\there \\ backslash",
            addedAtMillis = 1730592151000L
        ),
        TrustedContact(id = "2", name = "Тимур Беков", phone = "+996555123456")
    )

    @Test
    fun roundTripsContactsIncludingSpecialCharacters() {
        assertEquals(contacts, ContactCodec.decode(ContactCodec.encode(contacts)))
    }

    @Test
    fun roundTripsAnEmptyDirectory() {
        assertEquals(emptyList<TrustedContact>(), ContactCodec.decode(ContactCodec.encode(emptyList())))
    }

    @Test
    fun returnsNothingForUnknownFormats() {
        assertEquals(emptyList<TrustedContact>(), ContactCodec.decode(""))
        assertEquals(emptyList<TrustedContact>(), ContactCodec.decode("{\"contacts\":[]}"))
    }

    @Test
    fun skipsDamagedLinesButKeepsTheRest() {
        val encoded = ContactCodec.encode(contacts)
        val damaged = encoded.lines().toMutableList().apply {
            add(1, "not\ta\tcontact")
            add("3\tBad Rating\t+8613800138000\tUNVERIFIED\t9\t\t0")
            add("4\tBad Phone\t12345\tUNVERIFIED\t\t\t0")
            add("5\tBad Status\t+8613800138001\tTRUSTED\t\t\t0")
        }.joinToString("\n")

        assertEquals(contacts, ContactCodec.decode(damaged))
    }
}
