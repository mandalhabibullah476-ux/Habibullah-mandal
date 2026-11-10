package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AdminLogEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.CouponEntity
import com.example.data.local.DeliveryPartnerEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.NotificationEntity
import com.example.data.local.OrderEntity
import com.example.data.local.OrderItemEntity
import com.example.data.local.PlatformSettingsEntity
import com.example.data.local.RestaurantEntity
import com.example.data.local.SettlementEntity
import com.example.data.local.SupportTicketEntity
import com.example.data.local.UserEntity
import com.example.data.model.DeliveryPartnerStatus
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.StoreType
import com.example.data.model.TicketStatus
import com.example.data.model.UserRole
import com.example.data.model.VendorStatus
import com.example.data.repository.MarketplaceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = MarketplaceRepository(db)

    // Current Active Role for testing and using the marketplace (Default Master Platform Owner)
    private val _currentRole = MutableStateFlow(UserRole.SUPER_ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    // Default Guest user representation when logged out
    val guestUser = UserEntity(
        id = 0,
        name = "Guest User",
        email = "guest@omnifeast.com",
        phone = "+1 (555) 000-0000",
        role = UserRole.CUSTOMER,
        passwordHash = "",
        address = "Select delivery location",
        latitude = 12.9716,
        longitude = 77.5946
    )

    // Logged-in state
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // Current Logged-in Master Owner User
    private val _currentUser = MutableStateFlow(
        UserEntity(
            id = 7,
            name = "Habibullah Mandal (Master Owner)",
            email = "mandalhabibullah476@gmail.com",
            phone = "+91 98765 43210",
            role = UserRole.SUPER_ADMIN,
            passwordHash = "master123",
            address = "OmniFeast Global HQ, Tower A (Master Owner Office)",
            latitude = 12.9716,
            longitude = 77.5946
        )
    )
    val currentUser: StateFlow<UserEntity> = _currentUser.asStateFlow()

    // UI Feedback Event
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    // Customer filters
    val selectedStoreType = MutableStateFlow<StoreType?>(null) // null = all, or Food / Grocery
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val vegOnlyFilter = MutableStateFlow(false)

    // Applied Coupon
    val appliedCoupon = MutableStateFlow<CouponEntity?>(null)

    // Selected Restaurant for Detail Screen
    val selectedRestaurantId = MutableStateFlow<Long?>(null)
    // Selected Order for Live Tracking
    val activeTrackingOrderId = MutableStateFlow<Long?>(null)

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
            val users = repository.allUsers.first()
            val masterUser = users.find { it.email.equals("mandalhabibullah476@gmail.com", ignoreCase = true) }
            if (masterUser != null) {
                _currentUser.value = masterUser
            }
        }
    }

    // Database flows
    val allRestaurants: StateFlow<List<RestaurantEntity>> = repository.allRestaurants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedRestaurants: StateFlow<List<RestaurantEntity>> = repository.approvedRestaurants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMenuItems: StateFlow<List<MenuItemEntity>> = repository.allMenuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCoupons: StateFlow<List<CouponEntity>> = repository.allCoupons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCoupons: StateFlow<List<CouponEntity>> = repository.activeCoupons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customerOrders: StateFlow<List<OrderEntity>> = repository.getOrdersForCustomer(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDeliveryPartners: StateFlow<List<DeliveryPartnerEntity>> = repository.allDeliveryPartners
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSupportTickets: StateFlow<List<SupportTicketEntity>> = repository.allSupportTickets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val platformSettings: StateFlow<PlatformSettingsEntity?> = repository.platformSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adminLogs: StateFlow<List<AdminLogEntity>> = repository.adminLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSettlements: StateFlow<List<SettlementEntity>> = repository.allSettlements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItemEntity>> = repository.getCartItems(1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotifications(UserRole.CUSTOMER, 1)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Restaurants for Customer Browse
    val filteredRestaurants: StateFlow<List<RestaurantEntity>> = combine(
        approvedRestaurants,
        selectedStoreType,
        searchQuery,
        selectedCategory,
        vegOnlyFilter
    ) { list, storeType, query, cat, vegOnly ->
        list.filter { r ->
            val matchType = storeType == null || r.storeType == storeType
            val matchQuery = query.isBlank() || r.name.contains(query, ignoreCase = true) || r.cuisineOrCategory.contains(query, ignoreCase = true)
            val matchCat = cat == "All" || r.cuisineOrCategory.contains(cat, ignoreCase = true)
            matchType && matchQuery && matchCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart calculations
    val cartSubtotal: StateFlow<Double> = cartItems.combine(allRestaurants) { items, _ ->
        items.sumOf { it.price * it.quantity }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        _isLoggedIn.value = true
        viewModelScope.launch {
            val users = repository.allUsers.first()
            val masterUser = users.find { it.email.equals("mandalhabibullah476@gmail.com", ignoreCase = true) }
            if (role == UserRole.SUPER_ADMIN && masterUser != null) {
                _currentUser.value = masterUser
            } else {
                val matchedUser = users.find { it.role == role }
                if (matchedUser != null) {
                    _currentUser.value = matchedUser
                } else {
                    _currentUser.value = _currentUser.value.copy(
                        name = when (role) {
                            UserRole.CUSTOMER -> "Alex Morgan (Customer)"
                            UserRole.RESTAURANT_OWNER -> "Chef Marco Rossi (Vendor)"
                            UserRole.RESTAURANT_STAFF -> "Staff Kitchen Operator"
                            UserRole.DELIVERY_PARTNER -> "Carlos Vance (Driver)"
                            UserRole.ADMIN_STAFF -> "Admin Operations"
                            UserRole.SUPER_ADMIN -> "Habibullah Mandal (Master Owner)"
                        },
                        role = role
                    )
                }
            }
            _uiEvent.emit("Switched to ${role.displayName} Portal")
        }
    }

    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            if (trimmedEmail.isBlank()) {
                val errorMsg = "Please enter an email address"
                onResult(false, errorMsg)
                _uiEvent.emit(errorMsg)
                return@launch
            }

            val users = repository.allUsers.first()

            // Special unrestricted Master Owner access verification
            if (trimmedEmail.equals("mandalhabibullah476@gmail.com", ignoreCase = true)) {
                val masterUser = users.find { it.email.equals("mandalhabibullah476@gmail.com", ignoreCase = true) }
                    ?: UserEntity(
                        id = 7,
                        name = "Habibullah Mandal (Master Owner)",
                        email = "mandalhabibullah476@gmail.com",
                        phone = "+91 98765 43210",
                        role = UserRole.SUPER_ADMIN,
                        passwordHash = "master123",
                        address = "OmniFeast Global HQ, Tower A (Master Owner Office)",
                        latitude = 12.9716,
                        longitude = 77.5946
                    )
                val fullAccessUser = masterUser.copy(role = UserRole.SUPER_ADMIN, name = "Habibullah Mandal (Master Owner)")
                _currentUser.value = fullAccessUser
                _currentRole.value = UserRole.SUPER_ADMIN
                _isLoggedIn.value = true
                val successMsg = "Full Access Granted: Welcome Habibullah Mandal (Master Platform Owner)!"
                onResult(true, successMsg)
                _uiEvent.emit(successMsg)
                return@launch
            }

            val user = repository.getUserByEmail(trimmedEmail)
                ?: users.find { it.email.equals(trimmedEmail, ignoreCase = true) }

            if (user != null) {
                // If password matches or is standard demo password or blank, log in
                if (user.passwordHash.isNotEmpty() && password.isNotBlank() && user.passwordHash != password && password != "demo123" && password != "master123") {
                    val errorMsg = "Incorrect password entered"
                    onResult(false, errorMsg)
                    _uiEvent.emit(errorMsg)
                    return@launch
                }

                _currentUser.value = user
                _currentRole.value = user.role
                _isLoggedIn.value = true
                val successMsg = "Welcome back, ${user.name}!"
                onResult(true, successMsg)
                _uiEvent.emit(successMsg)
            } else {
                // New user - auto create account so login never gets stuck
                val defaultName = trimmedEmail.substringBefore("@")
                    .split(".", "_", "-")
                    .joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
                    .ifBlank { "OmniFeast Customer" }
                val newEntity = UserEntity(
                    name = defaultName,
                    email = trimmedEmail,
                    phone = "+1 (555) 123-4567",
                    role = UserRole.CUSTOMER,
                    passwordHash = if (password.isNotBlank()) password else "demo123",
                    address = "Sector 4, Metro City",
                    latitude = 12.9716,
                    longitude = 77.5946
                )
                val newId = repository.registerUser(newEntity)
                val registeredUser = newEntity.copy(id = newId)
                _currentUser.value = registeredUser
                _currentRole.value = UserRole.CUSTOMER
                _isLoggedIn.value = true
                val successMsg = "Welcome to OmniFeast, ${registeredUser.name}!"
                onResult(true, successMsg)
                _uiEvent.emit(successMsg)
            }
        }
    }

    fun quickLoginAs(user: UserEntity) {
        viewModelScope.launch {
            _currentUser.value = user
            _currentRole.value = user.role
            _isLoggedIn.value = true
            _uiEvent.emit("Logged in as ${user.name} (${user.role.displayName})")
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoggedIn.value = false
            _currentUser.value = guestUser
            _currentRole.value = UserRole.CUSTOMER
            _uiEvent.emit("Logged out successfully. You are now browsing as Guest.")
        }
    }

    fun emitMessage(msg: String) {
        viewModelScope.launch {
            _uiEvent.emit(msg)
        }
    }

    fun registerNewUser(
        name: String,
        email: String,
        phone: String,
        role: UserRole,
        password: String,
        address: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmedEmail = email.trim()
            val existing = repository.getUserByEmail(trimmedEmail)
            if (existing != null) {
                onResult(false, "An account with this email already exists")
                return@launch
            }
            val isMasterOwner = trimmedEmail.equals("mandalhabibullah476@gmail.com", ignoreCase = true)
            val assignedRole = if (isMasterOwner) UserRole.SUPER_ADMIN else role
            val assignedName = if (isMasterOwner) name.trim().ifEmpty { "Habibullah Mandal (Master Owner)" } else name.trim().ifEmpty { "New User" }
            val newUser = UserEntity(
                name = assignedName,
                email = trimmedEmail,
                phone = phone.trim().ifEmpty { "+91 99999 00000" },
                role = assignedRole,
                passwordHash = password.trim().ifEmpty { "master123" },
                address = address.trim().ifEmpty { "Flat 204, Downtown, Metro City" },
                latitude = 12.9716,
                longitude = 77.5946
            )
            val newId = repository.registerUser(newUser)
            val createdUser = newUser.copy(id = newId)
            _currentUser.value = createdUser
            _currentRole.value = createdUser.role
            onResult(true, "Account created successfully!")
            _uiEvent.emit("Welcome to OmniFeast, ${createdUser.name}!")
        }
    }

    fun addToCart(
        restaurantId: Long,
        menuItemId: Long,
        name: String,
        price: Double,
        isVeg: Boolean,
        variation: String = "",
        addons: String = ""
    ) {
        viewModelScope.launch {
            repository.addToCart(
                userId = _currentUser.value.id,
                restaurantId = restaurantId,
                menuItemId = menuItemId,
                name = name,
                price = price,
                isVeg = isVeg,
                variation = variation,
                addons = addons
            )
            _uiEvent.emit("Added $name to cart")
        }
    }

    fun updateCartQuantity(cartItemId: Long, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, newQuantity)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart(_currentUser.value.id)
            appliedCoupon.value = null
            _uiEvent.emit("Cart cleared")
        }
    }

    fun applyCoupon(coupon: CouponEntity, subtotal: Double) {
        if (subtotal < coupon.minOrderAmount) {
            viewModelScope.launch {
                _uiEvent.emit("Minimum order of ₹${coupon.minOrderAmount.toInt()} required for this coupon")
            }
            return
        }
        appliedCoupon.value = coupon
        viewModelScope.launch {
            _uiEvent.emit("Coupon '${coupon.code}' applied successfully!")
        }
    }

    fun removeCoupon() {
        appliedCoupon.value = null
    }

    fun checkout(
        restaurant: RestaurantEntity,
        deliveryFee: Double,
        taxes: Double,
        platformFee: Double,
        discount: Double,
        total: Double,
        paymentMethod: PaymentMethod,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) {
                _uiEvent.emit("Cart is empty")
                return@launch
            }
            val orderId = repository.placeOrder(
                customerId = _currentUser.value.id,
                customerName = _currentUser.value.name,
                customerPhone = _currentUser.value.phone,
                customerAddress = _currentUser.value.address,
                customerLat = _currentUser.value.latitude,
                customerLng = _currentUser.value.longitude,
                restaurant = restaurant,
                cartItems = items,
                itemTotal = cartSubtotal.value,
                deliveryFee = deliveryFee,
                taxes = taxes,
                platformFee = platformFee,
                discountAmount = discount,
                couponCode = appliedCoupon.value?.code,
                totalAmount = total,
                paymentMethod = paymentMethod
            )
            appliedCoupon.value = null
            activeTrackingOrderId.value = orderId
            _uiEvent.emit("Order Placed Successfully! Tracking order...")
            onSuccess(orderId)
        }
    }

    fun updateOrderStatus(orderId: Long, status: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, _currentUser.value.name)
            _uiEvent.emit("Order status updated: ${status.label}")
        }
    }

    fun assignDriverToOrder(orderId: Long, driver: DeliveryPartnerEntity) {
        viewModelScope.launch {
            repository.assignDriverToOrder(orderId, driver)
            _uiEvent.emit("${driver.name} assigned to order")
        }
    }

    fun verifyDeliveryOtp(orderId: Long, otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val success = repository.verifyAndCompleteDelivery(orderId, otp)
            if (success) {
                _uiEvent.emit("OTP Verified! Order Marked Delivered 🎉")
                onSuccess()
            } else {
                _uiEvent.emit("Invalid Delivery OTP. Please verify with customer.")
            }
        }
    }

    fun cancelOrder(orderId: Long, reason: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId, reason, _currentUser.value.name)
            _uiEvent.emit("Order cancelled: $reason")
        }
    }

    fun requestRefund(orderId: Long, reason: String) {
        viewModelScope.launch {
            repository.requestRefund(orderId, reason, _currentUser.value.name)
            _uiEvent.emit("Refund request submitted for review")
        }
    }

    fun processRefund(orderId: Long) {
        viewModelScope.launch {
            repository.processRefund(orderId, _currentUser.value.name)
            _uiEvent.emit("Refund marked completed")
        }
    }

    // Vendor actions
    fun toggleRestaurantOpen(restaurantId: Long, isOpen: Boolean) {
        viewModelScope.launch {
            repository.toggleRestaurantOpen(restaurantId, isOpen)
            _uiEvent.emit(if (isOpen) "Restaurant marked OPEN" else "Restaurant marked CLOSED")
        }
    }

    fun addMenuItem(
        restaurantId: Long,
        category: String,
        name: String,
        description: String,
        price: Double,
        discountPrice: Double?,
        isVeg: Boolean,
        variations: String,
        addons: String
    ) {
        viewModelScope.launch {
            repository.addMenuItem(
                restaurantId = restaurantId,
                categoryName = category,
                name = name,
                description = description,
                price = price,
                discountPrice = discountPrice,
                isVeg = isVeg,
                variations = variations,
                addons = addons
            )
            _uiEvent.emit("Added item '$name' to menu")
        }
    }

    fun updateMenuItemAvailability(id: Long, isAvailable: Boolean) {
        viewModelScope.launch {
            repository.updateMenuItemAvailability(id, isAvailable)
            _uiEvent.emit(if (isAvailable) "Item marked in stock" else "Item marked sold out")
        }
    }

    fun deleteMenuItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMenuItem(id)
            _uiEvent.emit("Item removed from menu")
        }
    }

    fun registerNewRestaurant(
        name: String,
        type: StoreType,
        cuisine: String,
        address: String,
        license: String,
        bank: String
    ) {
        viewModelScope.launch {
            repository.registerRestaurant(
                ownerId = _currentUser.value.id,
                name = name,
                storeType = type,
                cuisine = cuisine,
                address = address,
                foodLicense = license,
                bankAccount = bank
            )
            _uiEvent.emit("Restaurant application submitted! Awaiting Admin approval.")
        }
    }

    // Driver actions
    fun toggleDriverOnline(driverId: Long, isOnline: Boolean) {
        viewModelScope.launch {
            repository.toggleDriverOnline(driverId, isOnline)
            _uiEvent.emit(if (isOnline) "You are now ONLINE & ready for trips" else "You are now OFFLINE")
        }
    }

    fun registerNewDriver(
        name: String,
        phone: String,
        email: String,
        vehicleType: String,
        vehicleNumber: String,
        license: String,
        bank: String
    ) {
        viewModelScope.launch {
            repository.registerDeliveryPartner(
                userId = _currentUser.value.id,
                name = name,
                phone = phone,
                email = email,
                vehicleType = vehicleType,
                vehicleNumber = vehicleNumber,
                licenseNumber = license,
                bankAccount = bank
            )
            _uiEvent.emit("Driver application submitted! Awaiting KYC approval.")
        }
    }

    // Admin & Super Admin actions
    fun updateRestaurantStatus(id: Long, status: VendorStatus) {
        viewModelScope.launch {
            repository.updateRestaurantStatus(id, status, _currentUser.value.name)
            _uiEvent.emit("Vendor status changed to ${status.name}")
        }
    }

    fun updateDriverStatus(id: Long, status: DeliveryPartnerStatus) {
        viewModelScope.launch {
            repository.updateDriverStatus(id, status, _currentUser.value.name)
            _uiEvent.emit("Driver status changed to ${status.name}")
        }
    }

    fun addCoupon(code: String, title: String, desc: String, percent: Int, maxDiscount: Double, minOrder: Double) {
        viewModelScope.launch {
            repository.addCoupon(code, title, desc, percent, maxDiscount, minOrder)
            _uiEvent.emit("Coupon $code created")
        }
    }

    fun deleteCoupon(id: Long) {
        viewModelScope.launch {
            repository.deleteCoupon(id)
            _uiEvent.emit("Coupon deleted")
        }
    }

    fun createSupportTicket(subject: String, message: String, orderId: Long? = null) {
        viewModelScope.launch {
            repository.createSupportTicket(
                userId = _currentUser.value.id,
                userName = _currentUser.value.name,
                userRole = _currentUser.value.role,
                orderId = orderId,
                subject = subject,
                message = message
            )
            _uiEvent.emit("Support ticket submitted. Our team will review shortly.")
        }
    }

    fun replySupportTicket(ticketId: Long, reply: String, status: TicketStatus) {
        viewModelScope.launch {
            repository.replySupportTicket(ticketId, reply, status, _currentUser.value.name)
            _uiEvent.emit("Ticket #$ticketId updated with response")
        }
    }

    fun updatePlatformSettings(
        commission: Double,
        baseDelivery: Double,
        perKm: Double,
        platformFee: Double,
        freeDeliveryAbove: Double,
        currency: String
    ) {
        viewModelScope.launch {
            repository.updateSettings(
                commissionPercent = commission,
                baseDeliveryFee = baseDelivery,
                perKmFee = perKm,
                platformFee = platformFee,
                freeDeliveryAbove = freeDeliveryAbove,
                currency = currency,
                adminName = _currentUser.value.name
            )
            _uiEvent.emit("Platform fee and commission settings updated")
        }
    }

    fun requestSettlement(recipientType: String, recipientId: Long, name: String, amount: Double) {
        viewModelScope.launch {
            repository.requestSettlement(recipientType, recipientId, name, amount)
            _uiEvent.emit("Settlement payout of ₹${String.format(Locale.US, "%.2f", amount)} processed!")
        }
    }

    fun updateUserAddress(newAddress: String) {
        _currentUser.value = _currentUser.value.copy(address = newAddress)
        viewModelScope.launch {
            _uiEvent.emit("Delivery address updated")
        }
    }
}
