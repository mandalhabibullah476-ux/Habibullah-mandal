package com.example.data.model

enum class UserRole(val displayName: String, val badgeColorHex: Long) {
    CUSTOMER("Customer", 0xFF00897B),
    RESTAURANT_OWNER("Restaurant Owner", 0xFFFF5722),
    RESTAURANT_STAFF("Restaurant Staff", 0xFFF57C00),
    DELIVERY_PARTNER("Delivery Partner", 0xFF1E88E5),
    ADMIN_STAFF("Operations Admin", 0xFF5E35B1),
    SUPER_ADMIN("Master Platform Owner", 0xFF311B92)
}

enum class OrderStatus(val label: String, val stepIndex: Int) {
    PLACED("Order Placed", 0),
    RESTAURANT_ACCEPTED("Restaurant Accepted", 1),
    PREPARING("Preparing Food / Packing", 2),
    READY_FOR_PICKUP("Ready for Pickup", 3),
    DRIVER_ASSIGNED("Delivery Partner Assigned", 4),
    OUT_FOR_DELIVERY("Out for Delivery", 5),
    DELIVERED("Delivered", 6),
    CANCELLED("Cancelled", -1),
    REFUND_REQUESTED("Refund Requested", -2),
    REFUNDED("Refund Processed", -3)
}

enum class PaymentMethod(val label: String) {
    CASH_ON_DELIVERY("Cash on Delivery (COD)"),
    ONLINE_UPI("UPI (Google Pay, PhonePe, Paytm)"),
    ONLINE_CARD("Credit / Debit Card"),
    NET_BANKING("Net Banking")
}

enum class PaymentStatus {
    PENDING,
    SUCCESSFUL,
    FAILED,
    REFUNDED
}

enum class VendorStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}

enum class DeliveryPartnerStatus {
    PENDING,
    APPROVED,
    REJECTED,
    SUSPENDED
}

enum class StoreType {
    FOOD_RESTAURANT,
    GROCERY_MART
}

enum class TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
}
