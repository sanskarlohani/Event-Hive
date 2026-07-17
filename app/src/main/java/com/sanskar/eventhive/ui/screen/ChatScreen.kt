package com.sanskar.eventhive.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.model.RtdbChatMessage
import com.sanskar.eventhive.presentation.chat.ChatViewModel
import com.sanskar.eventhive.presentation.chat.MessageUiState
import com.sanskar.eventhive.presentation.common.EmptyScreen
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun GroupOrDmChatScreen(
    navController: NavController,
    roomId: String,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(roomId) {
        viewModel.openRoom(roomId)
    }

    LaunchedEffect(listState, roomId) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { it < 3 }
            .filter { it }
            .distinctUntilChanged()
            .collect {
                viewModel.loadOlderMessages()
            }
    }

    val onBack = remember(navController) { { navController.popBackStack(); Unit } }
    val onInputChange = remember(viewModel) { { value: String -> viewModel.inputText.value = value } }
    val onSend = remember(viewModel, inputText) { { viewModel.sendMessage(inputText) } }

    ChatScreenContent(
        roomTitle = roomId,
        messagesState = messagesState,
        inputText = inputText,
        isLoadingMore = isLoadingMore,
        listState = listState,
        currentUid = viewModel.currentUid(),
        canDelete = { viewModel.canDelete(it) },
        onBack = onBack,
        onInputChange = onInputChange,
        onSend = onSend,
        onDelete = { viewModel.deleteMessage(it) },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun AnonymousChatScreen(
    navController: NavController,
    collegeId: String,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val roomId = "anon_$collegeId"
    val messagesState by viewModel.messagesState.collectAsStateWithLifecycle()
    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(roomId) {
        viewModel.openRoom(roomId)
    }

    LaunchedEffect(listState, roomId) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .map { it < 3 }
            .filter { it }
            .distinctUntilChanged()
            .collect {
                viewModel.loadOlderMessages()
            }
    }

    val onBack = remember(navController) { { navController.popBackStack(); Unit } }
    val onInputChange = remember(viewModel) { { value: String -> viewModel.inputText.value = value } }
    val onSend = remember(viewModel, inputText) { { viewModel.sendMessage(inputText) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Anonymous Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type anonymously") },
                )
                IconButton(onClick = onSend) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null)
                    Text("You are anonymous. Other students cannot see your identity.")
                }
            }
            when (messagesState) {
                is MessageUiState.Loading -> LoadingScreen()
                is MessageUiState.Error -> ErrorScreen((messagesState as MessageUiState.Error).message, onSend)
                is MessageUiState.Success -> {
                    val messages = (messagesState as MessageUiState.Success).messages
                    if (messages.isEmpty()) {
                        EmptyScreen("No messages yet")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            state = listState,
                            reverseLayout = true,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(messages, key = { it.id }) { message ->
                                val onLongClickDelete = remember(viewModel, message.id) {
                                    { if (viewModel.canDelete(message)) viewModel.deleteMessage(message.id) }
                                }
                                Text(
                                    text = "${message.displayName}: ${message.text}",
                                    modifier = Modifier.combinedClickable(
                                        onClick = {},
                                        onLongClick = onLongClickDelete,
                                    ),
                                )
                            }
                            item {
                                if (isLoadingMore) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ChatScreenContent(
    roomTitle: String,
    messagesState: MessageUiState,
    inputText: String,
    isLoadingMore: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    currentUid: String,
    canDelete: (RtdbChatMessage) -> Boolean,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onDelete: (String) -> Unit,
) {
    val onRetry = remember(onSend) { onSend }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(roomTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message") },
                )
                IconButton(onClick = onSend) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                }
            }
        },
    ) { padding ->
        when (messagesState) {
            is MessageUiState.Loading -> LoadingScreen()
            is MessageUiState.Error -> ErrorScreen(messagesState.message, onRetry)
            is MessageUiState.Success -> {
                if (messagesState.messages.isEmpty()) {
                    EmptyScreen("No messages yet")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 12.dp),
                        state = listState,
                        reverseLayout = true,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(messagesState.messages, key = { it.id }) { message ->
                            val ownMessage = message.senderId == currentUid || (message.senderId == "anon" && message.displayName.isNotBlank())
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (ownMessage) Arrangement.End else Arrangement.Start,
                            ) {
                                Text(
                                    text = "${message.displayName}: ${message.text}",
                                    modifier = Modifier.combinedClickable(
                                        onClick = {},
                                        onLongClick = {
                                            if (canDelete(message)) {
                                                onDelete(message.id)
                                            }
                                        },
                                    ),
                                )
                            }
                        }
                        item {
                            if (isLoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
