package com.example.ui.vendor

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MenuItemEntity
import com.example.data.local.OrderEntity
import com.example.data.local.RestaurantEntity
import com.example.data.model.OrderStatus
import com.example.data.model.StoreType
import com.example.data.model.UserRole
import com.example.data.model.VendorStatus
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusChip
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.VegGreen
import com.example.ui.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val allRestaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allMenuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    // Find the restaurant owned by current vendor, or default to Bella Napoli
    val currentRestaurant = allRestaurants.find { it.ownerId == currentUser.id }
        ?: allRestaurants.firstOrNull { it.status == VendorStatus.APPROVED }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showRegisterStoreDialog by remember { mutableStateOf(false) }

    // Add Item Dialog State
    var newItemName by remember { mutableStateOf("") }
    var newItemDesc by remember { mutableStateOf("") }
    var newItemPrice by remember { mutableStateOf("") }
    var newItemDiscount by remember { mutableStateOf("") }
    var newItemCategory by remember { mutableStateOf("Main Course") }
    var newItemIsVeg by remember { mutableStateOf(true) }
    var newItemVariations by remember { mutableStateOf("") }
    var newItemAddons by remember { mutableStateOf("") }

    if (currentRestaurant == null) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Storefront, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Registered Restaurant Found", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Register your kitchen or grocery mart to start accepting customer orders.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showRegisterStoreDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
                ) {
                    Text("Register Store / Kitchen")
                }
            }
        }
        return
    }

    val restaurantOrders = allOrders.filter { it.restaurantId == currentRestaurant.id }
    val menuItems = allMenuItems.filter { it.restaurantId == currentRestaurant.id }

    Column(modifier = modifier.fillMaxSize()) {
        // Vendor Top Status Bar (Open / Closed toggle & Store Name)
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = currentRestaurant.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = if (currentRestaurant.status == VendorStatus.APPROVED) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = currentRestaurant.status.name,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentRestaurant.status == VendorStatus.APPROVED) VegGreen else PrimaryOrange,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "FSSAI: ${currentRestaurant.foodLicense} • ${currentRestaurant.cuisineOrCategory}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Open/Close toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (currentRestaurant.isOpen) "ACCEPTING" else "CLOSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentRestaurant.isOpen) VegGreen else Color(0xFFC62828)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = currentRestaurant.isOpen,
                        onCheckedChange = { viewModel.toggleRestaurantOpen(currentRestaurant.id, it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = VegGreen, checkedTrackColor = VegGreen.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("vendor_online_toggle")
                    )
                }
            }
        }

        // Secondary Tabs (Orders, Menu Catalogue, Settlements & Financials)
        SecondaryTabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Live Kitchen (${restaurantOrders.count { it.status in listOf(OrderStatus.PLACED, OrderStatus.RESTAURANT_ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP) }})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Menu (${menuItems.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Finances", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTab) {
            0 -> {
                // Live Orders Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("vendor_orders_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Quick Analytics Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricStatCard(
                                title = "Orders Today",
                                value = "${restaurantOrders.size}",
                                subtitle = "Kitchen volume",
                                accentColor = PrimaryOrange,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "Gross Sales",
                                value = "₹${currentRestaurant.totalSales.toInt()}",
                                subtitle = "Platform volume",
                                accentColor = SecondaryTeal,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "Net Payout",
                                value = "₹${currentRestaurant.totalEarnings.toInt()}",
                                subtitle = "After 15% fee",
                                accentColor = VegGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    if (restaurantOrders.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No incoming orders yet", fontWeight = FontWeight.Bold)
                                    Text("Customer orders will chime here in real-time.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(restaurantOrders, key = { it.id }) { order ->
                            VendorOrderCard(
                                order = order,
                                onAccept = { viewModel.updateOrderStatus(order.id, OrderStatus.RESTAURANT_ACCEPTED) },
                                onStartPrep = { viewModel.updateOrderStatus(order.id, OrderStatus.PREPARING) },
                                onMarkReady = { viewModel.updateOrderStatus(order.id, OrderStatus.READY_FOR_PICKUP) },
                                onCancel = { viewModel.cancelOrder(order.id, "Rejected by Kitchen") }
                            )
                        }
                    }
                }
            }

            1 -> {
                // Menu Catalogue Management
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("vendor_menu_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddItemDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("vendor_add_item_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add New Item / Dish", fontWeight = FontWeight.Bold)
                        }
                    }

                    items(menuItems, key = { it.id }) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    VegNonVegBadge(isVeg = item.isVeg)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("₹${item.price.toInt()} • ${item.categoryName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        if (item.variations.isNotEmpty()) {
                                            Text("Variations: ${item.variations}", fontSize = 10.sp, color = PrimaryOrange)
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = item.isAvailable,
                                        onCheckedChange = { viewModel.updateMenuItemAvailability(item.id, it) },
                                        colors = SwitchDefaults.colors(checkedThumbColor = VegGreen)
                                    )
                                    IconButton(onClick = { viewModel.deleteMenuItem(item.id) }) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC62828), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Financials & Settlements Tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Vendor Payout Balance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "₹${String.format(Locale.US, "%.2f", currentRestaurant.pendingSettlement)}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = VegGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Linked Bank: ${currentRestaurant.bankAccount}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        viewModel.requestSettlement(
                                            recipientType = "RESTAURANT",
                                            recipientId = currentRestaurant.id,
                                            name = currentRestaurant.name,
                                            amount = currentRestaurant.pendingSettlement
                                        )
                                    },
                                    enabled = currentRestaurant.pendingSettlement > 0,
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("request_settlement_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Request Settlement Payout to Bank", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Commission & Fee Breakdown", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Platform Commission", fontSize = 13.sp)
                                    Text("15.0%", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("GST on Commission", fontSize = 13.sp)
                                    Text("18.0%", fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Settlement Cycle", fontSize = 13.sp)
                                    Text("T + 1 Days / Weekly", fontWeight = FontWeight.Bold, color = PrimaryOrange)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Menu Item Dialog
        if (showAddItemDialog) {
            AlertDialog(
                onDismissRequest = { showAddItemDialog = false },
                title = { Text("Add Dish / Product") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newItemName,
                            onValueChange = { newItemName = it },
                            label = { Text("Item Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newItemDesc,
                            onValueChange = { newItemDesc = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newItemPrice,
                                onValueChange = { newItemPrice = it },
                                label = { Text("Price (₹)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = newItemDiscount,
                                onValueChange = { newItemDiscount = it },
                                label = { Text("Offer Price (Opt)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newItemVariations,
                            onValueChange = { newItemVariations = it },
                            label = { Text("Variations (e.g. Regular, Large)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newItemAddons,
                            onValueChange = { newItemAddons = it },
                            label = { Text("Addons (e.g. Extra Cheese (+₹50))") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = newItemIsVeg, onCheckedChange = { newItemIsVeg = it })
                            Text("Vegetarian Item", fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val priceVal = newItemPrice.toDoubleOrNull() ?: 0.0
                            val discountVal = newItemDiscount.toDoubleOrNull()
                            if (newItemName.isNotBlank() && priceVal > 0) {
                                viewModel.addMenuItem(
                                    restaurantId = currentRestaurant.id,
                                    category = newItemCategory,
                                    name = newItemName,
                                    description = newItemDesc,
                                    price = priceVal,
                                    discountPrice = discountVal,
                                    isVeg = newItemIsVeg,
                                    variations = newItemVariations,
                                    addons = newItemAddons
                                )
                                showAddItemDialog = false
                                newItemName = ""
                                newItemDesc = ""
                                newItemPrice = ""
                                newItemDiscount = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        modifier = Modifier.testTag("vendor_save_item_button")
                    ) {
                        Text("Save Item")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddItemDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun VendorOrderCard(
    order: OrderEntity,
    onAccept: () -> Unit,
    onStartPrep: () -> Unit,
    onMarkReady: () -> Unit,
    onCancel: () -> Unit
) {
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
                Column {
                    Text("Order #${order.orderNumber}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    val dateStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date(order.orderTimestamp))
                    Text("Placed at $dateStr • ${order.paymentMethod.name}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusChip(order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Customer: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Address: ${order.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total: ₹${order.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = PrimaryOrange)

                // Workflow action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    when (order.status) {
                        OrderStatus.PLACED -> {
                            OutlinedButton(
                                onClick = onCancel,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828))
                            ) {
                                Text("Reject", fontSize = 11.sp)
                            }
                            Button(
                                onClick = onAccept,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text("Accept Order", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.RESTAURANT_ACCEPTED -> {
                            Button(
                                onClick = onStartPrep,
                                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text("Start Preparing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.PREPARING -> {
                            Button(
                                onClick = onMarkReady,
                                colors = ButtonDefaults.buttonColors(containerColor = VegGreen),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Text("Food Ready for Pickup", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        else -> {
                            Text(order.status.label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
