package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.AdminPurple
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    viewModel: MarketplaceViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.allUsers.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Register

    // Sign In inputs
    var emailInput by remember { mutableStateOf(currentUser.email) }
    var passwordInput by remember { mutableStateOf("master123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Register inputs
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regRole by remember { mutableStateOf(UserRole.CUSTOMER) }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .heightIn(max = 680.dp)
            .testTag("login_dialog_container"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = PrimaryOrange.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = PrimaryOrange,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Account & Login",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "OmniFeast Marketplace Hub",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_login_dialog")) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Active Account Banner
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when (currentUser.role) {
                                UserRole.SUPER_ADMIN -> Color(0xFF311B92).copy(alpha = 0.12f)
                                UserRole.ADMIN_STAFF -> AdminPurple.copy(alpha = 0.12f)
                                UserRole.RESTAURANT_OWNER -> PrimaryOrange.copy(alpha = 0.12f)
                                UserRole.DELIVERY_PARTNER -> DeliveryBlue.copy(alpha = 0.12f)
                                else -> SecondaryTeal.copy(alpha = 0.12f)
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().testTag("active_user_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = when (currentUser.role) {
                                    UserRole.SUPER_ADMIN -> Color(0xFF311B92)
                                    UserRole.ADMIN_STAFF -> AdminPurple
                                    UserRole.RESTAURANT_OWNER -> PrimaryOrange
                                    UserRole.DELIVERY_PARTNER -> DeliveryBlue
                                    else -> SecondaryTeal
                                },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = currentUser.name.take(2).uppercase(),
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "CURRENTLY LOGGED IN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = when (currentUser.role) {
                                            UserRole.SUPER_ADMIN -> Color(0xFF311B92)
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    ) {
                                        Text(
                                            text = currentUser.role.displayName.uppercase(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = currentUser.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = currentUser.email,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Sign In / Register Tabs
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0; errorMessage = null },
                            text = { Text("Sign In", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_sign_in")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1; errorMessage = null },
                            text = { Text("Register", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("tab_register")
                        )
                    }
                }

                // Error or Success Banner
                if (errorMessage != null) {
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                if (successMessage != null) {
                    item {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = successMessage!!, color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // TAB 0: SIGN IN
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Instant One-Tap Login (Saved Personas):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Quick account selector list (Prioritize Master Owner mandalhabibullah476@gmail.com)
                            val sortedUsers = remember(allUsers) {
                                allUsers.sortedByDescending { it.email.equals("mandalhabibullah476@gmail.com", ignoreCase = true) || it.role == UserRole.SUPER_ADMIN }
                            }

                            sortedUsers.forEach { user ->
                                val isSelected = user.id == currentUser.id
                                val isMaster = user.email.equals("mandalhabibullah476@gmail.com", ignoreCase = true) || user.role == UserRole.SUPER_ADMIN
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            isMaster -> Color(0xFF162032)
                                            isSelected -> PrimaryOrange.copy(alpha = 0.12f)
                                            else -> MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    border = when {
                                        isMaster -> androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFFB703))
                                        isSelected -> androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryOrange)
                                        else -> androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("quick_user_${user.role.name.lowercase()}")
                                        .clickable {
                                            viewModel.quickLoginAs(user)
                                            emailInput = user.email
                                            passwordInput = user.passwordHash
                                            successMessage = "Logged in as ${user.name}"
                                            errorMessage = null
                                            onDismiss()
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                shape = CircleShape,
                                                color = when {
                                                    isMaster -> Color(0xFFFFB703)
                                                    user.role == UserRole.SUPER_ADMIN -> Color(0xFF311B92)
                                                    user.role == UserRole.ADMIN_STAFF -> AdminPurple
                                                    user.role == UserRole.RESTAURANT_OWNER -> PrimaryOrange
                                                    user.role == UserRole.DELIVERY_PARTNER -> DeliveryBlue
                                                    else -> SecondaryTeal
                                                },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Text(
                                                        text = if (isMaster) "👑" else user.name.take(1).uppercase(),
                                                        color = if (isMaster) Color.Black else Color.White,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = if (isMaster) 15.sp else 13.sp
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = user.name,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = if (isMaster) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (isMaster) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("★", color = Color(0xFFFFB703), fontSize = 12.sp)
                                                    }
                                                }
                                                Text(
                                                    text = user.email,
                                                    fontSize = 11.sp,
                                                    color = if (isMaster) Color(0xFFFFD166) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = when {
                                                isMaster -> Color(0xFFFFB703)
                                                user.role == UserRole.SUPER_ADMIN -> Color(0xFF311B92).copy(alpha = 0.15f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            }
                                        ) {
                                            Text(
                                                text = if (isMaster) "FULL ACCESS" else user.role.displayName,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = when {
                                                    isMaster -> Color.Black
                                                    user.role == UserRole.SUPER_ADMIN -> Color(0xFF311B92)
                                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Or Enter Login Credentials:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it; errorMessage = null },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMessage = null },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Primary Login Button
                        Button(
                            onClick = {
                                if (emailInput.isBlank()) {
                                    errorMessage = "Please enter your email"
                                    return@Button
                                }
                                viewModel.login(emailInput, passwordInput) { success, msg ->
                                    if (success) {
                                        successMessage = msg
                                        errorMessage = null
                                        onDismiss()
                                    } else {
                                        errorMessage = msg
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_login_button")
                        ) {
                            Icon(imageVector = Icons.Default.Login, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "LOG IN", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Logout / Switch Button
                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                                successMessage = "Logged out"
                                errorMessage = null
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("logout_button")
                        ) {
                            Icon(imageVector = Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "LOG OUT OF CURRENT ACCOUNT", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    // TAB 1: REGISTER
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                label = { Text("Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("register_name_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("register_phone_input"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                            OutlinedTextField(
                                value = regPassword,
                                onValueChange = { regPassword = it },
                                label = { Text("Create Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation()
                            )
                            OutlinedTextField(
                                value = regAddress,
                                onValueChange = { regAddress = it },
                                label = { Text("Delivery Address / Base City") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth().testTag("register_address_input"),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Select Account Role:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(UserRole.CUSTOMER, UserRole.RESTAURANT_OWNER, UserRole.DELIVERY_PARTNER).forEach { role ->
                                    FilterChip(
                                        selected = regRole == role,
                                        onClick = { regRole = role },
                                        label = { Text(role.displayName, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (regEmail.isBlank() || regName.isBlank()) {
                                        errorMessage = "Please enter name and email"
                                        return@Button
                                    }
                                    viewModel.registerNewUser(
                                        name = regName,
                                        email = regEmail,
                                        phone = regPhone,
                                        role = regRole,
                                        password = regPassword,
                                        address = regAddress
                                    ) { success, msg ->
                                        if (success) {
                                            successMessage = msg
                                            errorMessage = null
                                            selectedTab = 0
                                            onDismiss()
                                        } else {
                                            errorMessage = msg
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("register_button")
                            ) {
                                Text("CREATE ACCOUNT & LOG IN", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("login_dialog_done_button")
            ) {
                Text("DONE")
            }
        }
    )
}
