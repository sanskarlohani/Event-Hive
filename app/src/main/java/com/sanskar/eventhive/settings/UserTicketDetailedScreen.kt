package com.sanskar.eventhive.settings

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.Resource
import com.sanskar.eventhive.ui.viewModel.EventViewModel
import com.sanskar.eventhive.ui.viewModel.TicketViewModel
import com.sanskar.eventhive.ui.viewModel.UserViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserTicketDetailedScreen(
    userId: String,
    ticketId: String,
    navController: NavController,
    ticketViewModel: TicketViewModel = hiltViewModel(),
    eventViewModel: EventViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    fun userLabel(user: com.sanskar.eventhive.data.model.User): String {
        val descriptor = listOfNotNull(
            user.course.takeIf { it.isNotBlank() },
            user.yearOfJoining.takeIf { it.isNotBlank() },
            user.yearOfPassing.takeIf { it.isNotBlank() },
        ).joinToString(" | ")
        return if (descriptor.isBlank()) user.name else "${user.name} ($descriptor)"
    }

    val ticket by ticketViewModel.singleUserTicket.collectAsStateWithLifecycle()
    val team by ticketViewModel.team.collectAsStateWithLifecycle()
    val event by eventViewModel.event.collectAsStateWithLifecycle()
    val allUsersRes by userViewModel.allUsers.collectAsStateWithLifecycle()
    val usersById = remember(allUsersRes) {
        ((allUsersRes as? Resource.Success)?.data ?: emptyList()).associateBy { it.userId }
    }

    LaunchedEffect(Unit) {
        ticketViewModel.getSingleUserTicket(userId, ticketId)
    }
    LaunchedEffect(ticket?.ticketId, ticket?.teamId) {
        val currentTicket = ticket ?: return@LaunchedEffect
        if (currentTicket.ticketId.isNotBlank() && currentTicket.teamId.isNotBlank()) {
            ticketViewModel.getTeamForTicket(currentTicket.ticketId, currentTicket.teamId)
        }
    }
    LaunchedEffect(ticket?.categoryId, ticket?.clubId, ticket?.eventId) {
        val currentTicket = ticket ?: return@LaunchedEffect
        if (
            currentTicket.categoryId.isNotBlank() &&
            currentTicket.clubId.isNotBlank() &&
            currentTicket.eventId.isNotBlank()
        ) {
            eventViewModel.getEvent(currentTicket.categoryId, currentTicket.clubId, currentTicket.eventId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            ticket?.let { it1 ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = event?.title?.ifBlank { null }
                            ?: team?.eventName?.ifBlank { null }
                            ?: ticket!!.eventId.ifEmpty { "Event Name" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    // Generate QR code from ticket details
                    val qrData = "TicketID: ${ticket!!.ticketId}, EventID: ${ticket!!.eventId}, Category: ${ticket!!.categoryId}"
                    val qrBitmap = generateQrCodeBitmap(qrData)
                    qrBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp))
                        )
                    }
                    InfoRow("Ticket ID", ticket!!.ticketId)
                    InfoRow("Team Name", team?.teamName?.ifBlank { "N/A" } ?: "N/A")
                    InfoRow("Category", ticket!!.categoryId)
                    InfoRow("Issued At", formatTimestamp(ticket!!.issuedAt))
                    val statusColor = when (ticket!!.status) {
                        com.sanskar.eventhive.data.model.RegistrationStatus.CANCELLED -> ComposeColor(0xFFD32F2F)
                        com.sanskar.eventhive.data.model.RegistrationStatus.CLAIMED -> ComposeColor(0xFF2E7D32)
                        else -> ComposeColor.Unspecified
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Status", fontWeight = FontWeight.Medium)
                        Text(ticket!!.status.name, fontWeight = FontWeight.SemiBold, color = statusColor)
                    }
                    val memberIds = when {
                        team?.teamMemberIds?.isNotEmpty() == true -> team!!.teamMemberIds
                        ticket!!.participantIds.isNotEmpty() -> ticket!!.participantIds
                        else -> emptyList()
                    }
                    if (memberIds.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Members", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                        memberIds.forEach { memberId ->
                            val label = team?.teamMemberNames?.get(memberId)
                                ?.takeIf { it.isNotBlank() && it != memberId }
                                ?: usersById[memberId]?.let(::userLabel)
                                ?: memberId
                            Text(
                                text = "• $label",
                                modifier = Modifier.fillMaxWidth(),
                                fontSize = 14.sp
                            )
                        }
                    }
                    ticket!!.redeemedAt?.let {
                        InfoRow("Redeemed At", formatTimestamp(it.seconds * 1000))
                    }
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (ticket!!.valid) "Share Ticket" else "Invalid Ticket")
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

fun formatTimestamp(timeInMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(timeInMillis))
}

fun generateQrCodeBitmap(data: String, size: Int = 512): Bitmap? {
    return try {
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}
