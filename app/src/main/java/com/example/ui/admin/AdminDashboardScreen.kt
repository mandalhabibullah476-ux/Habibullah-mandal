package com.example.ui.admin

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsMotorsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.SupportTicketEntity
import com.example.data.model.DeliveryPartnerStatus
import com.example.data.model.TicketStatus
import com.example.data.model.VendorStatus
import com.example.ui.components.MetricStatCard
import com.example.ui.theme.PremiumBorderStroke
import com.example.ui.theme.PremiumCardElevated
import com.example.ui.theme.PremiumCardSurface
import com.example.ui.theme.PremiumCrimson
import com.example.ui.theme.PremiumEmerald
import com.example.ui.theme.PremiumEmeraldLight
import com.example.ui.theme.PremiumGoldDark
import com.example.ui.theme.PremiumGoldLight
import com.example.ui.theme.PremiumGoldPrimary
import com.example.ui.theme.PremiumObsidianBg
import com.example.ui.theme.PremiumSapphireLight
import com.example.ui.theme.PremiumSlateSurface
import com.example.ui.theme.PremiumTextPrimary
import com.example.ui.theme.PremiumTextSecondary
import com.example.ui.theme.PremiumTextTertiary
import com.example.ui.theme.PremiumVioletLight
import com.example.ui.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    viewModel: MarketplaceViewModel,
    modifier: Modifier = Modifier
) {
    val allRestaurants by viewModel.allRestaurants.collectAsStateWithLifecycle()
    val allDeliveryPartners by viewModel.allDeliveryPartners.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val allCoupons by viewModel.allCoupons.collectAsStateWithLifecycle()
    val allTickets by viewModel.allSupportTickets.collectAsStateWithLifecycle()
    val platformSettings by viewModel.platformSettings.collectAsStateWithLifecycle()
    val adminLogs by viewModel.adminLogs.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialogs state
    var showAddCouponDialog by remember { mutableStateOf(false) }
    var couponCode by remember { mutableStateOf("") }
    var couponTitle by remember { mutableStateOf("") }
    var couponDesc by remember { mutableStateOf("") }
    var couponPercent by remember { mutableStateOf("20") }
    var couponMax by remember { mutableStateOf("100") }
    var couponMinOrder by remember { mutableStateOf("299") }

    // Ticket Reply Dialog
    var replyingTicket by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var ticketReplyText by remember { mutableStateOf("") }

    // Settings edit state
    var commRate by remember { mutableStateOf((platformSettings?.restaurantCommissionPercent ?: 15.0).toString()) }
    var baseFee by remember { mutableStateOf((platformSettings?.baseDeliveryFee ?: 25.0).toString()) }
    var platformFeeVal by remember { mutableStateOf((platformSettings?.platformFee ?: 5.0).toString()) }
    var freeThresholdVal by remember { mutableStateOf((platformSettings?.freeDeliveryThreshold ?: 499.0).toString()) }

    val totalGMV = allOrders.sumOf { it.totalAmount }
    val estimatedPlatformRevenue = totalGMV * ((platformSettings?.restaurantCommissionPercent ?: 15.0) / 100.0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PremiumObsidianBg)
    ) {
        // Executive Premium Navigation Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 12.dp,
            containerColor = PremiumSlateSurface,
            contentColor = PremiumGoldPrimary
        ) {
            val tabs = listOf(
                "Overview & GMV",
                "Vendors (${allRestaurants.size})",
                "Fleet Partners (${allDeliveryPartners.size})",
                "Fee Settings",
                "Coupons (${allCoupons.size})",
                "Support Tickets",
                "Audit Trail"
            )
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) PremiumGoldLight else PremiumTextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Tab 0: Executive Overview & GMV Control
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_overview_view"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Executive Master Platform Owner Hero Card (Unrestricted Access)
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PremiumCardSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(
                                1.5.dp,
                                Brush.horizontalGradient(
                                    listOf(PremiumGoldPrimary, PremiumGoldLight, PremiumGoldDark)
                                )
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("master_owner_card")
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PremiumGoldPrimary,
                                        modifier = Modifier.size(50.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "👑",
                                                fontSize = 24.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "MASTER PLATFORM OWNER",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 1.2.sp,
                                                color = PremiumGoldLight
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = PremiumEmerald
                                            ) {
                                                Text(
                                                    text = "FULL ACCESS",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Habibullah Mandal",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = PremiumTextPrimary
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = PremiumGoldPrimary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "mandalhabibullah476@gmail.com",
                                                fontSize = 12.sp,
                                                color = PremiumGoldLight
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))
                                HorizontalDivider(color = PremiumBorderStroke)
                                Spacer(modifier = Modifier.height(10.dp))

                                // Master Badges
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PremiumCardElevated,
                                        border = BorderStroke(1.dp, PremiumBorderStroke)
                                    ) {
                                        Text(
                                            text = "⚡ Root Authority",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PremiumTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PremiumCardElevated,
                                        border = BorderStroke(1.dp, PremiumBorderStroke)
                                    ) {
                                        Text(
                                            text = "💰 100% GMV & Payout Control",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PremiumTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PremiumCardElevated,
                                        border = BorderStroke(1.dp, PremiumBorderStroke)
                                    ) {
                                        Text(
                                            text = "🛡️ Global Arbitration",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PremiumTextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 2. High-Impact Financial KPI Row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            MetricStatCard(
                                title = "Gross Marketplace GMV",
                                value = "₹${totalGMV.toInt()}",
                                subtitle = "Cumulative volume",
                                accentColor = PremiumGoldPrimary,
                                containerColor = PremiumCardSurface,
                                borderColor = PremiumBorderStroke,
                                titleColor = PremiumTextSecondary,
                                subtitleColor = PremiumTextTertiary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "Platform Net Revenue",
                                value = "₹${estimatedPlatformRevenue.toInt()}",
                                subtitle = "At 15% commission",
                                accentColor = PremiumEmeraldLight,
                                containerColor = PremiumCardSurface,
                                borderColor = PremiumBorderStroke,
                                titleColor = PremiumTextSecondary,
                                subtitleColor = PremiumTextTertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 3. Operational Metrics
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetricStatCard(
                                title = "Total Orders",
                                value = "${allOrders.size}",
                                subtitle = "Placed on platform",
                                accentColor = PremiumSapphireLight,
                                containerColor = PremiumCardSurface,
                                borderColor = PremiumBorderStroke,
                                titleColor = PremiumTextSecondary,
                                subtitleColor = PremiumTextTertiary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "Fleet Riders",
                                value = "${allDeliveryPartners.size}",
                                subtitle = "Active dispatchers",
                                accentColor = PremiumEmeraldLight,
                                containerColor = PremiumCardSurface,
                                borderColor = PremiumBorderStroke,
                                titleColor = PremiumTextSecondary,
                                subtitleColor = PremiumTextTertiary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricStatCard(
                                title = "Stores & Marts",
                                value = "${allRestaurants.size}",
                                subtitle = "Approved partners",
                                accentColor = PremiumVioletLight,
                                containerColor = PremiumCardSurface,
                                borderColor = PremiumBorderStroke,
                                titleColor = PremiumTextSecondary,
                                subtitleColor = PremiumTextTertiary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. Real-Time Telemetry & SLA Compliance
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PremiumCardElevated),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, PremiumBorderStroke),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Platform Operational SLAs & Telemetry",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = PremiumTextPrimary
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = PremiumEmerald.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(PremiumEmerald, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "HEALTHY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = PremiumEmeraldLight
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // SLA Metric 1: Food Delivery SLA
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Food Delivery Turnaround", fontSize = 12.sp, color = PremiumTextSecondary)
                                    Text("28 min avg (99.2% On-Time)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PremiumGoldLight)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { 0.99f },
                                    color = PremiumGoldPrimary,
                                    trackColor = PremiumCardSurface,
                                    modifier = Modifier.fillMaxWidth().height(6.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // SLA Metric 2: 15-Min Quick Grocery Mart Hubs
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("15-Min Quick Mart Hub Dispatch", fontSize = 12.sp, color = PremiumTextSecondary)
                                    Text("11.8 min avg (100% Online)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PremiumEmeraldLight)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { 1.0f },
                                    color = PremiumEmerald,
                                    trackColor = PremiumCardSurface,
                                    modifier = Modifier.fillMaxWidth().height(6.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // SLA Metric 3: Automated Financial Settlement
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Weekly Vendor Settlement Payouts", fontSize = 12.sp, color = PremiumTextSecondary)
                                    Text("100% Reconciled", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PremiumSapphireLight)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { 1.0f },
                                    color = PremiumSapphireLight,
                                    trackColor = PremiumCardSurface,
                                    modifier = Modifier.fillMaxWidth().height(6.dp)
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Tab 1: Vendors KYC Management
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_vendors_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allRestaurants, key = { it.id }) { r ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PremiumCardSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PremiumBorderStroke),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = r.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = PremiumTextPrimary
                                        )
                                        Text(
                                            text = "${r.storeType.name} • ${r.cuisineOrCategory}",
                                            fontSize = 12.sp,
                                            color = PremiumTextSecondary
                                        )
                                    }
                                    Surface(
                                        color = when (r.status) {
                                            VendorStatus.APPROVED -> PremiumEmerald.copy(alpha = 0.2f)
                                            VendorStatus.PENDING -> PremiumGoldPrimary.copy(alpha = 0.2f)
                                            else -> PremiumCrimson.copy(alpha = 0.2f)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = r.status.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when (r.status) {
                                                VendorStatus.APPROVED -> PremiumEmeraldLight
                                                VendorStatus.PENDING -> PremiumGoldLight
                                                else -> PremiumCrimson
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("License: ${r.foodLicense} • Bank: ${r.bankAccount}", fontSize = 11.sp, color = PremiumTextSecondary)
                                Text("Address: ${r.address}", fontSize = 11.sp, color = PremiumTextTertiary)

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = PremiumBorderStroke)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (r.status != VendorStatus.APPROVED) {
                                        Button(
                                            onClick = { viewModel.updateRestaurantStatus(r.id, VendorStatus.APPROVED) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PremiumEmerald),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve KYC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                    if (r.status != VendorStatus.SUSPENDED) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.updateRestaurantStatus(r.id, VendorStatus.SUSPENDED) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumCrimson),
                                            border = BorderStroke(1.dp, PremiumCrimson),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Suspend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Tab 2: Delivery Fleet Partner KYC
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_drivers_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allDeliveryPartners, key = { it.id }) { dp ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PremiumCardSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PremiumBorderStroke),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = dp.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = PremiumTextPrimary
                                        )
                                        Text(
                                            text = "${dp.vehicleType} • ${dp.vehicleNumber}",
                                            fontSize = 12.sp,
                                            color = PremiumTextSecondary
                                        )
                                    }
                                    Surface(
                                        color = if (dp.kycStatus == DeliveryPartnerStatus.APPROVED) PremiumEmerald.copy(alpha = 0.2f) else PremiumGoldPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = dp.kycStatus.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (dp.kycStatus == DeliveryPartnerStatus.APPROVED) PremiumEmeraldLight else PremiumGoldLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("DL License: ${dp.licenseNumber} • Phone: ${dp.phone}", fontSize = 11.sp, color = PremiumTextSecondary)
                                Text("Wallet: ₹${dp.walletBalance.toInt()} • Lifetime Deliveries: ${dp.totalDeliveries}", fontSize = 11.sp, color = PremiumTextTertiary)

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = PremiumBorderStroke)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    if (dp.kycStatus != DeliveryPartnerStatus.APPROVED) {
                                        Button(
                                            onClick = { viewModel.updateDriverStatus(dp.id, DeliveryPartnerStatus.APPROVED) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PremiumEmerald),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Approve Driver", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                        }
                                    }
                                    if (dp.kycStatus != DeliveryPartnerStatus.SUSPENDED) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedButton(
                                            onClick = { viewModel.updateDriverStatus(dp.id, DeliveryPartnerStatus.SUSPENDED) },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PremiumCrimson),
                                            border = BorderStroke(1.dp, PremiumCrimson),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Text("Suspend", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Tab 3: Global Platform Commission & Fee Engine
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PremiumCardSurface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, PremiumBorderStroke),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Text(
                                    text = "Monetization & Commission Engine",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PremiumGoldLight
                                )
                                Text(
                                    text = "Configure marketplace revenue rates and customer delivery tariffs",
                                    fontSize = 12.sp,
                                    color = PremiumTextSecondary
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                val textFieldColors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PremiumGoldPrimary,
                                    focusedLabelColor = PremiumGoldLight,
                                    unfocusedBorderColor = PremiumBorderStroke,
                                    unfocusedLabelColor = PremiumTextSecondary,
                                    focusedTextColor = PremiumTextPrimary,
                                    unfocusedTextColor = PremiumTextPrimary,
                                    cursorColor = PremiumGoldPrimary
                                )

                                OutlinedTextField(
                                    value = commRate,
                                    onValueChange = { commRate = it },
                                    label = { Text("Vendor Commission (%)") },
                                    colors = textFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = baseFee,
                                    onValueChange = { baseFee = it },
                                    label = { Text("Base Customer Delivery Fee (₹)") },
                                    colors = textFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = platformFeeVal,
                                    onValueChange = { platformFeeVal = it },
                                    label = { Text("Platform Convenience Fee (₹)") },
                                    colors = textFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = freeThresholdVal,
                                    onValueChange = { freeThresholdVal = it },
                                    label = { Text("Free Delivery Threshold (₹)") },
                                    colors = textFieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(20.dp))
                                Button(
                                    onClick = {
                                        val c = commRate.toDoubleOrNull() ?: 15.0
                                        val b = baseFee.toDoubleOrNull() ?: 25.0
                                        val p = platformFeeVal.toDoubleOrNull() ?: 5.0
                                        val f = freeThresholdVal.toDoubleOrNull() ?: 499.0
                                        viewModel.updatePlatformSettings(c, b, 8.0, p, f, "₹")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PremiumGoldPrimary),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("save_fee_settings_button")
                                ) {
                                    Text("SAVE CONFIGURATION", fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Tab 4: Promotional Coupons Management
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_coupons_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddCouponDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumGoldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("add_coupon_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Promo Code / Coupon", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    items(allCoupons, key = { it.id }) { coupon ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PremiumCardSurface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, PremiumBorderStroke),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = PremiumGoldPrimary.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, PremiumGoldPrimary)
                                    ) {
                                        Text(
                                            text = coupon.code,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = PremiumGoldLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(coupon.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = PremiumTextPrimary)
                                    Text(
                                        text = "${coupon.discountPercent}% off up to ₹${coupon.maxDiscount.toInt()} • Min order ₹${coupon.minOrderAmount.toInt()}",
                                        fontSize = 11.sp,
                                        color = PremiumTextSecondary
                                    )
                                }

                                IconButton(onClick = { viewModel.deleteCoupon(coupon.id) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = PremiumCrimson)
                                }
                            }
                        }
                    }
                }
            }

            5 -> {
                // Tab 5: Support Tickets & Dispute Arbitration
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_tickets_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(allTickets, key = { it.id }) { ticket ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PremiumCardSurface),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, PremiumBorderStroke),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ticket.subject,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = PremiumTextPrimary
                                    )
                                    Surface(
                                        color = if (ticket.status == TicketStatus.RESOLVED) PremiumEmerald.copy(alpha = 0.2f) else PremiumGoldPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = ticket.status.name,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (ticket.status == TicketStatus.RESOLVED) PremiumEmeraldLight else PremiumGoldLight,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("From: ${ticket.userName} (${ticket.userRole.name})", fontSize = 11.sp, color = PremiumTextSecondary)
                                Text(ticket.message, fontSize = 12.sp, color = PremiumTextPrimary, modifier = Modifier.padding(vertical = 6.dp))

                                if (ticket.adminReply != null) {
                                    Surface(
                                        color = PremiumCardElevated,
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, PremiumBorderStroke),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = "Staff Reply: ${ticket.adminReply}",
                                            fontSize = 11.sp,
                                            color = PremiumEmeraldLight,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            replyingTicket = ticket
                                            ticketReplyText = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGoldPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.align(Alignment.End).padding(top = 6.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Black)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Respond & Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            6 -> {
                // Tab 6: Audit Trail
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("admin_logs_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(adminLogs, key = { it.id }) { log ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PremiumCardSurface),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, PremiumBorderStroke)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.action,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = PremiumGoldLight
                                    )
                                    val dateStr = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.US).format(Date(log.timestamp))
                                    Text(dateStr, fontSize = 10.sp, color = PremiumTextTertiary)
                                }
                                Text(
                                    text = log.details,
                                    fontSize = 12.sp,
                                    color = PremiumTextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                Text(
                                    text = "By: ${log.adminName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PremiumEmeraldLight,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Add Coupon Dialog
        if (showAddCouponDialog) {
            AlertDialog(
                onDismissRequest = { showAddCouponDialog = false },
                containerColor = PremiumCardSurface,
                title = { Text("Create Promotional Coupon", color = PremiumGoldLight, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        OutlinedTextField(
                            value = couponCode,
                            onValueChange = { couponCode = it },
                            label = { Text("Coupon Code (e.g. FLASH50)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = couponTitle,
                            onValueChange = { couponTitle = it },
                            label = { Text("Offer Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = couponDesc,
                            onValueChange = { couponDesc = it },
                            label = { Text("Short Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = couponPercent,
                                onValueChange = { couponPercent = it },
                                label = { Text("Discount %") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = couponMax,
                                onValueChange = { couponMax = it },
                                label = { Text("Max Cap (₹)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = couponMinOrder,
                                onValueChange = { couponMinOrder = it },
                                label = { Text("Min Order") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val p = couponPercent.toIntOrNull() ?: 20
                            val m = couponMax.toDoubleOrNull() ?: 100.0
                            val min = couponMinOrder.toDoubleOrNull() ?: 299.0
                            if (couponCode.isNotBlank()) {
                                viewModel.addCoupon(couponCode, couponTitle, couponDesc, p, m, min)
                                showAddCouponDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumGoldPrimary),
                        modifier = Modifier.testTag("save_coupon_button")
                    ) {
                        Text("Create Coupon", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddCouponDialog = false }) {
                        Text("Cancel", color = PremiumTextSecondary)
                    }
                }
            )
        }

        // Support Reply Dialog
        if (replyingTicket != null) {
            AlertDialog(
                onDismissRequest = { replyingTicket = null },
                containerColor = PremiumCardSurface,
                title = { Text("Respond to Ticket #${replyingTicket!!.id}", color = PremiumGoldLight, fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Subject: ${replyingTicket!!.subject}", fontWeight = FontWeight.Bold, color = PremiumTextPrimary)
                        Text(replyingTicket!!.message, fontSize = 12.sp, color = PremiumTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ticketReplyText,
                            onValueChange = { ticketReplyText = it },
                            label = { Text("Resolution Message") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (ticketReplyText.isNotBlank()) {
                                viewModel.replySupportTicket(replyingTicket!!.id, ticketReplyText, TicketStatus.RESOLVED)
                                replyingTicket = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PremiumEmerald),
                        modifier = Modifier.testTag("submit_ticket_resolution")
                    ) {
                        Text("Send & Mark Resolved", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { replyingTicket = null }) {
                        Text("Cancel", color = PremiumTextSecondary)
                    }
                }
            )
        }
    }
}
