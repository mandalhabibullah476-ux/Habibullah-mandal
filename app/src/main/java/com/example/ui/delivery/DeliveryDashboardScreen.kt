package com.example.ui.delivery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.OrderEntity
import com.example.data.model.DeliveryPartnerStatus
import com.example.data.model.OrderStatus
import com.example.ui.components.LiveDeliveryMapSimulation
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusChip
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.VegGreen
import com.example.ui.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeliveryDashboardScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val allDeliveryPartners by viewModel.allDeliveryPartners.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val currentPartner = allDeliveryPartners.find { it.userId == currentUser.id }
        ?: allDeliveryPartners.firstOrNull { it.kycStatus == DeliveryPartnerStatus.APPROVED }

    var showOtpDialog by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var verifyingOrderId by remember { mutableStateOf<Long?>(null) }

    if (currentPartner == null) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.SportsMotorsports, contentDescription = null, tint = DeliveryBlue, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Delivery Partner Profile Found", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Register your vehicle and license documents to start earning.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val assignedOrders = allOrders.filter { it.deliveryPartnerId == currentPartner.id }
    val activeDelivery = assignedOrders.find { it.status in listOf(OrderStatus.DRIVER_ASSIGNED, OrderStatus.OUT_FOR_DELIVERY) }
    val availableOrders = allOrders.filter { it.deliveryPartnerId == null && it.status in listOf(OrderStatus.READY_FOR_PICKUP, OrderStatus.PREPARING) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("delivery_dashboard_view"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Partner Status & Duty Switch
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(DeliveryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(currentPartner.name, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
                            Text("${currentPartner.vehicleType} • ${currentPartner.vehicleNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${currentPartner.rating} Rating (${currentPartner.totalDeliveries} trips)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (currentPartner.isOnline) "ON DUTY" else "OFFLINE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (currentPartner.isOnline) VegGreen else Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Switch(
                            checked = currentPartner.isOnline,
                            onCheckedChange = { viewModel.toggleDriverOnline(currentPartner.id, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = VegGreen),
                            modifier = Modifier.testTag("driver_duty_switch")
                        )
                    }
                }
            }
        }

        // 2. Earnings & Trip Metrics
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    title = "Wallet Balance",
                    value = "₹${currentPartner.walletBalance.toInt()}",
                    subtitle = "Available payout",
                    accentColor = VegGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Deliveries",
                    value = "${currentPartner.totalDeliveries}",
                    subtitle = "Completed total",
                    accentColor = DeliveryBlue,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Per Trip Fee",
                    value = "₹45.00",
                    subtitle = "+ surge bonuses",
                    accentColor = PrimaryOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Active Delivery In-Progress (If any)
        if (activeDelivery != null) {
            item {
                Text("Current Active Trip", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DeliveryBlue)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Trip #${activeDelivery.orderNumber}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            StatusChip(activeDelivery.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        // Visual GPS map
                        LiveDeliveryMapSimulation(
                            progressPercent = 0.75f,
                            riderName = currentPartner.name,
                            restaurantName = activeDelivery.restaurantName,
                            customerAddress = activeDelivery.deliveryAddress
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        // Pickup
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick Up: ${activeDelivery.restaurantName} (${activeDelivery.restaurantAddress})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        // Drop
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = VegGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Drop To: ${activeDelivery.customerName} - ${activeDelivery.deliveryAddress}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (activeDelivery.status == OrderStatus.DRIVER_ASSIGNED) {
                                Button(
                                    onClick = { viewModel.updateOrderStatus(activeDelivery.id, OrderStatus.OUT_FOR_DELIVERY) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeliveryBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("picked_up_order_button")
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Food Picked Up")
                                }
                            } else if (activeDelivery.status == OrderStatus.OUT_FOR_DELIVERY) {
                                Button(
                                    onClick = {
                                        verifyingOrderId = activeDelivery.id
                                        enteredOtp = ""
                                        showOtpDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("verify_otp_complete_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verify OTP & Complete Trip")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.emitMessage("Connecting call to customer: ${activeDelivery.customerName} (${activeDelivery.customerPhone})")
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Call Customer", tint = DeliveryBlue)
                            }
                        }
                    }
                }
            }
        }

        // 4. Available Trips Ready for Pickup
        item {
            Text("Available Delivery Requests (${availableOrders.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        if (availableOrders.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No orders currently waiting for pickup nearby", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(availableOrders, key = { it.id }) { order ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("New Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Payout: ₹45.00", fontWeight = FontWeight.ExtraBold, color = VegGreen, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("From: ${order.restaurantName}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text("To: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.assignDriverToOrder(order.id, currentPartner) },
                            enabled = currentPartner.isOnline && activeDelivery == null,
                            colors = ButtonDefaults.buttonColors(containerColor = DeliveryBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("accept_trip_button_${order.id}")
                        ) {
                            Text(if (activeDelivery != null) "Finish Current Trip First" else "Accept Delivery (+₹45)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // OTP Verification Dialog
    if (showOtpDialog && verifyingOrderId != null) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimaryOrange)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enter Customer Delivery OTP")
                }
            },
            text = {
                Column {
                    Text("Ask the customer for the 4-digit code shown on their tracking screen:")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { if (it.length <= 4) enteredOtp = it },
                        label = { Text("4-Digit OTP") },
                        placeholder = { Text("e.g. 4812") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("rider_otp_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.verifyDeliveryOtp(verifyingOrderId!!, enteredOtp) {
                            showOtpDialog = false
                            verifyingOrderId = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                    modifier = Modifier.testTag("submit_rider_otp_button")
                ) {
                    Text("Confirm Delivery")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) { Text("Cancel") }
            }
        )
    }
}
