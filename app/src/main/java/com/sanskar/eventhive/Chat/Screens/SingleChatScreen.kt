package com.sanskar.eventhive.Chat.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sanskar.eventhive.Chat.CommonDivider
import com.sanskar.eventhive.Chat.CommonImage
import com.sanskar.eventhive.Chat.Data.Message
import com.sanskar.eventhive.Chat.LCViewModel

@Composable
fun SingleChatScreen(navController: NavController, vm: LCViewModel, chatId: String) {

    var reply by rememberSaveable {
        mutableStateOf("")
    }

    val onSendReply = {
        vm.onSendReply(chatId, reply)
        reply = ""
    }
    val chatMessage = vm.chatMessages

    val myUser = vm.userData.value
    val currentChat = vm.chats.value.first { it.chatId == chatId }
    val chatUser =
        if (myUser?.userId == currentChat.user1.userId) currentChat.user2 else currentChat.user1

    // Scroll state for LazyColumn
    val listState = rememberLazyListState()

    LaunchedEffect(chatMessage.value) {
        // Automatically scroll to the bottom when a new message is added
        if (chatMessage.value.isNotEmpty()) {
            listState.animateScrollToItem(chatMessage.value.size - 1)
        }
    }

    LaunchedEffect(Unit) {
        vm.populateMessage(chatId)
    }

    BackHandler {
        vm.depopulateMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
    ) {
        // Header at the top
        ChatHeader(
            name = chatUser.name ?: "",
            imagerUrl = chatUser.imageUrl ?: ""
        ) {
            navController.popBackStack()
            vm.depopulateMessage()
        }

        // Message list
        MessageBox(
            modifier = Modifier.weight(1f), // Take up remaining space
            chatMessage = chatMessage.value,
            currentUserId = myUser?.userId ?: "",
            listState = listState
        )

        // Reply box at the bottom
        ReplyBox(
            reply = reply,
            onReplyChange = { reply = it },
            onSendReply = onSendReply,
            modifier = Modifier.imePadding() // Push up when keyboard appears
        )
    }
}


@Composable
fun ChatHeader(name: String, imagerUrl: String, onBackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = null,
            modifier = Modifier
                .clickable { onBackClicked.invoke() }
                .padding(8.dp)
        )
        CommonImage(
            data = imagerUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
                .background(Color.Red)
        )
        Text(name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(4.dp))


    }
}


@Composable
fun MessageBox(
    modifier: Modifier,
    chatMessage: List<Message>,
    currentUserId: String,
    listState: LazyListState
) {
    LazyColumn(
        modifier = modifier,
        state = listState // Attach the state to control scrolling
    ) {
        items(chatMessage) { msg ->

            val alignment = if (msg.sendBy == currentUserId) Alignment.End else Alignment.Start
            val color = if (msg.sendBy == currentUserId) Color(0xFF68C400) else Color(0xFFC0C0C0)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = alignment
            ) {
                Text(
                    text = msg.message ?: "",
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(color)
                        .padding(12.dp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReplyBox(
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = reply,
                onValueChange = onReplyChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 3
            )
            Button(onClick = onSendReply) {
                Text(text = "Send")
            }
        }
    }
}
