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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eriktemiraliev.asmanpay.R
import com.eriktemiraliev.asmanpay.contacts.AddContactResult
import com.eriktemiraliev.asmanpay.contacts.ContactDirectory
import com.eriktemiraliev.asmanpay.contacts.ContactError
import com.eriktemiraliev.asmanpay.contacts.ContactRepository
import com.eriktemiraliev.asmanpay.contacts.PhoneNumbers
import com.eriktemiraliev.asmanpay.contacts.TrustedContact
import com.eriktemiraliev.asmanpay.contacts.VerificationStatus
import java.text.DateFormat
import java.util.Date
import java.util.UUID

private val PendingAmber = Color(0xFFB7791F)

@Composable
fun TrustedContactsScreen() {
    val context = LocalContext.current
    val repository = remember { ContactRepository(context) }
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
                    stringResource(R.string.trusted_contacts_title),
                    stringResource(R.string.trusted_contacts_subtitle)
                )
            }

            if (directory.contacts.isEmpty()) {
                item { EmptyDirectoryCard(onAdd = { showAddDialog = true }) }
            } else {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = { Text(stringResource(R.string.contacts_search_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                if (visibleContacts.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.contacts_no_matches, query.trim()),
                            color = Color.Gray,
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
                text = { Text(stringResource(R.string.contacts_add)) },
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
                    id = UUID.randomUUID().toString(),
                    nowMillis = System.currentTimeMillis()
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
            title = { Text(stringResource(R.string.contacts_remove_title, contact.name)) },
            text = { Text(stringResource(R.string.contacts_remove_message)) },
            confirmButton = {
                TextButton(onClick = {
                    update(directory.remove(contact.id))
                    if (expandedId == contact.id) expandedId = null
                    removingContactId = null
                }) {
                    Text(stringResource(R.string.contacts_remove), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { removingContactId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun EmptyDirectoryCard(onAdd: () -> Unit) {
    SectionCard(title = stringResource(R.string.contacts_empty_title)) {
        Text(text = stringResource(R.string.contacts_empty_message), color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAdd,
            colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.contacts_add_first))
        }
    }
}

@Composable
private fun VerificationNotice() {
    Text(
        text = stringResource(R.string.contacts_verification_notice),
        color = Color.Gray,
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(NavyBlue, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.initials,
                        color = Color.White,
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
                                contentDescription = stringResource(R.string.verified_label),
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(text = PhoneNumbers.format(contact.phone), color = Color.Gray)
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
                        R.string.contacts_added_on,
                        DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(contact.addedAtMillis))
                    ),
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    TextButton(onClick = onRateExchange) {
                        Text(stringResource(R.string.contacts_rate_exchange))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onRemove) {
                        Text(stringResource(R.string.contacts_remove), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun reputationText(contact: TrustedContact): String {
    val average = contact.averageRating ?: return stringResource(R.string.contacts_no_exchanges)
    return stringResource(R.string.rating_value, average) + " · " +
        quantityString(R.plurals.contacts_exchange_count, contact.exchangeCount)
}

@Composable
private fun quantityString(id: Int, count: Int): String =
    LocalContext.current.resources.getQuantityString(id, count, count)

@Composable
private fun VerificationBadge(status: VerificationStatus) {
    val (label, color) = when (status) {
        VerificationStatus.VERIFIED -> stringResource(R.string.verified_label) to SuccessGreen
        VerificationStatus.PENDING -> stringResource(R.string.pending_label) to PendingAmber
        VerificationStatus.UNVERIFIED -> stringResource(R.string.unverified_label) to Color.Gray
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
        title = { Text(stringResource(R.string.contacts_add_title)) },
        text = {
            Column {
                AppTextField(
                    value = name,
                    onValueChange = { name = it; error = null },
                    label = stringResource(R.string.contacts_name_label)
                )
                AppTextField(
                    value = phone,
                    onValueChange = { phone = it; error = null },
                    label = stringResource(R.string.chinese_phone_label),
                    keyboardType = KeyboardType.Phone
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it; error = null },
                    label = { Text(stringResource(R.string.contacts_note_label)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
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
                Text(stringResource(R.string.contacts_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

private fun ContactError.messageRes(): Int = when (this) {
    ContactError.NAME_REQUIRED -> R.string.contacts_error_name_required
    ContactError.NAME_TOO_LONG -> R.string.contacts_error_name_too_long
    ContactError.INVALID_PHONE -> R.string.validation_chinese_phone_required
    ContactError.DUPLICATE_PHONE -> R.string.contacts_error_duplicate_phone
    ContactError.NOTE_TOO_LONG -> R.string.contacts_error_note_too_long
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
        title = { Text(stringResource(R.string.contacts_rate_title, contact.name)) },
        text = {
            Column {
                Text(stringResource(R.string.contacts_rate_message), color = Color.Gray)
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    for (star in TrustedContact.MIN_RATING..TrustedContact.MAX_RATING) {
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = stringResource(R.string.contacts_star_description, star),
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
                Text(stringResource(R.string.contacts_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
