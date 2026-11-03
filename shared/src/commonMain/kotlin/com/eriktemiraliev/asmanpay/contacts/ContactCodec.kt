package com.eriktemiraliev.asmanpay.contacts

/**
 * Turns the directory into text for storage and back.
 *
 * The format is a version line, then one contact per line with tab-separated fields:
 * id, name, phone, verification, ratings (comma-separated), note, added-at millis.
 * Backslashes, tabs and line breaks inside fields are escaped.
 *
 * It's hand-rolled (like the rate parser) so it can be unit tested on the JVM,
 * where Android's org.json is only a stub. A damaged line is skipped instead of
 * losing the whole directory.
 */
object ContactCodec {

    private const val HEADER = "asmanpay-contacts/1"
    private const val FIELD_COUNT = 7

    fun encode(contacts: List<TrustedContact>): String = buildString {
        append(HEADER)
        for (contact in contacts) {
            append('\n')
            append(
                listOf(
                    contact.id,
                    contact.name,
                    contact.phone,
                    contact.verification.name,
                    contact.ratings.joinToString(","),
                    contact.note,
                    contact.addedAtMillis.toString()
                ).joinToString("\t") { escape(it) }
            )
        }
    }

    /** Returns the stored contacts, or an empty list if [text] isn't in this format. */
    fun decode(text: String): List<TrustedContact> {
        val lines = text.split('\n')
        if (lines.firstOrNull() != HEADER) return emptyList()
        return lines.drop(1).mapNotNull(::decodeLine)
    }

    private fun decodeLine(line: String): TrustedContact? {
        val fields = line.split('\t').map(::unescape)
        if (fields.size != FIELD_COUNT) return null
        val (id, name, phone, verification, ratings) = fields

        val status = VerificationStatus.entries.firstOrNull { it.name == verification } ?: return null
        val ratingList = if (ratings.isEmpty()) emptyList() else ratings.split(',').map {
            it.toIntOrNull()?.takeIf { r -> r in TrustedContact.MIN_RATING..TrustedContact.MAX_RATING }
                ?: return null
        }
        if (id.isEmpty() || name.isEmpty() || PhoneNumbers.normalize(phone) != phone) return null

        return TrustedContact(
            id = id,
            name = name,
            phone = phone,
            verification = status,
            ratings = ratingList,
            note = fields[5],
            addedAtMillis = fields[6].toLongOrNull() ?: return null
        )
    }

    private fun escape(value: String): String = buildString {
        for (c in value) {
            when (c) {
                '\\' -> append("\\\\")
                '\t' -> append("\\t")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                else -> append(c)
            }
        }
    }

    private fun unescape(value: String): String = buildString {
        var i = 0
        while (i < value.length) {
            val c = value[i]
            if (c == '\\' && i + 1 < value.length) {
                when (value[i + 1]) {
                    't' -> append('\t')
                    'n' -> append('\n')
                    'r' -> append('\r')
                    else -> append(value[i + 1])
                }
                i += 2
            } else {
                append(c)
                i++
            }
        }
    }
}
