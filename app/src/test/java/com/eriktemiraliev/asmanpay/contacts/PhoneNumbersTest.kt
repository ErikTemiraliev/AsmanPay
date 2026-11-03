package com.eriktemiraliev.asmanpay.contacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PhoneNumbersTest {

    @Test
    fun normalizesSpacesDashesAndBrackets() {
        assertEquals("+8613800138000", PhoneNumbers.normalize(" +86 (138) 0013-8000 "))
        assertEquals("+996555123456", PhoneNumbers.normalize("+996 555.123.456"))
    }

    @Test
    fun treatsLeadingDoubleZeroAsPlus() {
        assertEquals("+77011234567", PhoneNumbers.normalize("007 701 123 4567"))
    }

    @Test
    fun rejectsNumbersWithoutCountryCodeOrWithLetters() {
        assertNull(PhoneNumbers.normalize("13800138000"))
        assertNull(PhoneNumbers.normalize("+86 138 abc 8000"))
        assertNull(PhoneNumbers.normalize("+0123456789"))
        assertNull(PhoneNumbers.normalize("+1234567"))
        assertNull(PhoneNumbers.normalize("+1234567890123456"))
        assertNull(PhoneNumbers.normalize(""))
    }

    @Test
    fun formatsCorridorNumbersForDisplay() {
        assertEquals("+86 138 0013 8000", PhoneNumbers.format("+8613800138000"))
        assertEquals("+996 555 123 456", PhoneNumbers.format("+996555123456"))
        assertEquals("+7 701 123 4567", PhoneNumbers.format("+77011234567"))
    }
}
