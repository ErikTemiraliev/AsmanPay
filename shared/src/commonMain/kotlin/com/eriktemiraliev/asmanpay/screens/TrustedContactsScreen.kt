package com.eriktemiraliev.asmanpay.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eriktemiraliev.asmanpay.resources.*
import com.eriktemiraliev.asmanpay.contacts.AddContactResult
import com.eriktemiraliev.asmanpay.contacts.ContactDirectory
import com.eriktemiraliev.asmanpay.contacts.ContactError
import com.eriktemiraliev.asmanpay.contacts.ContactRepository
import com.eriktemiraliev.asmanpay.contacts.PhoneNumbers
import com.eriktemiraliev.asmanpay.contacts.TrustedContact
import com.eriktemiraliev.asmanpay.contacts.VerificationStatus
import com.eriktemiraliev.asmanpay.platform.LocalKeyValueStores
import com.eriktemiraliev.asmanpay.platform.newId
import com.eriktemiraliev.asmanpay.platform.nowMillis
import com.eriktemiraliev.asmanpay.platform.formatDate
import com.eriktemiraliev.asmanpay.platform.formatDecimal
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource

private val PendingAmber = Color(0xFFB7791F)

@Composable
fun TrustedContactsScreen() {
    val stores = LocalKeyValueStores.current
    val repository = remember { ContactRepository(stores) }
    var directory by remember { mutableStateOf(repository.load()) }
    fun update(newDirectory: ContactDirectory) {
        directory = newDirectory
        repository.save(newDirectory)
    }

    var query by rememberSaveable { mutableStateOf("") }
    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var ratingContactId by rememberSaveable { mutableStateOf<String?>(null) }
    var removingContactId by rememberSaveable { mutableStateOf<String?>(null) }

    val visibleContacts = directory.search(query)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(LightBackground),
            contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp)
        ) {
            item {
                Header(
                    stringResource(Res.string.trusted_contacts_title),
                    stringResource(Res.string.trusted_contacts_subtitle)
                )
            }

            if (directory.contacts.isEmpty()) {
                item { EmptyDirectoryCard(onAdd = { showAddDialog = true }) }
            } else {
                item {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = { Text(stringResource(Res.string.contacts_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        colors = appTextFieldColors()
                    )
                }

                if (visibleContacts.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(Res.string.contacts_no_matches, query.trim()),
                            color = MutedText,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                items(visibleContacts, key = { it.id }) { contact ->
                    ContactCard(
                        contact = contact,
                        expanded = expandedId == contact.id,
                        onClick = { expandedId = if (expandedId == contact.id) null else contact.id },
                        onRateExchange = { ratingContactId = contact.id },
                        onRemove = { removingContactId = contact.id }
                    )
                }

                item { VerificationNotice() }
            }
        }

        if (directory.contacts.isNotEmpty()) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(Res.string.contacts_add)) },
                containerColor = BrightBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            )
        }
    }

    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { name, phone, note ->
                when (val result = directory.add(
                    name = name,
                    phone = phone,
                    note = note,
                    id = newId(),
                    nowMillis = nowMillis()
                )) {
                    is AddContactResult.Added -> {
                        update(result.directory)
                        query = ""
                        expandedId = result.contact.id
                        showAddDialog = false
                        null
                    }
                    is AddContactResult.Rejected -> result.error
                }
            }
        )
    }

    ratingContactId?.let(directory::find)?.let { contact ->
        RateExchangeDialog(
            contact = contact,
            onDismiss = { ratingContactId = null },
            onRate = { rating ->
                update(directory.recordExchange(contact.id, rating))
                ratingContactId = null
            }
        )
    }

    removingContactId?.let(directory::find)?.let { contact ->
        AlertDialog(
            onDismissRequest = { removingContactId = null },
            title = { Text(stringResource(Res.string.contacts_remove_title, contact.name)) },
            text = { Text(stringResource(Res.string.contacts_remove_message)) },
            confirmButton = {
                TextButton(onClick = {
                    update(directory.remove(contact.id))
                    if (expandedId == contact.id) expandedId = null
                    removingContactId = null
                }) {
                    Text(stringResource(Res.string.contacts_remove), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { removingContactId = null }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyDirectoryCard(onAdd: () -> Unit) {
    SectionCard(title = stringResource(Res.string.contacts_empty_title)) {
        Text(text = stringResource(Res.string.contacts_empty_message), color = MutedText)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
            shape = RoundedCornerShape(50)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(Res.string.contacts_add_first))
        }
    }
}

@Composable
private fun VerificationNotice() {
    Text(
        text = stringResource(Res.string.contacts_verification_notice),
        color = MutedText,
        fontSize = 13.sp,
        modifier = Modifier.padding(top = 12.dp)
    )
}

@Composable
fun ContactCard(
    contact: TrustedContact,
    expanded: Boolean,
    onClick: () -> Unit,
    onRateExchange: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SoftBlue, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.initials,
                        color = BrightBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (contact.verification == VerificationStatus.VERIFIED) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = stringResource(Res.string.verified_label),
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(text = PhoneNumbers.format(contact.phone), color = MutedText)
                    Text(
                        text = reputationText(contact),
                        color = BrightBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))
                VerificationBadge(contact.verification)
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                if (contact.note.isNotEmpty()) {
                    Text(text = contact.note)
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = stringResource(
                        Res.string.contacts_added_on,
                        formatDate(contact.addedAtMillis)
                    ),
                    color = MutedText,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    TextButton(onClick = onRateExchange) {
                        Text(stringResource(Res.string.contacts_rate_exchange))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onRemove) {
                        Text(stringResource(Res.string.contacts_remove), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun reputationText(contact: TrustedContact): String {
    val average = contact.averageRating ?: return stringResource(Res.string.contacts_no_exchanges)
    return stringResource(Res.string.rating_value, formatDecimal(average, 1, 1, useGrouping = false)) + " · " +
        pluralStringResource(Res.plurals.contacts_exchange_count, contact.exchangeCount, contact.exchangeCount)
}

@Composable
private fun VerificationBadge(status: VerificationStatus) {
    val (label, color) = when (status) {
        VerificationStatus.VERIFIED -> stringResource(Res.string.verified_label) to SuccessGreen
        VerificationStatus.PENDING -> stringResource(Res.string.pending_label) to PendingAmber
        VerificationStatus.UNVERIFIED -> stringResource(Res.string.unverified_label) to MutedText
    }
    Text(text = label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
}

@Composable
private fun AddContactDialog(
    onDismiss: () -> Unit,
    /** Returns null when the contact was added, or why it was rejected. */
    onSubmit: (name: String, phone: String, note: String) -> ContactError?
) {
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<ContactError?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.contacts_add_title)) },
        text = {
            Column {
                AppTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = stringResource(Res.string.contacts_name_label)
                )
                AppTextField(
                    value = phone,
                    onValueChange = { phone = it; error = null },
                    label = stringResource(Res.string.chinese_phone_label),
                    keyboardType = KeyboardType.Phone
                )
                TextField(
                    value = note,
                    onValueChange = { note = it; error = null },
                    label = { Text(stringResource(Res.string.contacts_note_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    maxLines = 3,
                    shape = RoundedCornerShape(18.dp),
                    colors = appTextFieldColors()
                )
                error?.let {
                    Text(
                        text = stringResource(it.messageRes()),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { error = onSubmit(name, phone, note) }) {
                Text(stringResource(Res.string.contacts_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        }
    )
}

private fun ContactError.messageRes(): StringResource = when (this) {
    ContactError.NAME_REQUIRED -> Res.string.contacts_error_name_required
    ContactError.NAME_TOO_LONG -> Res.string.contacts_error_name_too_long
    ContactError.INVALID_PHONE -> Res.string.validation_chinese_phone_required
    ContactError.DUPLICATE_PHONE -> Res.string.contacts_error_duplicate_phone
    ContactError.NOTE_TOO_LONG -> Res.string.contacts_error_note_too_long
}

@Composable
private fun RateExchangeDialog(
    contact: TrustedContact,
    onDismiss: () -> Unit,
    onRate: (Int) -> Unit
) {
    var rating by rememberSaveable { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.contacts_rate_title, contact.name)) },
        text = {
            Column {
                Text(stringResource(Res.string.contacts_rate_message), color = MutedText)
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    for (star in TrustedContact.MIN_RATING..TrustedContact.MAX_RATING) {
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = stringResource(Res.string.contacts_star_description, star),
                                tint = if (star <= rating) StarGold else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onRate(rating) }, enabled = rating > 0) {
                Text(stringResource(Res.string.contacts_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) }
        }
    )
}
