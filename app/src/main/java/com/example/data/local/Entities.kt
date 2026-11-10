package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DeliveryPartnerStatus
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.StoreType
import com.example.data.model.TicketStatus
import com.example.data.model.UserRole
import com.example.data.model.VendorStatus

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val passwordHash: String,
    val isVerified: Boolean = true,
    val address: String = "42 Silicon Ave, Tech District, City",
    val latitude: Double = 12.9716,
    val longitude: Double = 77.5946,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long = 0,
    val name: String,
    val storeType: StoreType = StoreType.FOOD_RESTAURANT,
    val cuisineOrCategory: String,
    val address: String,
    val city: String = "Metro City",
    val pincode: String = "560001",
    val latitude: Double,
    val longitude: Double,
    val rating: Double = 4.5,
    val reviewCount: Int = 128,
    val deliveryRadiusKm: Double = 10.0,
    val minOrder: Double = 100.0,
    val prepTimeMinutes: Int = 25,
    val isOpen: Boolean = true,
    val status: VendorStatus = VendorStatus.APPROVED,
    val coverImage: String = "",
    val logo: String = "",
    val foodLicense: String = "FSSAI-112233445566",
    val bankAccount: String = "HDFC0001234 - A/C 9876543210",
    val totalSales: Double = 0.0,
    val totalEarnings: Double = 0.0,
    val pendingSettlement: Double = 0.0
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantId: Long = 0, // 0 for global category
    val name: String,
    val storeType: StoreType = StoreType.FOOD_RESTAURANT,
    val iconName: String = "restaurant",
    val sortOrder: Int = 0
)

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantId: Long,
    val categoryName: String,
    val name: String,
    val description: String,
    val price: Double,
    val discountPrice: Double? = null,
    val isVeg: Boolean = true,
    val isAvailable: Boolean = true,
    val imageUrl: String = "",
    val variations: String = "Standard", // comma-separated or label
    val addons: String = "Extra Cheese (+$30), Spices (+$10)"
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long = 1,
    val restaurantId: Long,
    val menuItemId: Long,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val isVeg: Boolean = true,
    val selectedVariation: String = "",
    val selectedAddons: String = ""
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String,
    val customerId: Long,
    val customerName: String,
    val customerPhone: String,
    val deliveryAddress: String,
    val customerLat: Double,
    val customerLng: Double,
    val restaurantId: Long,
    val restaurantName: String,
    val restaurantAddress: String,
    val restaurantLat: Double,
    val restaurantLng: Double,
    val deliveryPartnerId: Long? = null,
    val deliveryPartnerName: String? = null,
    val deliveryPartnerPhone: String? = null,
    val status: OrderStatus = OrderStatus.PLACED,
    val itemTotal: Double,
    val deliveryFee: Double,
    val taxes: Double,
    val platformFee: Double,
    val discountAmount: Double = 0.0,
    val couponCode: String? = null,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod = PaymentMethod.ONLINE_UPI,
    val paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    val deliveryOtp: String = "4921",
    val orderTimestamp: Long = System.currentTimeMillis(),
    val estimatedMinutes: Int = 30,
    val cancelOrRefundReason: String? = null
)

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val menuItemId: Long,
    val name: String,
    val price: Double,
    val quantity: Int,
    val isVeg: Boolean,
    val customization: String = ""
)

@Entity(tableName = "delivery_partners")
data class DeliveryPartnerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val phone: String,
    val email: String,
    val vehicleType: String = "Electric Bike",
    val vehicleNumber: String = "DL-01-AB-1234",
    val licenseNumber: String = "DL-IND-2024-9988",
    val kycStatus: DeliveryPartnerStatus = DeliveryPartnerStatus.APPROVED,
    val isOnline: Boolean = true,
    val currentLat: Double = 12.9716,
    val currentLng: Double = 77.5946,
    val totalDeliveries: Int = 240,
    val rating: Double = 4.8,
    val walletBalance: Double = 1450.0,
    val bankAccount: String = "SBI0004567 - A/C 1234567890"
)

@Entity(tableName = "coupons")
data class CouponEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,
    val title: String,
    val description: String,
    val discountPercent: Int,
    val maxDiscount: Double,
    val minOrderAmount: Double,
    val expiryDate: String = "31 Dec 2026",
    val isActive: Boolean = true
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userRole: UserRole,
    val orderId: Long? = null,
    val subject: String,
    val message: String,
    val status: TicketStatus = TicketStatus.OPEN,
    val createdAt: Long = System.currentTimeMillis(),
    val adminReply: String? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val targetRole: UserRole,
    val targetUserId: Long? = null,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val relatedOrderId: Long? = null
)

@Entity(tableName = "platform_settings")
data class PlatformSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val restaurantCommissionPercent: Double = 15.0,
    val baseDeliveryFee: Double = 25.0,
    val perKmFee: Double = 8.0,
    val platformFee: Double = 5.0,
    val taxRatePercent: Double = 5.0,
    val freeDeliveryThreshold: Double = 500.0,
    val isPlatformActive: Boolean = true,
    val currencySymbol: String = "₹"
)

@Entity(tableName = "settlements")
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recipientType: String, // "RESTAURANT" or "DELIVERY_PARTNER"
    val recipientId: Long,
    val recipientName: String,
    val amount: Double,
    val status: String = "COMPLETED", // PENDING, PROCESSING, COMPLETED
    val requestedAt: Long = System.currentTimeMillis(),
    val referenceNumber: String = "SETTLE-TXN-998811"
)

@Entity(tableName = "admin_logs")
data class AdminLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adminName: String,
    val action: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
