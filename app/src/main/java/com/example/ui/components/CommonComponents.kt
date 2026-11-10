package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AdminPurple
import com.example.ui.theme.DeliveryBlue
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.SecondaryTeal
import com.example.ui.theme.VegGreen

@Composable
fun RoleSwitcherBar(
    currentRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PORTAL:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            UserRole.values().forEach { role ->
                val isSelected = role == currentRole
                val roleColor = when (role) {
                    UserRole.CUSTOMER -> SecondaryTeal
                    UserRole.RESTAURANT_OWNER -> PrimaryOrange
                    UserRole.RESTAURANT_STAFF -> AccentAmber
                    UserRole.DELIVERY_PARTNER -> DeliveryBlue
                    UserRole.ADMIN_STAFF -> AdminPurple
                    UserRole.SUPER_ADMIN -> Color(0xFFFFB703)
                }

                Surface(
                    onClick = { onRoleSelected(role) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) roleColor else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.testTag("role_chip_${role.name.lowercase()}")
                ) {
                    Text(
                        text = if (role == UserRole.SUPER_ADMIN) "👑 ${role.displayName}" else role.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                        color = when {
                            isSelected && role == UserRole.SUPER_ADMIN -> Color.Black
                            isSelected -> Color.White
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun TopMarketplaceHeader(
    title: String,
    subtitle: String,
    currentRole: UserRole,
    notificationCount: Int = 0,
    onRoleSwitchClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    onLoginClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val isAdmin = currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.ADMIN_STAFF
    Surface(
        color = if (isAdmin) Color(0xFF101726) else MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onLocationClick() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isAdmin) Icons.Default.Star else Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = if (isAdmin) Color(0xFFFFB703) else PrimaryOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isAdmin) Color(0xFFF8FAFC) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Location",
                        tint = if (isAdmin) Color(0xFFFFD166) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isAdmin) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Prominent Login Button
                Button(
                    onClick = onLoginClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("login_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Login Button",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "LOGIN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }

                // Prominent Logout Button
                OutlinedButton(
                    onClick = onLogoutClick,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Logout Button",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "LOGOUT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                }

                Box {
                    IconButton(
                        onClick = onNotificationClick,
                        modifier = Modifier.testTag("notifications_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (notificationCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 6.dp, end = 6.dp)
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(PrimaryOrange),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (notificationCount > 9) "9+" else notificationCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRoleSwitchClick,
                    modifier = Modifier.testTag("role_switch_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SwitchAccount,
                        contentDescription = "Switch Portal",
                        tint = when (currentRole) {
                            UserRole.CUSTOMER -> SecondaryTeal
                            UserRole.RESTAURANT_OWNER -> PrimaryOrange
                            UserRole.DELIVERY_PARTNER -> DeliveryBlue
                            else -> AdminPurple
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: OrderStatus) {
    val (bgColor, textColor) = when (status) {
        OrderStatus.PLACED -> Pair(Color(0xFFFFF3E0), Color(0xFFE65100))
        OrderStatus.RESTAURANT_ACCEPTED -> Pair(Color(0xFFE1F5FE), Color(0xFF0277BD))
        OrderStatus.PREPARING -> Pair(Color(0xFFEDE7F6), Color(0xFF512DA8))
        OrderStatus.READY_FOR_PICKUP -> Pair(Color(0xFFE0F7FA), Color(0xFF00695C))
        OrderStatus.DRIVER_ASSIGNED -> Pair(Color(0xFFE8EAF6), Color(0xFF283593))
        OrderStatus.OUT_FOR_DELIVERY -> Pair(Color(0xFFFFF8E1), Color(0xFFF57F17))
        OrderStatus.DELIVERED -> Pair(Color(0xFFE8F5E9), Color(0xFF2E7D32))
        OrderStatus.CANCELLED -> Pair(Color(0xFFFFEBEE), Color(0xFFC62828))
        OrderStatus.REFUND_REQUESTED -> Pair(Color(0xFFFBE9E7), Color(0xFFD84315))
        OrderStatus.REFUNDED -> Pair(Color(0xFFE0F2F1), Color(0xFF00796B))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun RatingPill(rating: Double, reviewCount: Int? = null) {
    Surface(
        color = Color(0xFF2E7D32),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%.1f", rating),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            if (reviewCount != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "($reviewCount)",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
fun VegNonVegBadge(isVeg: Boolean) {
    val borderColor = if (isVeg) VegGreen else NonVegRed
    Box(
        modifier = Modifier
            .size(16.dp)
            .border(1.5.dp, borderColor, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(borderColor)
        )
    }
}

@Composable
fun MetricStatCard(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color? = null,
    titleColor: Color? = null,
    subtitleColor: Color? = null
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = borderColor?.let { BorderStroke(1.dp, it) },
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = titleColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor ?: MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LiveDeliveryMapSimulation(
    progressPercent: Float = 0.65f,
    riderName: String = "Carlos Vance",
    restaurantName: String = "Bella Napoli Woodfire",
    customerAddress: String = "742 Evergreen Terrace",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Draw map roads / grid
                val roadPaint = Color(0xFFE2E8F0)
                drawLine(roadPaint, Offset(0f, h * 0.3f), Offset(w, h * 0.3f), strokeWidth = 14f)
                drawLine(roadPaint, Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 14f)
                drawLine(roadPaint, Offset(w * 0.25f, 0f), Offset(w * 0.25f, h), strokeWidth = 14f)
                drawLine(roadPaint, Offset(w * 0.75f, 0f), Offset(w * 0.75f, h), strokeWidth = 14f)

                // Route Path from Restaurant to Customer
                val p1 = Offset(w * 0.15f, h * 0.7f)
                val p2 = Offset(w * 0.25f, h * 0.7f)
                val p3 = Offset(w * 0.25f, h * 0.3f)
                val p4 = Offset(w * 0.75f, h * 0.3f)
                val p5 = Offset(w * 0.85f, h * 0.3f)

                val routePath = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    lineTo(p5.x, p5.y)
                }

                // Dotted route line
                drawPath(
                    path = routePath,
                    color = Color(0xFF3B82F6),
                    style = Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f),
                        cap = StrokeCap.Round
                    )
                )

                // Restaurant Pin (Start)
                drawCircle(color = Color(0xFFFF5722), radius = 14f, center = p1)
                drawCircle(color = Color.White, radius = 6f, center = p1)

                // Customer Pin (Destination)
                drawCircle(color = Color(0xFF10B981), radius = 14f, center = p5)
                drawCircle(color = Color.White, radius = 6f, center = p5)

                // Moving Delivery Rider Marker along progress
                val riderPos = Offset(
                    x = p3.x + (p4.x - p3.x) * progressPercent,
                    y = p3.y
                )

                // Pulse aura
                drawCircle(
                    color = Color(0xFF3B82F6).copy(alpha = 0.35f),
                    radius = pulseRadius,
                    center = riderPos
                )
                drawCircle(color = Color(0xFF1E40AF), radius = 16f, center = riderPos)
                drawCircle(color = Color.White, radius = 7f, center = riderPos)
            }

            // Overlay Cards on top of simulated map
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3B82F6))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live GPS: $riderName en route",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "📍 $restaurantName",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFF334155)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.92f),
                        shape = RoundedCornerShape(8.dp),
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "🏁 Destination",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFF334155)
                        )
                    }
                }
            }
        }
    }
}
