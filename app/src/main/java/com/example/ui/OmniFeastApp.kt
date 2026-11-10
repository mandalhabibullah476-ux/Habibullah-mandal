package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.admin.AdminDashboardScreen
import com.example.ui.auth.LoginDialog
import com.example.ui.components.RoleSwitcherBar
import com.example.ui.components.TopMarketplaceHeader
import com.example.ui.customer.AccountProfileScreen
import com.example.ui.customer.CartAndCheckoutScreen
import com.example.ui.customer.CustomerHomeScreen
import com.example.ui.customer.OrderTrackingScreen
import com.example.ui.customer.OrdersHistoryScreen
import com.example.ui.customer.SupportAndLegalScreen
import com.example.ui.delivery.DeliveryDashboardScreen
import com.example.ui.theme.AdminPurple
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.VegGreen
import com.example.ui.vendor.VendorDashboardScreen
import com.example.ui.viewmodel.MarketplaceViewModel
import kotlinx.coroutines.flow.collectLatest

enum class CustomerTab {
    HOME,
    ORDERS,
    SUPPORT,
    ACCOUNT
}

@Composable
fun OmniFeastApp(
    viewModel: MarketplaceViewModel = viewModel()
) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Customer Navigation state
    var customerTab by remember { mutableIntStateOf(0) }
    // Full screen overlay states
    var selectedRestaurantId by remember { mutableStateOf<Long?>(null) }
    var showCartScreen by remember { mutableStateOf(false) }
    var trackingOrderId by remember { mutableStateOf<Long?>(null) }

    // Dialogs
    var showLoginDialog by remember { mutableStateOf(false) }
    var showRoleModal by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showQuickActionsModal by remember { mutableStateOf(false) }
    var customAddressInput by remember { mutableStateOf(currentUser.address) }

    // Listen to UI messages
    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 768.dp

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (isWideScreen) {
                    OmniFeastWebTopBar(
                        currentRole = currentRole,
                        currentUserEmail = currentUser.email,
                        currentUserName = currentUser.name,
                        currentAddress = currentUser.address,
                        cartItemCount = cartItems.sumOf { it.quantity },
                        notificationCount = notifications.size,
                        activeCustomerTab = customerTab,
                        isCartOpen = showCartScreen,
                        selectedRestaurantId = selectedRestaurantId,
                        trackingOrderId = trackingOrderId,
                        onTabSelected = { tab: Int ->
                            customerTab = tab
                            selectedRestaurantId = null
                            showCartScreen = false
                            trackingOrderId = null
                            if (currentRole != UserRole.CUSTOMER) {
                                viewModel.switchRole(UserRole.CUSTOMER)
                            }
                        },
                        onOpenCart = {
                            showCartScreen = true
                            selectedRestaurantId = null
                            trackingOrderId = null
                            if (currentRole != UserRole.CUSTOMER) {
                                viewModel.switchRole(UserRole.CUSTOMER)
                            }
                        },
                        onRoleSelected = { role: UserRole ->
                            viewModel.switchRole(role)
                            selectedRestaurantId = null
                            showCartScreen = false
                            trackingOrderId = null
                        },
                        onAllRolesClick = { showRoleModal = true },
                        onLocationClick = { showLocationDialog = true },
                        onNotificationClick = { showNotificationsDialog = true },
                        onLoginClick = { showLoginDialog = true },
                        onLogoutClick = {
                            viewModel.logout()
                            showLoginDialog = true
                        }
                    )
                } else {
                    val showGlobalTopBar = selectedRestaurantId == null && !showCartScreen && trackingOrderId == null
                    if (showGlobalTopBar) {
                        Column {
                            // Top Global Header
                            TopMarketplaceHeader(
                                title = when (currentRole) {
                                    UserRole.CUSTOMER -> "Deliver to Home"
                                    UserRole.RESTAURANT_OWNER, UserRole.RESTAURANT_STAFF -> "Kitchen Portal"
                                    UserRole.DELIVERY_PARTNER -> "Rider Partner"
                                    UserRole.ADMIN_STAFF -> "Operations Admin"
                                    UserRole.SUPER_ADMIN -> "Master Platform Owner"
                                },
                                subtitle = when (currentRole) {
                                    UserRole.CUSTOMER -> currentUser.address
                                    UserRole.RESTAURANT_OWNER, UserRole.RESTAURANT_STAFF -> "OmniFeast Merchant Central"
                                    UserRole.DELIVERY_PARTNER -> "Electric Fleet Service"
                                    UserRole.ADMIN_STAFF -> "Global Control Centre"
                                    UserRole.SUPER_ADMIN -> "Habibullah Mandal (mandalhabibullah476@gmail.com)"
                                },
                                currentRole = currentRole,
                                notificationCount = notifications.size,
                                onRoleSwitchClick = { showRoleModal = true },
                                onNotificationClick = { showNotificationsDialog = true },
                                onLocationClick = { showLocationDialog = true },
                                onLoginClick = { showLoginDialog = true },
                                onLogoutClick = {
                                    viewModel.logout()
                                    showLoginDialog = true
                                }
                            )

                            // Quick Portal Switching Chips
                            RoleSwitcherBar(
                                currentRole = currentRole,
                                onRoleSelected = { role ->
                                    viewModel.switchRole(role)
                                    // Reset sub-screens
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                }
                            )
                        }
                    }
                }
            },
            floatingActionButton = {},
            bottomBar = {
                if (!isWideScreen && !showCartScreen && trackingOrderId == null) {
                val isAdminRole = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.ADMIN_STAFF
                NavigationBar(
                    containerColor = if (isAdminRole) Color(0xFF101726) else MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("app_bottom_navigation_bar")
                ) {
                    when (currentRole) {
                        UserRole.CUSTOMER -> {
                            // 1. Stores / Home
                            NavigationBarItem(
                                selected = customerTab == 0 && selectedRestaurantId == null && !showCartScreen,
                                onClick = {
                                    customerTab = 0
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.Storefront, contentDescription = "Home Stores") },
                                label = { Text("Stores", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryOrange,
                                    indicatorColor = PrimaryOrange.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("bottom_nav_stores")
                            )

                            // 2. Cart & Checkout (with live badge)
                            NavigationBarItem(
                                selected = showCartScreen,
                                onClick = {
                                    showCartScreen = true
                                    selectedRestaurantId = null
                                    trackingOrderId = null
                                },
                                icon = {
                                    if (cartItems.isNotEmpty()) {
                                        BadgedBox(
                                            badge = {
                                                Badge(containerColor = PrimaryOrange, contentColor = Color.White) {
                                                    Text("${cartItems.sumOf { it.quantity }}")
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "Cart")
                                        }
                                    } else {
                                        Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "Cart")
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (cartItems.isNotEmpty()) "Cart (${cartItems.sumOf { it.quantity }})" else "Cart",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryOrange,
                                    indicatorColor = PrimaryOrange.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("action_bottom_button")
                            )

                            // 3. Orders & Tracking
                            NavigationBarItem(
                                selected = customerTab == 1 && selectedRestaurantId == null && !showCartScreen,
                                onClick = {
                                    customerTab = 1
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.ReceiptLong, contentDescription = "My Orders") },
                                label = { Text("Orders", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryOrange,
                                    indicatorColor = PrimaryOrange.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("bottom_nav_orders")
                            )

                            // 4. Support & Help
                            NavigationBarItem(
                                selected = customerTab == 2 && selectedRestaurantId == null && !showCartScreen,
                                onClick = {
                                    customerTab = 2
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.SupportAgent, contentDescription = "Customer Support") },
                                label = { Text("Support", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryOrange,
                                    indicatorColor = PrimaryOrange.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("bottom_nav_support")
                            )

                            // 5. Account & Profile
                            NavigationBarItem(
                                selected = customerTab == 3 && selectedRestaurantId == null && !showCartScreen,
                                onClick = {
                                    customerTab = 3
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.AccountCircle, contentDescription = "My Account") },
                                label = { Text("Account", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryOrange,
                                    indicatorColor = PrimaryOrange.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("bottom_nav_account")
                            )

                            // 6. Direct Logout
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.logout()
                                    showLoginDialog = true
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error) },
                                label = { Text("Logout", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                colors = NavigationBarItemDefaults.colors(
                                    unselectedIconColor = MaterialTheme.colorScheme.error,
                                    unselectedTextColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("bottom_nav_logout")
                            )
                        }
                        UserRole.SUPER_ADMIN, UserRole.ADMIN_STAFF -> {
                            val isSuperAdmin = currentRole == UserRole.SUPER_ADMIN
                            NavigationBarItem(
                                selected = true,
                                onClick = {
                                    // Active admin dashboard view
                                },
                                icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Control") },
                                label = { Text(if (isSuperAdmin) "Master" else "Admin", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = if (isSuperAdmin) Color(0xFFFFB703) else AdminPurple,
                                    selectedTextColor = if (isSuperAdmin) Color(0xFFFFD166) else AdminPurple,
                                    indicatorColor = (if (isSuperAdmin) Color(0xFFFFB703) else AdminPurple).copy(alpha = 0.2f),
                                    unselectedIconColor = Color(0xFF94A3B8),
                                    unselectedTextColor = Color(0xFF94A3B8)
                                ),
                                modifier = Modifier.testTag("bottom_nav_admin")
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.CUSTOMER)
                                    customerTab = 0
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.Storefront, contentDescription = "Stores") },
                                label = { Text("Stores", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("bottom_nav_switch_stores")
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.RESTAURANT_OWNER)
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.Restaurant, contentDescription = "Kitchen") },
                                label = { Text("Kitchen", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("bottom_nav_switch_kitchen")
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.DELIVERY_PARTNER)
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = "Rider") },
                                label = { Text("Rider", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("bottom_nav_switch_rider")
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { showRoleModal = true },
                                icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Portals") },
                                label = { Text("Portals", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                modifier = Modifier.testTag("bottom_nav_switch_role")
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.logout()
                                    showLoginDialog = true
                                },
                                icon = { Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error) },
                                label = { Text("Logout", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                colors = NavigationBarItemDefaults.colors(
                                    unselectedIconColor = MaterialTheme.colorScheme.error,
                                    unselectedTextColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("bottom_nav_admin_logout")
                            )
                        }
                        UserRole.RESTAURANT_OWNER, UserRole.RESTAURANT_STAFF -> {
                            NavigationBarItem(
                                selected = true,
                                onClick = { },
                                icon = { Icon(imageVector = Icons.Default.Restaurant, contentDescription = "Kitchen") },
                                label = { Text("Kitchen", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = PrimaryOrange, indicatorColor = PrimaryOrange.copy(alpha = 0.15f)),
                                modifier = Modifier.testTag("bottom_nav_kitchen")
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.CUSTOMER)
                                    customerTab = 0
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.Storefront, contentDescription = "Stores") },
                                label = { Text("Stores", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.SUPER_ADMIN)
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                label = { Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.DELIVERY_PARTNER)
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = "Rider") },
                                label = { Text("Rider", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { showRoleModal = true },
                                icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Portals") },
                                label = { Text("Portals", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.logout()
                                    showLoginDialog = true
                                },
                                icon = { Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error) },
                                label = { Text("Logout", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                colors = NavigationBarItemDefaults.colors(
                                    unselectedIconColor = MaterialTheme.colorScheme.error,
                                    unselectedTextColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("bottom_nav_kitchen_logout")
                            )
                        }
                        UserRole.DELIVERY_PARTNER -> {
                            NavigationBarItem(
                                selected = true,
                                onClick = { },
                                icon = { Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = "Rider") },
                                label = { Text("Rider", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                colors = NavigationBarItemDefaults.colors(selectedIconColor = DeliveryBlue, indicatorColor = DeliveryBlue.copy(alpha = 0.15f)),
                                modifier = Modifier.testTag("bottom_nav_rider")
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.CUSTOMER)
                                    customerTab = 0
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.Storefront, contentDescription = "Stores") },
                                label = { Text("Stores", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.RESTAURANT_OWNER)
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.Restaurant, contentDescription = "Kitchen") },
                                label = { Text("Kitchen", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.switchRole(UserRole.SUPER_ADMIN)
                                    selectedRestaurantId = null
                                    showCartScreen = false
                                    trackingOrderId = null
                                },
                                icon = { Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                label = { Text("Admin", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { showRoleModal = true },
                                icon = { Icon(imageVector = Icons.Default.Person, contentDescription = "Portals") },
                                label = { Text("Portals", fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = {
                                    viewModel.logout()
                                    showLoginDialog = true
                                },
                                icon = { Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error) },
                                label = { Text("Logout", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) },
                                colors = NavigationBarItemDefaults.colors(
                                    unselectedIconColor = MaterialTheme.colorScheme.error,
                                    unselectedTextColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.testTag("bottom_nav_rider_logout")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = if (isWideScreen) Alignment.TopCenter else Alignment.TopStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isWideScreen) Modifier.widthIn(max = 1280.dp) else Modifier)
            ) {
                when (currentRole) {
                UserRole.CUSTOMER -> {
                    when {
                        trackingOrderId != null -> {
                            OrderTrackingScreen(
                                orderId = trackingOrderId!!,
                                viewModel = viewModel,
                                onBackClick = { trackingOrderId = null },
                                onContactSupportClick = { orderId ->
                                    trackingOrderId = null
                                    customerTab = 2
                                }
                            )
                        }
                        showCartScreen -> {
                            CartAndCheckoutScreen(
                                viewModel = viewModel,
                                onBackClick = { showCartScreen = false },
                                onOrderPlaced = { orderId ->
                                    showCartScreen = false
                                    selectedRestaurantId = null
                                    trackingOrderId = orderId
                                }
                            )
                        }
                        selectedRestaurantId != null -> {
                            com.example.ui.customer.RestaurantDetailScreen(
                                restaurantId = selectedRestaurantId!!,
                                viewModel = viewModel,
                                onBackClick = { selectedRestaurantId = null },
                                onViewCartClick = { showCartScreen = true }
                            )
                        }
                        customerTab == 0 -> {
                            CustomerHomeScreen(
                                viewModel = viewModel,
                                onRestaurantClick = { id -> selectedRestaurantId = id },
                                onViewCartClick = { showCartScreen = true },
                                onTrackOrderClick = { id -> trackingOrderId = id },
                                onLoginClick = { showLoginDialog = true },
                                onLogoutClick = {
                                    viewModel.logout()
                                    showLoginDialog = true
                                }
                            )
                        }
                        customerTab == 1 -> {
                            OrdersHistoryScreen(
                                viewModel = viewModel,
                                onTrackOrder = { id -> trackingOrderId = id }
                            )
                        }
                        customerTab == 2 -> {
                            SupportAndLegalScreen(
                                viewModel = viewModel
                            )
                        }
                        customerTab == 3 -> {
                            AccountProfileScreen(
                                viewModel = viewModel,
                                onOpenLoginDialog = { showLoginDialog = true },
                                onViewOrdersClick = { customerTab = 1 },
                                onSupportClick = { customerTab = 2 }
                            )
                        }
                    }
                }

                UserRole.RESTAURANT_OWNER, UserRole.RESTAURANT_STAFF -> {
                    VendorDashboardScreen(viewModel = viewModel)
                }

                UserRole.DELIVERY_PARTNER -> {
                    DeliveryDashboardScreen(viewModel = viewModel)
                }

                UserRole.ADMIN_STAFF, UserRole.SUPER_ADMIN -> {
                    AdminDashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

        // Role Switcher Modal Dialog
        if (showRoleModal) {
            AlertDialog(
                onDismissRequest = { showRoleModal = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Switch Platform Role")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "OmniFeast is a complete 6-role marketplace platform. Select a persona to test the respective real-time interfaces:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        UserRole.values().forEach { role ->
                            val isCurrent = role == currentRole
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.switchRole(role)
                                        showRoleModal = false
                                        selectedRestaurantId = null
                                        showCartScreen = false
                                        trackingOrderId = null
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isCurrent) PrimaryOrange.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(role.displayName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = when (role) {
                                                UserRole.CUSTOMER -> "Browse foods, grocery mart, cart, live GPS tracking"
                                                UserRole.RESTAURANT_OWNER -> "Live kitchen tickets, menu catalog, financial payouts"
                                                UserRole.RESTAURANT_STAFF -> "Prep food, mark items ready for rider pickup"
                                                UserRole.DELIVERY_PARTNER -> "Duty toggle, accept trips, verify customer OTP"
                                                UserRole.ADMIN_STAFF -> "Review store KYC, driver verification, disputes"
                                                UserRole.SUPER_ADMIN -> "Platform GMV, commission % setup, full audit logs"
                                            },
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isCurrent) {
                                        Surface(
                                            color = PrimaryOrange,
                                            shape = CircleShape,
                                            modifier = Modifier.size(10.dp)
                                        ) {}
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRoleModal = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Notifications Dialog
        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = PrimaryOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Notifications (${notifications.size})")
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (notifications.isEmpty()) {
                            Text("No new notifications.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            notifications.forEach { n ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(n.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(n.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showNotificationsDialog = false }) { Text("Close") }
                }
            )
        }

        // Change Address Dialog
        if (showLocationDialog) {
            AlertDialog(
                onDismissRequest = { showLocationDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PrimaryOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delivery Location")
                    }
                },
                text = {
                    Column {
                        Text("Set your live delivery destination:", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customAddressInput,
                            onValueChange = { customAddressInput = it },
                            label = { Text("Street Address, Sector, City") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (customAddressInput.isNotBlank()) {
                                viewModel.updateUserAddress(customAddressInput)
                            }
                            showLocationDialog = false
                        }
                    ) {
                        Text("Update Location")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLocationDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Login & Account Dialog
        if (showLoginDialog) {
            LoginDialog(
                viewModel = viewModel,
                onDismiss = { showLoginDialog = false }
            )
        }

        // Quick Actions Bottom Dialog
        if (showQuickActionsModal) {
            AlertDialog(
                onDismissRequest = { showQuickActionsModal = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FlashOn, contentDescription = null, tint = PrimaryOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Action Hub", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Instant shortcuts and active controls:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // 1. Cart Action
                        if (cartItems.isNotEmpty()) {
                            Button(
                                onClick = {
                                    showQuickActionsModal = false
                                    showCartScreen = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_action_view_cart")
                            ) {
                                Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View Cart & Checkout (${cartItems.sumOf { it.quantity }} items)")
                            }
                        }

                        // 2. Active User Profile & Login
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Logged-In User", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(currentUser.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(currentUser.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            showQuickActionsModal = false
                                            showLoginDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("quick_action_login")
                                    ) {
                                        Text("LOGIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            viewModel.logout()
                                            showQuickActionsModal = false
                                            showLoginDialog = true
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("quick_action_logout")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Logout,
                                            contentDescription = "Logout",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("LOGOUT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        // 3. Switch Portals
                        Text("Switch Active Portal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    viewModel.switchRole(UserRole.CUSTOMER)
                                    customerTab = 0
                                    showQuickActionsModal = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentRole == UserRole.CUSTOMER) PrimaryOrange else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(
                                    "Customer",
                                    fontSize = 11.sp,
                                    color = if (currentRole == UserRole.CUSTOMER) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.switchRole(UserRole.SUPER_ADMIN)
                                    showQuickActionsModal = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentRole == UserRole.SUPER_ADMIN) Color(0xFF311B92) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(4.dp)
                            ) {
                                Text(
                                    "Admin",
                                    fontSize = 11.sp,
                                    color = if (currentRole == UserRole.SUPER_ADMIN) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // 4. Role Modal Trigger
                        OutlinedButton(
                            onClick = {
                                showQuickActionsModal = false
                                showRoleModal = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_action_all_roles")
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = PrimaryOrange)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("All 6 Marketplace Portals", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                        }

                        // 5. Contact Support
                        TextButton(
                            onClick = {
                                showQuickActionsModal = false
                                viewModel.switchRole(UserRole.CUSTOMER)
                                customerTab = 2
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.SupportAgent, contentDescription = null, tint = SecondaryTeal)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Customer Support & Helpdesk", color = SecondaryTeal)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQuickActionsModal = false }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
}

@Composable
fun OmniFeastWebTopBar(
    currentRole: UserRole,
    currentUserEmail: String,
    currentUserName: String,
    currentAddress: String,
    cartItemCount: Int,
    notificationCount: Int,
    activeCustomerTab: Int,
    isCartOpen: Boolean,
    selectedRestaurantId: Long?,
    trackingOrderId: Long?,
    onTabSelected: (Int) -> Unit,
    onOpenCart: () -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    onAllRolesClick: () -> Unit,
    onLocationClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val isAdminRole = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.ADMIN_STAFF
    val headerBg = if (isAdminRole) Color(0xFF101726) else MaterialTheme.colorScheme.surface
    val onHeaderColor = if (isAdminRole) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.onSurface

    Surface(
        color = headerBg,
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("web_top_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 1. Brand Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onTabSelected(0) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = "OmniFeast Logo",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "OmniFeast",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = if (isAdminRole) Color(0xFFFFB703) else PrimaryOrange
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PrimaryOrange.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "WEB",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryOrange,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Food & Grocery Marketplace",
                            fontSize = 10.sp,
                            color = if (isAdminRole) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 2. Deliver To Address Pill
                Surface(
                    onClick = onLocationClick,
                    shape = RoundedCornerShape(24.dp),
                    color = if (isAdminRole) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = PrimaryOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Deliver to: $currentAddress",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = onHeaderColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Location",
                            tint = onHeaderColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // 3. Web Navigation Links (Stores, Cart, Orders, Support, Profile)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Stores Link
                    WebNavLink(
                        label = "Restaurants",
                        icon = Icons.Default.Storefront,
                        isSelected = activeCustomerTab == 0 && !isCartOpen && selectedRestaurantId == null,
                        onClick = { onTabSelected(0) }
                    )

                    // Cart Link (with live count badge)
                    Surface(
                        onClick = onOpenCart,
                        shape = RoundedCornerShape(20.dp),
                        color = if (isCartOpen) PrimaryOrange.copy(alpha = 0.15f) else Color.Transparent,
                        modifier = Modifier.testTag("web_nav_cart")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (cartItemCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(containerColor = PrimaryOrange, contentColor = Color.White) {
                                            Text("$cartItemCount")
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingBag,
                                        contentDescription = "Cart",
                                        tint = if (isCartOpen) PrimaryOrange else onHeaderColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "Cart",
                                    tint = if (isCartOpen) PrimaryOrange else onHeaderColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cart",
                                fontSize = 12.sp,
                                fontWeight = if (isCartOpen) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCartOpen) PrimaryOrange else onHeaderColor
                            )
                        }
                    }

                    // Orders Link
                    WebNavLink(
                        label = "Orders",
                        icon = Icons.Default.ReceiptLong,
                        isSelected = activeCustomerTab == 1 && !isCartOpen,
                        onClick = { onTabSelected(1) }
                    )

                    // Support Link
                    WebNavLink(
                        label = "Support",
                        icon = Icons.Default.SupportAgent,
                        isSelected = activeCustomerTab == 2 && !isCartOpen,
                        onClick = { onTabSelected(2) }
                    )

                    // Profile Link
                    WebNavLink(
                        label = "Account",
                        icon = Icons.Default.Person,
                        isSelected = activeCustomerTab == 3 && !isCartOpen,
                        onClick = { onTabSelected(3) }
                    )
                }

                // 4. Portal Selector & User Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Portal Indicator / Switch Button
                    Surface(
                        onClick = onAllRolesClick,
                        shape = RoundedCornerShape(20.dp),
                        color = when (currentRole) {
                            UserRole.CUSTOMER -> SecondaryTeal.copy(alpha = 0.15f)
                            UserRole.RESTAURANT_OWNER, UserRole.RESTAURANT_STAFF -> PrimaryOrange.copy(alpha = 0.15f)
                            UserRole.DELIVERY_PARTNER -> DeliveryBlue.copy(alpha = 0.15f)
                            UserRole.ADMIN_STAFF, UserRole.SUPER_ADMIN -> Color(0xFFFFB703).copy(alpha = 0.2f)
                        },
                        border = BorderStroke(1.dp, PrimaryOrange.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("role_switch_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (currentRole == UserRole.SUPER_ADMIN) "👑 ${currentRole.displayName}" else currentRole.displayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdminRole) Color(0xFFFFD166) else PrimaryOrange
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Switch Portal",
                                tint = if (isAdminRole) Color(0xFFFFD166) else PrimaryOrange,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Notification Icon with Badge
                    Box {
                        IconButton(
                            onClick = onNotificationClick,
                            modifier = Modifier.testTag("notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = onHeaderColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (notificationCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (notificationCount > 9) "9+" else "$notificationCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Prominent Login Button
                    Button(
                        onClick = onLoginClick,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("login_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Login",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LOGIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    // Prominent Logout Button
                    OutlinedButton(
                        onClick = onLogoutClick,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LOGOUT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Quick Role Switcher bar below top row for instant 1-click access
            RoleSwitcherBar(
                currentRole = currentRole,
                onRoleSelected = onRoleSelected
            )
        }
    }
}

@Composable
private fun WebNavLink(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryOrange.copy(alpha = 0.15f) else Color.Transparent,
        modifier = Modifier.testTag("web_nav_${label.lowercase()}")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) PrimaryOrange else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) PrimaryOrange else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

