package com.example.ui.customer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpCenter
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.TicketStatus
import com.example.ui.theme.PrimaryOrange
import com.example.ui.theme.VegGreen
import com.example.ui.viewmodel.MarketplaceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportAndLegalScreen(
    viewModel: MarketplaceViewModel,
    preselectedOrderId: Long? = null,
    modifier: Modifier = Modifier
) {
    val allTickets by viewModel.allSupportTickets.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showNewTicketDialog by remember { mutableStateOf(preselectedOrderId != null) }
    var newTicketSubject by remember { mutableStateOf(if (preselectedOrderId != null) "Order #$preselectedOrderId Issue" else "") }
    var newTicketMessage by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize()) {
        SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("My Tickets (${allTickets.size})", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("FAQs", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Legal & Policy", fontWeight = FontWeight.Bold) }
            )
        }

        when (selectedTabIndex) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("support_tickets_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Button(
                            onClick = { showNewTicketDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().testTag("raise_ticket_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Raise New Support Ticket", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (allTickets.isEmpty()) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.QuestionAnswer, contentDescription = null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No support tickets raised yet", fontWeight = FontWeight.Bold)
                                    Text("If you encounter any order or payment issues, submit a ticket above.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(allTickets, key = { it.id }) { ticket ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(ticket.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Surface(
                                            color = if (ticket.status == TicketStatus.RESOLVED) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = ticket.status.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (ticket.status == TicketStatus.RESOLVED) VegGreen else PrimaryOrange,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(ticket.message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    if (ticket.adminReply != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = VegGreen, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.size(4.dp))
                                                    Text("OmniFeast Support Reply:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                                Text(ticket.adminReply, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        FaqCard(
                            question = "How does 15-minute grocery delivery work?",
                            answer = "OmniFeast partners with local dark stores and hyper-local grocery supermarkets positioned within a 3-5 km radius, allowing prompt pick, pack, and electric rider dispatch within 15 minutes."
                        )
                    }
                    item {
                        FaqCard(
                            question = "What is the Delivery OTP?",
                            answer = "To ensure secure and contactless handover, each customer order generates a private 4-digit OTP. Only share this OTP with the rider once they arrive with your items."
                        )
                    }
                    item {
                        FaqCard(
                            question = "Can I cancel an order once placed?",
                            answer = "Yes! You can cancel anytime before the restaurant marks the order as 'Ready for Pickup' or 'Out for Delivery'. If payment was deducted, refunds are processed back to the original payment method within 2-4 business hours."
                        )
                    }
                    item {
                        FaqCard(
                            question = "How are vendor commissions calculated?",
                            answer = "The platform sets a transparent percentage commission (default 15%) on food & grocery menu items. Payouts and settlements are released weekly directly to registered bank accounts."
                        )
                    }
                }
            }

            2 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        LegalCard(
                            title = "OmniFeast Terms of Service",
                            content = "By accessing the OmniFeast platform as a customer, vendor, or delivery partner, you agree to fair usage, zero tolerance for food safety adulteration, compliant KYC submissions, and genuine order transactions."
                        )
                    }
                    item {
                        LegalCard(
                            title = "Refund & Cancellation Policy",
                            content = "Customers are entitled to 100% full refund if an order is cancelled prior to restaurant preparation, if items are demonstrably missing, or if delivery is severely delayed due to platform failure."
                        )
                    }
                    item {
                        LegalCard(
                            title = "Food Safety & FSSAI Standards",
                            content = "All registered restaurant kitchens and grocery store vendors must hold a valid regulatory food business license (e.g. FSSAI) and maintain hygienic preparation conditions."
                        )
                    }
                    item {
                        LegalCard(
                            title = "Privacy & Data Protection",
                            content = "Customer location coordinates and phone numbers are encrypted and masked when shared with delivery personnel for the sole purpose of delivery fulfillment."
                        )
                    }
                }
            }
        }

        // New Ticket Dialog
        if (showNewTicketDialog) {
            AlertDialog(
                onDismissRequest = { showNewTicketDialog = false },
                title = { Text("Raise Support Ticket") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newTicketSubject,
                            onValueChange = { newTicketSubject = it },
                            label = { Text("Subject") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTicketMessage,
                            onValueChange = { newTicketMessage = it },
                            label = { Text("Describe the issue in detail") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newTicketSubject.isNotBlank() && newTicketMessage.isNotBlank()) {
                                viewModel.createSupportTicket(newTicketSubject, newTicketMessage, preselectedOrderId)
                                showNewTicketDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
                        modifier = Modifier.testTag("submit_ticket_button")
                    ) {
                        Text("Submit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNewTicketDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}

@Composable
fun FaqCard(question: String, answer: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = question, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = answer, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun LegalCard(title: String, content: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Gavel, contentDescription = null, tint = PrimaryOrange, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
