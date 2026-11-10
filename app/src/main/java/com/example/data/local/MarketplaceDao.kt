package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DeliveryPartnerStatus
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.TicketStatus
import com.example.data.model.UserRole
import com.example.data.model.VendorStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceDao {

    // --- Users ---
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- Restaurants ---
    @Query("SELECT * FROM restaurants ORDER BY rating DESC, id DESC")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE status = 'APPROVED' ORDER BY rating DESC")
    fun getApprovedRestaurants(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    fun getRestaurantById(id: Long): Flow<RestaurantEntity?>

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    suspend fun getRestaurantDirect(id: Long): RestaurantEntity?

    @Query("SELECT * FROM restaurants WHERE ownerId = :ownerId LIMIT 1")
    fun getRestaurantByOwner(ownerId: Long): Flow<RestaurantEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity): Long

    @Update
    suspend fun updateRestaurant(restaurant: RestaurantEntity)

    @Query("UPDATE restaurants SET status = :status WHERE id = :id")
    suspend fun updateRestaurantStatus(id: Long, status: VendorStatus)

    @Query("UPDATE restaurants SET isOpen = :isOpen WHERE id = :id")
    suspend fun updateRestaurantOpenStatus(id: Long, isOpen: Boolean)

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    // --- Menu Items ---
    @Query("SELECT * FROM menu_items ORDER BY id DESC")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE restaurantId = :restaurantId ORDER BY isAvailable DESC, id DESC")
    fun getMenuItemsByRestaurant(restaurantId: Long): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity): Long

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItem(id: Long)

    @Query("UPDATE menu_items SET isAvailable = :isAvailable WHERE id = :id")
    suspend fun updateMenuItemAvailability(id: Long, isAvailable: Boolean)

    // --- Cart ---
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    fun getCartItems(userId: Long): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartItemQuantity(id: Long, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun clearCart(userId: Long)

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY orderTimestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderTimestamp DESC")
    fun getOrdersForCustomer(customerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE restaurantId = :restaurantId ORDER BY orderTimestamp DESC")
    fun getOrdersForRestaurant(restaurantId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE deliveryPartnerId = :partnerId ORDER BY orderTimestamp DESC")
    fun getOrdersForDeliveryPartner(partnerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status IN ('READY_FOR_PICKUP', 'PREPARING') AND deliveryPartnerId IS NULL ORDER BY orderTimestamp DESC")
    fun getAvailableDeliveryOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus)

    @Query("UPDATE orders SET deliveryPartnerId = :partnerId, deliveryPartnerName = :partnerName, deliveryPartnerPhone = :partnerPhone, status = 'DRIVER_ASSIGNED' WHERE id = :orderId")
    suspend fun assignDeliveryPartner(orderId: Long, partnerId: Long, partnerName: String, partnerPhone: String)

    @Query("UPDATE orders SET status = 'CANCELLED', cancelOrRefundReason = :reason WHERE id = :orderId")
    suspend fun cancelOrder(orderId: Long, reason: String)

    @Query("UPDATE orders SET status = 'REFUND_REQUESTED', cancelOrRefundReason = :reason WHERE id = :orderId")
    suspend fun requestRefund(orderId: Long, reason: String)

    @Query("UPDATE orders SET status = 'REFUNDED', paymentStatus = 'REFUNDED' WHERE id = :orderId")
    suspend fun processRefund(orderId: Long)

    // Order Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Query("SELECT * FROM order_items WHERE orderId = :orderId")
    fun getOrderItems(orderId: Long): Flow<List<OrderItemEntity>>

    // --- Delivery Partners ---
    @Query("SELECT * FROM delivery_partners ORDER BY rating DESC")
    fun getAllDeliveryPartners(): Flow<List<DeliveryPartnerEntity>>

    @Query("SELECT * FROM delivery_partners WHERE userId = :userId LIMIT 1")
    fun getDeliveryPartnerByUserId(userId: Long): Flow<DeliveryPartnerEntity?>

    @Query("SELECT * FROM delivery_partners WHERE id = :id LIMIT 1")
    fun getDeliveryPartnerById(id: Long): Flow<DeliveryPartnerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeliveryPartner(partner: DeliveryPartnerEntity): Long

    @Update
    suspend fun updateDeliveryPartner(partner: DeliveryPartnerEntity)

    @Query("UPDATE delivery_partners SET kycStatus = :status WHERE id = :id")
    suspend fun updateDeliveryPartnerStatus(id: Long, status: DeliveryPartnerStatus)

    @Query("UPDATE delivery_partners SET isOnline = :isOnline WHERE id = :id")
    suspend fun updateDeliveryPartnerOnline(id: Long, isOnline: Boolean)

    // --- Coupons ---
    @Query("SELECT * FROM coupons ORDER BY discountPercent DESC")
    fun getAllCoupons(): Flow<List<CouponEntity>>

    @Query("SELECT * FROM coupons WHERE isActive = 1")
    fun getActiveCoupons(): Flow<List<CouponEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoupon(coupon: CouponEntity): Long

    @Query("DELETE FROM coupons WHERE id = :id")
    suspend fun deleteCoupon(id: Long)

    // --- Support Tickets ---
    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun getAllSupportTickets(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun getTicketsForUser(userId: Long): Flow<List<SupportTicketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupportTicket(ticket: SupportTicketEntity): Long

    @Query("UPDATE support_tickets SET adminReply = :reply, status = :status WHERE id = :ticketId")
    suspend fun replySupportTicket(ticketId: Long, reply: String, status: TicketStatus)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE targetRole = :role OR targetUserId = :userId ORDER BY timestamp DESC")
    fun getNotificationsForRoleAndUser(role: UserRole, userId: Long): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // --- Platform Settings ---
    @Query("SELECT * FROM platform_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<PlatformSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: PlatformSettingsEntity)

    // --- Admin Logs ---
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC LIMIT 50")
    fun getAdminLogs(): Flow<List<AdminLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLogEntity): Long

    // --- Settlements ---
    @Query("SELECT * FROM settlements ORDER BY requestedAt DESC")
    fun getAllSettlements(): Flow<List<SettlementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettlement(settlement: SettlementEntity): Long
}
