package com.sanskar.eventhive.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.R
import com.sanskar.eventhive.data.model.ChatRoomPreview
import com.sanskar.eventhive.presentation.chat.ChatViewModel
import com.sanskar.eventhive.presentation.chat.UiState
import com.sanskar.eventhive.ui.navigation.NavigationItem
import com.sanskar.eventhive.presentation.common.EmptyScreen
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen
import com.sanskar.eventhive.ui.components.BottomBarScaffold
import com.sanskar.eventhive.ui.components.ClayCard
import com.sanskar.eventhive.ui.components.ClayButton

private enum class ChatTab(val type: String?) {
    ALL(null),
    DMS("dm"),
    CLUBS("club"),
    EVENTS("event"),
    ANON("anonymous"),
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatHomeScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val roomsState by viewModel.roomsState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadRooms()
    }

    BottomBarScaffold(
        navController = navController,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.chat_title),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            when (val state = roomsState) {
                is UiState.Loading -> LoadingScreen()
                is UiState.Error -> ErrorScreen(state.message) { viewModel.loadRooms() }
                is UiState.Success -> {
                    val tabLabels = listOf(
                        "All",
                        stringResource(R.string.chat_tab_dms),
                        stringResource(R.string.chat_tab_clubs),
                        stringResource(R.string.chat_tab_events),
                        stringResource(R.string.chat_tab_anon),
                    )
                    val tab = ChatTab.entries[selectedTab]
                    val rooms = state.rooms.filter {
                        (tab.type == null) || (it.type == tab.type)
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedTab,
                            edgePadding = 16.dp,
                            containerColor = Color.Transparent,
                            divider = {},
                            indicator = {}
                        ) {
                            ChatTab.entries.forEachIndexed { idx, _ ->
                                Tab(
                                    selected = selectedTab == idx,
                                    onClick = { selectedTab = idx },
                                    text = { 
                                        val isSelected = selectedTab == idx
                                        ClayCard(
                                            cornerRadius = 12.dp,
                                            elevation = if (isSelected) 4.dp else 0.dp,
                                            backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        ) {
                                            Text(
                                                text = tabLabels[idx],
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                                ),
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                maxLines = 1
                                            )
                                        }
                                    },
                                )
                            }
                        }
                        
                        if (rooms.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                EmptyScreen(stringResource(R.string.chat_empty_rooms))
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                items(rooms, key = { it.id }) { room ->
                                    val onItemClick = remember(navController, room) {
                                        {
                                            if (room.type == "anonymous") {
                                                navController.navigate(
                                                    NavigationItem.ChatAnon.createRoute(room.roomId.removePrefix("anon_"))
                                                )
                                            } else {
                                                navController.navigate(
                                                    NavigationItem.ChatRoom.createRoute(room.roomId, false)
                                                )
                                            }
                                        }
                                    }
                                    ChatRoomRow(room = room, onClick = onItemClick)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatRoomRow(
    room: ChatRoomPreview,
    onClick: () -> Unit,
) {
    ClayCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        cornerRadius = 24.dp,
        elevation = 8.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .semantics(mergeDescendants = true) {
                    // Combine name and last message for screen readers
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = room.name
                        .split(" ")
                        .mapNotNull { it.firstOrNull()?.toString() }
                        .joinToString("")
                        .take(2)
                        .uppercase()
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column {
                    Text(
                        text = room.name, 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = room.lastMessage.ifBlank { "No messages yet" },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (room.timestamp == 0L) "-" else formatTimestamp(room.timestamp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                if (room.timestamp != 0L) {
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
