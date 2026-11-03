package com.eriktemiraliev.asmanpay.screens

import org.jetbrains.compose.resources.stringResource
import com.eriktemiraliev.asmanpay.resources.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    user: UserAccount,
    onUserUpdated: (UserAccount) -> Unit
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var phoneNumber by remember { mutableStateOf(user.phoneNumber) }
    var email by remember { mutableStateOf(user.email) }
    var newPassword by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var avatarColorIndex by remember { mutableIntStateOf(0) }

    val profileUpdatedText = stringResource(Res.string.profile_updated)

    val avatarColors = listOf(
        NavyBlue,
        Color(0xFF7C5CFF),
        Color(0xFF00A88F),
        Color(0xFFFF7A59),
        Color(0xFFE0488B)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
            .padding(20.dp)
    ) {
        item {
            Header(
                title = stringResource(Res.string.profile_title),
                subtitle = stringResource(Res.string.account_settings)
            )

            GradientCard {
                Column {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ProfileAvatar(
                            fullName = user.fullName,
                            backgroundColor = avatarColors[avatarColorIndex]
                        )

                        Spacer(modifier = Modifier.width(18.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.fullName,
                                color = Color.White,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = stringResource(Res.string.verified_account_text),
                                color = Color.White.copy(alpha = 0.82f),
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            avatarColorIndex = (avatarColorIndex + 1) % avatarColors.size
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = BrightBlue
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.change_picture),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            SectionCard(title = stringResource(Res.string.account_identity)) {
                Text(
                    text = stringResource(Res.string.passport_number_value, user.passportNumber),
                    color = NavyBlue,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = stringResource(
                        Res.string.verified_status_value,
                        if (user.verified) stringResource(Res.string.verified_label)
                        else stringResource(Res.string.pending_label)
                    ),
                    color = SuccessGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = stringResource(Res.string.contact_information)) {
                AppTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = stringResource(Res.string.full_real_name_label)
                )

                AppTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = stringResource(Res.string.chinese_phone_label),
                    keyboardType = KeyboardType.Phone
                )

                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = stringResource(Res.string.email_address_label),
                    keyboardType = KeyboardType.Email
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard(title = stringResource(Res.string.security)) {
                AppTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = stringResource(Res.string.new_password_label),
                    keyboardType = KeyboardType.Password,
                    isPassword = true
                )
            }

            if (message.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = message,
                    color = SuccessGreen,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val updatedPassword = newPassword.ifBlank {
                        user.password
                    }

                    onUserUpdated(
                        user.copy(
                            fullName = fullName,
                            phoneNumber = phoneNumber,
                            email = email,
                            password = updatedPassword
                        )
                    )

                    newPassword = ""
                    message = profileUpdatedText
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrightBlue),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = stringResource(Res.string.save_changes),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ProfileAvatar(
    fullName: String,
    backgroundColor: Color
) {
    val initials = fullName
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Box(
        modifier = Modifier
            .size(70.dp)
            .background(backgroundColor, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifBlank { "AP" },
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
