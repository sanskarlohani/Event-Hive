package com.sanskar.eventhive.ui.screen.User

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.settings.formatTimestamp
import com.sanskar.eventhive.ui.components.ClayCard
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.ui.viewModel.TicketViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTicketScreen(
    navController: NavController,
    userId: String,
    userViewModel: UserViewModel = hiltViewModel(),
    ticketViewModel: TicketViewModel = hiltViewModel(),
) {
    val tickets by ticketViewModel.userTickets.collectAsStateWithLifecycle()
    val userResource by userViewModel.observeUser.collectAsStateWithLifecycle(initialValue = Resource.Loading)
    val currentUserId = remember(userId, userResource) {
        userId.ifBlank { (userResource as? Resource.Success)?.data?.userId.orEmpty() }
    }
    val userOwnedTickets = remember(tickets, currentUserId) {
        if (currentUserId.isBlank()) {
            emptyList()
        } else {
            tickets.asSequence()
                .filter { ticket ->
                    (ticket.userId == currentUserId && ticket.userId.isNotBlank()) || 
                    (ticket.participantIds.contains(currentUserId) && currentUserId.isNotBlank())
                }
                .sortedByDescending { it.issuedAt }
                .toList()
        }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            ticketViewModel.getUserTickets(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tickets") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            if (userOwnedTickets.isEmpty()) {
                Text(
                    text = "No tickets found",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 16.sp
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(userOwnedTickets) { ticket ->
                        TicketCard(ticket) {
                            navController.navigate(
                                NavigationItem.UserTicketDetail.createRoute(
                                    currentUserId, ticket.ticketId
                                )
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun TicketCard(ticket: Ticket, onClick: () -> Unit) {
    ClayCard(
        cornerRadius = 24.dp,
        elevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Text(
                text = ticket.eventId.ifEmpty { "Event" },
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = formatTimestamp(ticket.issuedAt),
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, 
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = ticket.status.name,
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Text(
                    text = "ID: ${ticket.ticketId.take(8).uppercase()}",
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}


