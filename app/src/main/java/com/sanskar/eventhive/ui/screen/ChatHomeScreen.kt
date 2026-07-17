package com.sanskar.eventhive.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.model.ChatRoomPreview
import com.sanskar.eventhive.presentation.chat.ChatViewModel
import com.sanskar.eventhive.presentation.chat.UiState
import com.sanskar.eventhive.presentation.common.EmptyScreen
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen
import com.sanskar.eventhive.ui.components.BottomBarScaffold

private enum class ChatTab(val label: String, val type: String?) {
    DMS("DMs", "dm"),
    CLUBS("Clubs", "club"),
    EVENTS("Events", "event"),
    ANON("Anonymous", "anonymous"),
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
        topBar = { TopAppBar(title = { Text("Chats") }) },
    ) { padding ->
        when (val state = roomsState) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error -> ErrorScreen(state.message) { viewModel.loadRooms() }
            is UiState.Success -> {
                val tab = ChatTab.entries[selectedTab]
                val rooms = state.rooms.filter {
                    tab.type == null || it.type == tab.type
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    TabRow(selectedTabIndex = selectedTab) {
                        ChatTab.entries.forEachIndexed { idx, item ->
                            Tab(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                text = { Text(item.label) },
                            )
                        }
                    }
                    if (rooms.isEmpty()) {
                        EmptyScreen("No chat rooms")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(rooms, key = { it.id }) { room ->
                                val onItemClick = remember(navController, room) {
                                    {
                                        if (room.type == "anonymous") {
                                            navController.navigate("chat/anon/${room.roomId.removePrefix("anon_")}")
                                        } else {
                                            navController.navigate("chat/${room.roomId}?isAnon=false")
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

@Composable
private fun ChatRoomRow(
    room: ChatRoomPreview,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(room.name)
            Text(room.lastMessage)
        }
        Text(text = if (room.timestamp == 0L) "-" else room.timestamp.toString())
    }
}
