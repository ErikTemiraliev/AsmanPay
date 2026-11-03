package com.eriktemiraliev.asmanpay.contacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactDirectoryTest {

    private fun ContactDirectory.added(name: String, phone: String, id: String = name, note: String = ""): ContactDirectory {
        val result = add(name = name, phone = phone, note = note, id = id, nowMillis = 1_000L)
        assertTrue("Expected $name to be added but got $result", result is AddContactResult.Added)
        return (result as AddContactResult.Added).directory
    }

    private fun ContactDirectory.rejection(name: String, phone: String, note: String = ""): ContactError? =
        (add(name = name, phone = phone, note = note, id = "x", nowMillis = 0L) as? AddContactResult.Rejected)?.error

    @Test
    fun newContactsStartUnverifiedWithNoExchanges() {
        val result = ContactDirectory().add("  Alina   Zhang ", "+86 139 2244 5108", " Classmate ", "a1", 42L)
        val contact = (result as AddContactResult.Added).contact

        assertEquals(
            TrustedContact(
                id = "a1",
                name = "Alina Zhang",
                phone = "+8613922445108",
                verification = VerificationStatus.UNVERIFIED,
                ratings = emptyList(),
                note = "Classmate",
                addedAtMillis = 42L
            ),
            contact
        )
        assertEquals(listOf(contact), result.directory.contacts)
        assertNull(contact.averageRating)
        assertEquals(0, contact.exchangeCount)
        assertEquals("AZ", contact.initials)
    }

    @Test
    fun rejectsInvalidInput() {
        val directory = ContactDirectory()
        assertEquals(ContactError.NAME_REQUIRED, directory.rejection("   ", "+8613800138000"))
        assertEquals(ContactError.NAME_TOO_LONG, directory.rejection("a".repeat(61), "+8613800138000"))
        assertEquals(ContactError.INVALID_PHONE, directory.rejection("Timur", "13800138000"))
        assertEquals(ContactError.NOTE_TOO_LONG, directory.rejection("Timur", "+8613800138000", "n".repeat(201)))
    }

    @Test
    fun rejectsTheSamePhoneNumberWrittenDifferently() {
        val directory = ContactDirectory().added("Timur Bekov", "+86 136 8821 3345")
        assertEquals(ContactError.DUPLICATE_PHONE, directory.rejection("Timur B.", "0086-136-8821-3345"))
    }

    @Test
    fun recordsExchangesAndAveragesRatings() {
        val directory = ContactDirectory()
            .added("Aibek", "+996555123456")
            .recordExchange("Aibek", 5)
            .recordExchange("Aibek", 4)

        val aibek = directory.find("Aibek")!!
        assertEquals(listOf(5, 4), aibek.ratings)
        assertEquals(2, aibek.exchangeCount)
        assertEquals(4.5, aibek.averageRating!!, 1e-9)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsRatingsOutsideOneToFive() {
        ContactDirectory().added("Aibek", "+996555123456").recordExchange("Aibek", 6)
    }

    @Test
    fun listsVerifiedFirstThenByRatingThenByExchanges() {
        val directory = ContactDirectory(
            listOf(
                TrustedContact("u-low", "Unverified Low", "+8613800000001", ratings = listOf(2)),
                TrustedContact("u-high-few", "Unverified High Few", "+8613800000002", ratings = listOf(5)),
                TrustedContact("u-high-many", "Unverified High Many", "+8613800000003", ratings = listOf(5, 5)),
                TrustedContact("p", "Pending", "+8613800000004", VerificationStatus.PENDING),
                TrustedContact("v", "Verified", "+8613800000005", VerificationStatus.VERIFIED, ratings = listOf(3))
            )
        )

        assertEquals(
            listOf("v", "p", "u-high-many", "u-high-few", "u-low"),
            directory.contacts.map { it.id }
        )
    }

    @Test
    fun removesContacts() {
        val directory = ContactDirectory()
            .added("Alina", "+8613922445108")
            .added("Timur", "+8613688213345")
            .remove("Alina")

        assertEquals(listOf("Timur"), directory.contacts.map { it.id })
    }

    @Test
    fun searchesByNameNoteAndPhoneDigits() {
        val directory = ContactDirectory()
            .added("Alina Zhang", "+8613922445108", note = "Beijing campus")
            .added("Timur Bekov", "+996555123456", note = "Osh bazaar")

        assertEquals(listOf("Alina Zhang"), directory.search("zhang").map { it.id })
        assertEquals(listOf("Timur Bekov"), directory.search("BAZAAR").map { it.id })
        assertEquals(listOf("Timur Bekov"), directory.search("555 123").map { it.id })
        assertEquals(2, directory.search("  ").size)
        assertTrue(directory.search("nobody").isEmpty())
    }
}
