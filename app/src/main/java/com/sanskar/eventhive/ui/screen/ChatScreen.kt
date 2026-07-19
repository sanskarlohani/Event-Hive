package com.sanskar.eventhive.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sanskar.eventhive.data.model.RtdbChatMessage
import com.sanskar.eventhive.presentation.chat.ChatViewModel
import com.sanskar.eventhive.presentation.chat.MessageUiState
import com.sanskar.eventhive.presentation.common.EmptyScreen
import com.sanskar.eventhive.presentation.common.ErrorScreen
import com.sanskar.eventhive.presentation.common.LoadingScreen
import com.sanskar.eventhive.ui.components.ClayCard
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
    val currentAnonAlias by viewModel.currentAnonAlias.collectAsStateWithLifecycle()
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

    val roomTitle = when (val state = messagesState) {
        is MessageUiState.Success -> state.roomMetadata.name.ifBlank { roomId }
        else -> roomId
    }

    ChatScreenContent(
        roomTitle = roomTitle,
        messagesState = messagesState,
        inputText = inputText,
        isLoadingMore = isLoadingMore,
        listState = listState,
        currentUid = viewModel.currentUid(),
        currentAnonAlias = currentAnonAlias,
        canDelete = { viewModel.canDelete(it) },
        onBack = onBack,
        onInputChange = onInputChange,
        onSend = onSend,
    ) { viewModel.deleteMessage(it) }
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
    val currentAnonAlias by viewModel.currentAnonAlias.collectAsStateWithLifecycle()
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
                title = { 
                    Text(
                        "Anonymous Chat", 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type anonymously...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    IconButton(
                        onClick = onSend,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ClayCard(
                cornerRadius = 16.dp,
                elevation = 6.dp,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(
                        "You are anonymous. Other students cannot see your identity.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            ChatList(
                messagesState = messagesState,
                listState = listState,
                currentUid = "anon",
                currentAnonAlias = currentAnonAlias,
                isLoadingMore = isLoadingMore,
                canDelete = { viewModel.canDelete(it) },
                onDelete = { viewModel.deleteMessage(it) },
                onRetry = onSend
            )
        }
    }
}

@Composable
private fun ChatList(
    messagesState: MessageUiState,
    listState: androidx.compose.foundation.lazy.LazyListState,
    currentUid: String,
    currentAnonAlias: String?,
    isLoadingMore: Boolean,
    canDelete: (RtdbChatMessage) -> Boolean,
    onDelete: (String) -> Unit,
    onRetry: () -> Unit
) {
    when (messagesState) {
        is MessageUiState.Loading -> LoadingScreen()
        is MessageUiState.Error -> ErrorScreen(messagesState.message, onRetry)
        is MessageUiState.Success -> {
            val messages = messagesState.messages
            if (messages.isEmpty()) {
                EmptyScreen("No messages yet. Say hello!")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    state = listState,
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(messages, key = { it.id }) { message ->
                        val ownMessage = message.senderId == currentUid || (currentUid == "anon" && message.senderId == "anon" && message.displayName == currentAnonAlias)
                        ChatBubble(
                            message = message,
                            isOwn = ownMessage,
                            onLongClick = {
                                if (canDelete(message)) {
                                    onDelete(message.id)
                                }
                            }
                        )
                    }
                    item {
                        if (isLoadingMore) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
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
    currentAnonAlias: String?,
    canDelete: (RtdbChatMessage) -> Boolean,
    onBack: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onDelete: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = roomTitle, 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = onInputChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    IconButton(
                        onClick = onSend,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ChatList(
                messagesState = messagesState,
                listState = listState,
                currentUid = currentUid,
                currentAnonAlias = currentAnonAlias,
                isLoadingMore = isLoadingMore,
                canDelete = canDelete,
                onDelete = onDelete,
                onRetry = onSend
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatBubble(
    message: RtdbChatMessage,
    isOwn: Boolean,
    onLongClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
    ) {
        if (!isOwn) {
            Text(
                text = message.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }
        
        ClayCard(
            cornerRadius = 16.dp,
            elevation = 4.dp,
            backgroundColor = if (isOwn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = {},
                    onLongClick = onLongClick
                )
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isOwn) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(8.dp)
            )
        }
        
        val time = remember(message.timestamp) {
            java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(message.timestamp))
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}
