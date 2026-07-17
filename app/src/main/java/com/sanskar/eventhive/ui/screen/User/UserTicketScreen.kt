package com.sanskar.eventhive.ui.screen.User

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.model.Ticket
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.settings.formatTimestamp
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
        if (userId.isNotBlank()) userId
        else (userResource as? Resource.Success)?.data?.userId.orEmpty()
    }
    val userOwnedTickets = remember(tickets, currentUserId) {
        if (currentUserId.isBlank()) emptyList()
        else tickets.filter { ticket ->
            ticket.userId == currentUserId || ticket.participantIds.contains(currentUserId)
        }.sortedByDescending { it.issuedAt }
    }

    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            ticketViewModel.getUserTickets(currentUserId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("My Tickets") }, navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            })
        }) { paddingValues ->
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
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = ticket.eventId.ifEmpty { "Event" },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatTimestamp(ticket.issuedAt),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = ticket.status.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Seat: ${ticket.categoryId}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


