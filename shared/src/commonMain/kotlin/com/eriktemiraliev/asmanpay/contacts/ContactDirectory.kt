package com.eriktemiraliev.asmanpay.contacts

/** Why a new contact was rejected. */
enum class ContactError {
    NAME_REQUIRED,
    NAME_TOO_LONG,
    INVALID_PHONE,
    DUPLICATE_PHONE,
    NOTE_TOO_LONG
}

sealed interface AddContactResult {
    data class Added(val directory: ContactDirectory, val contact: TrustedContact) : AddContactResult
    data class Rejected(val error: ContactError) : AddContactResult
}

/**
 * The user's trusted-contacts directory. Immutable: every change returns a new directory,
 * which the screen saves through [ContactRepository].
 *
 * Contacts are listed most trustworthy first: verified identities, then those still
 * pending, then unverified; within each group by average rating, then by number of exchanges.
 */
class ContactDirectory(contacts: List<TrustedContact> = emptyList()) {

    /** Every contact, in trust order. */
    val contacts: List<TrustedContact> = contacts.sortedWith(trustOrder)

    fun find(id: String): TrustedContact? = contacts.firstOrNull { it.id == id }

    /**
     * Adds a contact the user entered by hand. It starts [VerificationStatus.UNVERIFIED]
     * with no exchanges. The same phone number can't be added twice.
     */
    fun add(
        name: String,
        phone: String,
        note: String = "",
        id: String,
        nowMillis: Long
    ): AddContactResult {
        val cleanName = name.trim().replace(Regex("\\s+"), " ")
        val cleanNote = note.trim()
        val normalizedPhone = PhoneNumbers.normalize(phone)

        val error = when {
            cleanName.isEmpty() -> ContactError.NAME_REQUIRED
            cleanName.length > TrustedContact.MAX_NAME_LENGTH -> ContactError.NAME_TOO_LONG
            normalizedPhone == null -> ContactError.INVALID_PHONE
            contacts.any { it.phone == normalizedPhone } -> ContactError.DUPLICATE_PHONE
            cleanNote.length > TrustedContact.MAX_NOTE_LENGTH -> ContactError.NOTE_TOO_LONG
            else -> null
        }
        if (error != null) return AddContactResult.Rejected(error)

        val contact = TrustedContact(
            id = id,
            name = cleanName,
            phone = normalizedPhone!!,
            note = cleanNote,
            addedAtMillis = nowMillis
        )
        return AddContactResult.Added(ContactDirectory(contacts + contact), contact)
    }

    fun remove(id: String): ContactDirectory = ContactDirectory(contacts.filterNot { it.id == id })

    /** Records a completed exchange with [id], rated 1 to 5 stars. Unknown ids are ignored. */
    fun recordExchange(id: String, rating: Int): ContactDirectory {
        require(rating in TrustedContact.MIN_RATING..TrustedContact.MAX_RATING) {
            "Rating must be between ${TrustedContact.MIN_RATING} and ${TrustedContact.MAX_RATING}, was $rating"
        }
        return ContactDirectory(
            contacts.map { if (it.id == id) it.copy(ratings = it.ratings + rating) else it }
        )
    }

    /**
     * Contacts whose name or note contains [query] (ignoring case), or whose phone
     * number contains its digits. A blank query returns everyone.
     */
    fun search(query: String): List<TrustedContact> {
        val text = query.trim()
        if (text.isEmpty()) return contacts
        val digits = text.filter { it.isDigit() }
        return contacts.filter { contact ->
            contact.name.contains(text, ignoreCase = true) ||
                contact.note.contains(text, ignoreCase = true) ||
                (digits.length >= 3 && contact.phone.contains(digits))
        }
    }

    override fun equals(other: Any?): Boolean = other is ContactDirectory && other.contacts == contacts

    override fun hashCode(): Int = contacts.hashCode()

    private companion object {
        val trustOrder: Comparator<TrustedContact> =
            compareBy<TrustedContact> { it.verification.ordinal }
                .thenByDescending { it.averageRating ?: 0.0 }
                .thenByDescending { it.exchangeCount }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { it.name }
                .thenBy { it.id }
    }
}
