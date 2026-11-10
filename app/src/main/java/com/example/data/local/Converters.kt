package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.DeliveryPartnerStatus
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.StoreType
import com.example.data.model.TicketStatus
import com.example.data.model.UserRole
import com.example.data.model.VendorStatus

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.CUSTOMER)

    @TypeConverter
    fun fromOrderStatus(value: OrderStatus): String = value.name

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus = runCatching { OrderStatus.valueOf(value) }.getOrDefault(OrderStatus.PLACED)

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = runCatching { PaymentMethod.valueOf(value) }.getOrDefault(PaymentMethod.CASH_ON_DELIVERY)

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus): String = value.name

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus = runCatching { PaymentStatus.valueOf(value) }.getOrDefault(PaymentStatus.PENDING)

    @TypeConverter
    fun fromVendorStatus(value: VendorStatus): String = value.name

    @TypeConverter
    fun toVendorStatus(value: String): VendorStatus = runCatching { VendorStatus.valueOf(value) }.getOrDefault(VendorStatus.APPROVED)

    @TypeConverter
    fun fromDeliveryPartnerStatus(value: DeliveryPartnerStatus): String = value.name

    @TypeConverter
    fun toDeliveryPartnerStatus(value: String): DeliveryPartnerStatus = runCatching { DeliveryPartnerStatus.valueOf(value) }.getOrDefault(DeliveryPartnerStatus.APPROVED)

    @TypeConverter
    fun fromStoreType(value: StoreType): String = value.name

    @TypeConverter
    fun toStoreType(value: String): StoreType = runCatching { StoreType.valueOf(value) }.getOrDefault(StoreType.FOOD_RESTAURANT)

    @TypeConverter
    fun fromTicketStatus(value: TicketStatus): String = value.name

    @TypeConverter
    fun toTicketStatus(value: String): TicketStatus = runCatching { TicketStatus.valueOf(value) }.getOrDefault(TicketStatus.OPEN)
}
