package com.example.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethod
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.VegGreen
import com.example.ui.viewmodel.MarketplaceViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAndCheckoutScreen(
    viewModel: MarketplaceViewModel,
    onBackClick: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()
    val allRestaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val activeCoupons by viewModel.activeCoupons.collectAsStateWithLifecycle()
    val appliedCoupon by viewModel.appliedCoupon.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val platformSettings by viewModel.platformSettings.collectAsStateWithLifecycle()

    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.ONLINE_UPI) }
    var showAddressDialog by remember { mutableStateOf(false) }
    var editedAddress by remember { mutableStateOf(currentUser.address) }
    var isProcessingPayment by remember { mutableStateOf(false) }

    val restaurant = if (cartItems.isNotEmpty()) {
        allRestaurants.find { it.id == cartItems.first().restaurantId }
    } else null

    // Financial calculations
    val baseDelivery = platformSettings?.baseDeliveryFee ?: 25.0
    val freeThreshold = platformSettings?.freeDeliveryThreshold ?: 499.0
    val deliveryFee = if (appliedCoupon?.code == "FREEDEL" || cartSubtotal >= freeThreshold) 0.0 else baseDelivery
    val platformFee = platformSettings?.platformFee ?: 5.0
    val taxRate = (platformSettings?.taxRatePercent ?: 5.0) / 100.0
    val taxes = cartSubtotal * taxRate

    val discountAmount = if (appliedCoupon != null) {
        val coupon = appliedCoupon!!
        if (coupon.code != "FREEDEL") {
            val calc = cartSubtotal * (coupon.discountPercent / 100.0)
            minOf(calc, coupon.maxDiscount)
        } else {
            0.0
        }
    } else 0.0

    val finalTotal = maxOf(0.0, cartSubtotal + deliveryFee + platformFee + taxes - discountAmount)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Cart & Checkout", fontWeight = FontWeight.Bold)
                        if (restaurant != null) {
                            Text(restaurant.name, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCart() }, modifier = Modifier.testTag("clear_cart_button")) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Cart", tint = Color(0xFFC62828))
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty() && restaurant != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TO PAY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "₹${String.format(Locale.US, "%.2f", finalTotal)}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryOrange
                                )
                            }

                            Button(
                                onClick = {
                                    isProcessingPayment = true
                                    viewModel.checkout(
                                        restaurant = restaurant,
                                        deliveryFee = deliveryFee,
                                        taxes = taxes,
                                        platformFee = platformFee,
                                        discount = discountAmount,
                                        total = finalTotal,
                                        paymentMethod = selectedPaymentMethod,
                                        onSuccess = { orderId ->
                                            isProcessingPayment = false
                                            onOrderPlaced(orderId)
                                        }
                                    )
                                },
                                enabled = !isProcessingPayment,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("place_order_button")
                            ) {
                                if (isProcessingPayment) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Verifying Payment...")
                                } else {
                                    Text("Place Order", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Your cart is empty", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Explore delicious food & fresh groceries nearby!", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) {
                        Text("Browse Stores")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("checkout_scroll_view"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Order Items Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Items in Cart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))

                            cartItems.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        VegNonVegBadge(isVeg = item.isVeg)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                            val customText = listOfNotNull(item.selectedVariation.takeIf { it.isNotEmpty() }, item.selectedAddons.takeIf { it.isNotEmpty() }).joinToString(", ")
                                            if (customText.isNotEmpty()) {
                                                Text(customText, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { viewModel.updateCartQuantity(item.id, item.quantity - 1) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(12.dp))
                                                }
                                                Text("${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                                IconButton(
                                                    onClick = { viewModel.updateCartQuantity(item.id, item.quantity + 1) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text("₹${(item.price * item.quantity).toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Delivery Address Card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Delivery Address", fontWeight = FontWeight.Bold)
                                }
                                TextButton(onClick = { showAddressDialog = true }) {
                                    Text("Change", color = PrimaryOrange)
                                }
                            }
                            Text(
                                text = currentUser.address,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 3. Coupons & Offers
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.LocalOffer, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Offers & Coupons", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            if (appliedCoupon != null) {
                                Surface(
                                    color = Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = VegGreen, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "'${appliedCoupon!!.code}' applied",
                                                    fontWeight = FontWeight.Bold,
                                                    color = VegGreen
                                                )
                                                Text(
                                                    text = appliedCoupon!!.title,
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        TextButton(onClick = { viewModel.removeCoupon() }) {
                                            Text("Remove", color = Color(0xFFC62828))
                                        }
                                    }
                                }
                            } else {
                                activeCoupons.take(2).forEach { coupon ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(coupon.code, fontWeight = FontWeight.Bold, color = PrimaryOrange, fontSize = 13.sp)
                                            Text(coupon.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.applyCoupon(coupon, cartSubtotal) },
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Apply", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4. Payment Method Selection
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Payment, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Payment Method", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            PaymentMethodRow(
                                title = "Instant UPI (GPay / PhonePe / Paytm)",
                                icon = Icons.Default.PhoneAndroid,
                                isSelected = selectedPaymentMethod == PaymentMethod.ONLINE_UPI,
                                onSelect = { selectedPaymentMethod = PaymentMethod.ONLINE_UPI }
                            )

                            PaymentMethodRow(
                                title = "Credit / Debit Card",
                                icon = Icons.Default.CreditCard,
                                isSelected = selectedPaymentMethod == PaymentMethod.ONLINE_CARD,
                                onSelect = { selectedPaymentMethod = PaymentMethod.ONLINE_CARD }
                            )

                            PaymentMethodRow(
                                title = "Cash on Delivery (COD)",
                                icon = Icons.Default.Money,
                                isSelected = selectedPaymentMethod == PaymentMethod.CASH_ON_DELIVERY,
                                onSelect = { selectedPaymentMethod = PaymentMethod.CASH_ON_DELIVERY }
                            )
                        }
                    }
                }

                // 5. Detailed Bill Summary
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Bill Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            BillRow(label = "Item Total", value = "₹${String.format(Locale.US, "%.2f", cartSubtotal)}")
                            BillRow(
                                label = "Delivery Partner Fee",
                                value = if (deliveryFee == 0.0) "FREE" else "₹${String.format(Locale.US, "%.2f", deliveryFee)}",
                                isDiscount = deliveryFee == 0.0
                            )
                            BillRow(label = "Platform Convenience Fee", value = "₹${String.format(Locale.US, "%.2f", platformFee)}")
                            BillRow(label = "Government Taxes & Restaurant GST (5%)", value = "₹${String.format(Locale.US, "%.2f", taxes)}")

                            if (discountAmount > 0) {
                                BillRow(
                                    label = "Coupon Discount (${appliedCoupon?.code})",
                                    value = "-₹${String.format(Locale.US, "%.2f", discountAmount)}",
                                    isDiscount = true
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("To Pay", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                                Text(
                                    "₹${String.format(Locale.US, "%.2f", finalTotal)}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = PrimaryOrange
                                )
                            }
                        }
                    }
                }
            }
        }

        // Edit Address Dialog
        if (showAddressDialog) {
            AlertDialog(
                onDismissRequest = { showAddressDialog = false },
                title = { Text("Update Delivery Address") },
                text = {
                    OutlinedTextField(
                        value = editedAddress,
                        onValueChange = { editedAddress = it },
                        label = { Text("Complete Address") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateUserAddress(editedAddress)
                            showAddressDialog = false
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddressDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun BillRow(label: String, value: String, isDiscount: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isDiscount) VegGreen else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PaymentMethodRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onSelect)
        Spacer(modifier = Modifier.width(6.dp))
        Icon(imageVector = icon, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
