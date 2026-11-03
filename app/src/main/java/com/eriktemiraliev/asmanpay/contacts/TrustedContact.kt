package com.eriktemiraliev.asmanpay.contacts

/**
 * How far a contact's identity has been confirmed.
 *
 * Only AsmanPay's identity verification can mark someone [VERIFIED]; a contact the
 * user adds by hand always starts as [UNVERIFIED]. The app has no verification
 * service yet, so [VERIFIED] and [PENDING] are stored and shown but never set
 * on the device. They exist so the directory is ready for that service.
 */
enum class VerificationStatus {
    VERIFIED,
    PENDING,
    UNVERIFIED
}

/**
 * Someone the user exchanges currency with.
 *
 * @param phone The phone number in E.164 form (for example "+8613800138000"); see [PhoneNumbers].
 * @param ratings The user's 1–5 star rating of each completed exchange with this contact, oldest first.
 */
data class TrustedContact(
    val id: String,
    val name: String,
    val phone: String,
    val verification: VerificationStatus = VerificationStatus.UNVERIFIED,
    val ratings: List<Int> = emptyList(),
    val note: String = "",
    val addedAtMillis: Long = 0L
) {
    /** How many exchanges the user has recorded with this contact. */
    val exchangeCount: Int get() = ratings.size

    /** The average rating across recorded exchanges, or null before the first one. */
    val averageRating: Double? get() = if (ratings.isEmpty()) null else ratings.average()

    /** Up to two initials for the avatar, such as "AZ" for "Alina Zhang". */
    val initials: String
        get() = name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
        const val MAX_NAME_LENGTH = 60
        const val MAX_NOTE_LENGTH = 200
    }
}
