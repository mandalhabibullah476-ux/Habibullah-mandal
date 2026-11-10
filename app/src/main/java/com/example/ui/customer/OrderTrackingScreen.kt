package com.example.ui.customer

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.OrderStatus
import com.example.ui.components.LiveDeliveryMapSimulation
import com.example.ui.components.StatusChip
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.VegGreen
import com.example.ui.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    orderId: Long,
    viewModel: MarketplaceViewModel,
    onBackClick: () -> Unit,
    onContactSupportClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val order = allOrders.find { it.id == orderId }

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var showRefundDialog by remember { mutableStateOf(false) }
    var refundReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(order?.orderNumber ?: "Track Order", fontWeight = FontWeight.Bold)
                        if (order != null) {
                            Text("Estimated: ${order.estimatedMinutes} mins", style = MaterialTheme.typography.bodySmall, color = PrimaryOrange)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onContactSupportClick(orderId) }) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = "Support")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (order == null) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Order details not found")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("order_tracking_view"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Live Interactive Simulated GPS Map
                item {
                    val progress = when (order.status) {
                        OrderStatus.PLACED -> 0.05f
                        OrderStatus.RESTAURANT_ACCEPTED -> 0.15f
                        OrderStatus.PREPARING -> 0.35f
                        OrderStatus.READY_FOR_PICKUP -> 0.50f
                        OrderStatus.DRIVER_ASSIGNED -> 0.60f
                        OrderStatus.OUT_FOR_DELIVERY -> 0.78f
                        OrderStatus.DELIVERED -> 1.0f
                        else -> 0.0f
                    }

                    LiveDeliveryMapSimulation(
                        progressPercent = progress,
                        riderName = order.deliveryPartnerName ?: "Awaiting Rider",
                        restaurantName = order.restaurantName,
                        customerAddress = order.deliveryAddress
                    )
                }

                // 2. Delivery OTP Card (Prominent for customer security)
                if (order.status != OrderStatus.DELIVERED && order.status != OrderStatus.CANCELLED && order.status != OrderStatus.REFUNDED) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryOrange)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = PrimaryOrange)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Delivery Verification OTP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Share this 4-digit OTP with driver", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    color = PrimaryOrange,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = order.deliveryOtp,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Order Status & Stepper
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
                                Column {
                                    Text(order.restaurantName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    val dateStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(order.orderTimestamp))
                                    Text(dateStr, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusChip(order.status)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Stepper Steps
                            OrderStatusStep(
                                title = "Order Placed & Confirmed",
                                subtitle = "We have received your order #${order.orderNumber}",
                                isDone = true,
                                isActive = order.status == OrderStatus.PLACED
                            )
                            OrderStatusStep(
                                title = "Kitchen Preparing Food",
                                subtitle = "Chef is preparing your fresh meal",
                                isDone = order.status in listOf(OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP, OrderStatus.DRIVER_ASSIGNED, OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED),
                                isActive = order.status in listOf(OrderStatus.RESTAURANT_ACCEPTED, OrderStatus.PREPARING)
                            )
                            OrderStatusStep(
                                title = "Delivery Partner Assigned",
                                subtitle = order.deliveryPartnerName ?: "Assigning closest delivery partner...",
                                isDone = order.status in listOf(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED),
                                isActive = order.status == OrderStatus.DRIVER_ASSIGNED
                            )
                            OrderStatusStep(
                                title = "Out for Delivery",
                                subtitle = "Rider is heading to your address",
                                isDone = order.status == OrderStatus.DELIVERED,
                                isActive = order.status == OrderStatus.OUT_FOR_DELIVERY
                            )
                            OrderStatusStep(
                                title = "Delivered",
                                subtitle = "Order delivered successfully. Enjoy!",
                                isDone = order.status == OrderStatus.DELIVERED,
                                isActive = order.status == OrderStatus.DELIVERED,
                                isLast = true
                            )
                        }
                    }
                }

                // 4. Delivery Partner Card (If assigned)
                if (order.deliveryPartnerName != null) {
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(DeliveryBlue.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.DeliveryDining, contentDescription = null, tint = DeliveryBlue, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(order.deliveryPartnerName ?: "", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                        Text("Electric Scooter • Rating 4.85 ★", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = DeliveryBlue.copy(alpha = 0.15f)
                                ) {
                                    IconButton(
                                        onClick = {
                                            viewModel.emitMessage("Calling delivery partner: ${order.deliveryPartnerName ?: "Rider"} (${order.deliveryPartnerPhone ?: "+91 98765 00000"})")
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Phone, contentDescription = "Call", tint = DeliveryBlue)
                                    }
                                }
                            }
                        }
                    }
                }

                // 5. Order Total & Details
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Payment Summary", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Method", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(order.paymentMethod.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Payment Status", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(order.paymentStatus.name, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = VegGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Paid", fontWeight = FontWeight.Bold)
                                Text("₹${String.format(Locale.US, "%.2f", order.totalAmount)}", fontWeight = FontWeight.ExtraBold, color = PrimaryOrange)
                            }
                        }
                    }
                }

                // 6. Action Buttons (Cancel / Refund / Support)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (order.status in listOf(OrderStatus.PLACED, OrderStatus.RESTAURANT_ACCEPTED, OrderStatus.PREPARING)) {
                            OutlinedButton(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                            ) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cancel Order")
                            }
                        }

                        if (order.status == OrderStatus.DELIVERED) {
                            OutlinedButton(
                                onClick = { showRefundDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100))
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Request Refund / Issue with Order")
                            }
                        }
                    }
                }
            }
        }

        // Cancel Order Dialog
        if (showCancelDialog) {
            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Cancel Order") },
                text = {
                    Column {
                        Text("Are you sure you want to cancel this order? Please state a reason:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cancelReason,
                            onValueChange = { cancelReason = it },
                            placeholder = { Text("e.g. Placed by mistake, changed mind") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.cancelOrder(orderId, cancelReason.ifBlank { "Cancelled by customer" })
                            showCancelDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("Confirm Cancellation")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) { Text("Back") }
                }
            )
        }

        // Refund Dialog
        if (showRefundDialog) {
            AlertDialog(
                onDismissRequest = { showRefundDialog = false },
                title = { Text("Request Refund") },
                text = {
                    Column {
                        Text("Explain the problem (e.g. Missing items, damaged package, wrong food):")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = refundReason,
                            onValueChange = { refundReason = it },
                            placeholder = { Text("Describe the issue...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.requestRefund(orderId, refundReason.ifBlank { "Customer refund request" })
                            showRefundDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                    ) {
                        Text("Submit Request")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRefundDialog = false }) { Text("Back") }
                }
            )
        }
    }
}

@Composable
fun OrderStatusStep(
    title: String,
    subtitle: String,
    isDone: Boolean,
    isActive: Boolean,
    isLast: Boolean = false
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> VegGreen
                            isActive -> PrimaryOrange
                            else -> MaterialTheme.colorScheme.outlineVariant
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isDone) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                } else if (isActive) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(if (isDone) VegGreen else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 16.dp)) {
            Text(
                text = title,
                fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
                color = if (isActive) PrimaryOrange else MaterialTheme.colorScheme.onSurface
            )
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
