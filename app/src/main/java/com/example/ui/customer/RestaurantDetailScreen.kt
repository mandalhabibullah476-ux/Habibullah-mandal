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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MenuItemEntity
import com.example.data.local.RestaurantEntity
import com.example.data.model.StoreType
import com.example.ui.components.RatingPill
import com.example.ui.components.VegNonVegBadge
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTeal
import com.example.ui.viewmodel.MarketplaceViewModel
import java.util.Locale

@Composable
fun RestaurantDetailScreen(
    restaurantId: Long,
    viewModel: MarketplaceViewModel,
    onBackClick: () -> Unit,
    onViewCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allRestaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val allMenuItems by viewModel.allMenuItems.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartSubtotal by viewModel.cartSubtotal.collectAsStateWithLifecycle()

    val restaurant = allRestaurants.find { it.id == restaurantId }
    val menuItems = allMenuItems.filter { it.restaurantId == restaurantId }

    // Customization dialog state
    var customizingItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var selectedVariation by remember { mutableStateOf("") }
    var selectedAddon by remember { mutableStateOf("") }

    if (restaurant == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Restaurant not found")
        }
        return
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("restaurant_detail_list"),
            contentPadding = PaddingValues(bottom = if (cartItems.isNotEmpty()) 90.dp else 24.dp)
        ) {
            // Header Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .background(
                            Brush.verticalGradient(
                                if (restaurant.storeType == StoreType.GROCERY_MART)
                                    listOf(Color(0xFF004D40), Color(0xFF00796B))
                                else
                                    listOf(Color(0xFFBF360C), Color(0xFFFF5722))
                            )
                        )
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.4f))
                            .testTag("detail_back_button")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = restaurant.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = restaurant.cuisineOrCategory,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // Restaurant Info Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RatingPill(rating = restaurant.rating, reviewCount = restaurant.reviewCount)

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = PrimaryOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${restaurant.prepTimeMinutes} mins delivery",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "📍 ${restaurant.address}, ${restaurant.city}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "License: ${restaurant.foodLicense} • Min Order: ₹${restaurant.minOrder.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Menu Section Header
            item {
                Text(
                    text = "Menu & Catalogue (${menuItems.size} items)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Menu Items List
            items(menuItems, key = { it.id }) { item ->
                val existingCartItem = cartItems.find { it.menuItemId == item.id }

                MenuItemRow(
                    item = item,
                    cartQuantity = existingCartItem?.quantity ?: 0,
                    onAddClick = {
                        if (item.variations.isNotEmpty() || item.addons.isNotEmpty()) {
                            customizingItem = item
                            selectedVariation = item.variations.split(",").firstOrNull()?.trim() ?: ""
                            selectedAddon = ""
                        } else {
                            viewModel.addToCart(
                                restaurantId = restaurant.id,
                                menuItemId = item.id,
                                name = item.name,
                                price = item.discountPrice ?: item.price,
                                isVeg = item.isVeg
                            )
                        }
                    },
                    onIncrement = {
                        if (existingCartItem != null) {
                            viewModel.updateCartQuantity(existingCartItem.id, existingCartItem.quantity + 1)
                        }
                    },
                    onDecrement = {
                        if (existingCartItem != null) {
                            viewModel.updateCartQuantity(existingCartItem.id, existingCartItem.quantity - 1)
                        }
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // Customization Dialog
        if (customizingItem != null) {
            val item = customizingItem!!
            AlertDialog(
                onDismissRequest = { customizingItem = null },
                title = {
                    Column {
                        Text(text = "Customize ${item.name}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(text = "₹${String.format(Locale.US, "%.2f", item.discountPrice ?: item.price)}", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        if (item.variations.isNotEmpty()) {
                            Text(text = "Select Portion / Size:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            item.variations.split(",").forEach { variation ->
                                val cleanVar = variation.trim()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedVariation = cleanVar }
                                ) {
                                    RadioButton(
                                        selected = selectedVariation == cleanVar,
                                        onClick = { selectedVariation = cleanVar }
                                    )
                                    Text(cleanVar, fontSize = 13.sp)
                                }
                            }
                        }

                        if (item.addons.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Add Extra Toppings & Dips:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            item.addons.split(",").forEach { addon ->
                                val cleanAddon = addon.trim()
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedAddon = if (selectedAddon == cleanAddon) "" else cleanAddon }
                                ) {
                                    RadioButton(
                                        selected = selectedAddon == cleanAddon,
                                        onClick = { selectedAddon = if (selectedAddon == cleanAddon) "" else cleanAddon }
                                    )
                                    Text(cleanAddon, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.addToCart(
                                restaurantId = restaurant.id,
                                menuItemId = item.id,
                                name = item.name,
                                price = item.discountPrice ?: item.price,
                                isVeg = item.isVeg,
                                variation = selectedVariation,
                                addons = selectedAddon
                            )
                            customizingItem = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        modifier = Modifier.testTag("confirm_customization_button")
                    ) {
                        Text("Add to Cart")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { customizingItem = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Bottom Sticky Cart Bar
        if (cartItems.isNotEmpty()) {
            Surface(
                onClick = onViewCartClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .testTag("detail_sticky_cart_bar"),
                shape = RoundedCornerShape(16.dp),
                color = PrimaryOrange,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${cartItems.sumOf { it.quantity }} items added",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Text(
                            text = "₹${String.format(Locale.US, "%.2f", cartSubtotal)} plus taxes",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Button(
                        onClick = onViewCartClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("detail_view_cart_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View Cart", color = PrimaryOrange, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemRow(
    item: MenuItemEntity,
    cartQuantity: Int,
    onAddClick: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("menu_item_row_${item.id}"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            VegNonVegBadge(isVeg = item.isVeg)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val effectivePrice = item.discountPrice ?: item.price
                Text(
                    text = "₹${effectivePrice.toInt()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (item.discountPrice != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "₹${item.price.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (item.variations.isNotEmpty() || item.addons.isNotEmpty()) {
                Text(
                    text = "Customisable",
                    style = MaterialTheme.typography.labelSmall,
                    color = PrimaryOrange,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Quantity Selector or Add Button
        if (cartQuantity == 0) {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFF3E0), contentColor = PrimaryOrange),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryOrange),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_item_${item.id}")
            ) {
                Text("ADD", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
            }
        } else {
            Surface(
                color = PrimaryOrange,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(onClick = onDecrement, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                    Text(
                        text = cartQuantity.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = onIncrement, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}
