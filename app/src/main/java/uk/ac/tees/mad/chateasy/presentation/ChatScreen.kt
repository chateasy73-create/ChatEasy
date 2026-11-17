package uk.ac.tees.mad.chateasy.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import com.google.firebase.auth.FirebaseAuth
import uk.ac.tees.mad.chateasy.ViewModel.AppViewModel
import uk.ac.tees.mad.chateasy.ViewModel.ChatScreenState
import uk.ac.tees.mad.chateasy.model.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: AppViewModel, userId: String, navController: NavController) {
    val state by viewModel.chatScreenState
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.getMessages(userId)
    }

    ChatScreenContent(
        state = state,
        currentUid = currentUid,
        messageText = messageText,
        onMessageChange = { messageText = it },
        onBack = { navController.popBackStack() },
        onSend = {
            if (messageText.isNotBlank()) {
                viewModel.sendMessage(userId, messageText)
                messageText = ""
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    state: ChatScreenState,
    currentUid: String,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onBack: () -> Unit,
    onSend: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            ChatInput(
                messageText = messageText,
                onMessageChange = onMessageChange,
                onSend = onSend
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading && state.messages.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error ?: "Error loading messages",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = false,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.messages) { message ->
                        MessageBubble(
                            message = message,
                            isCurrentUser = message.senderId == currentUid
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage, isCurrentUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isCurrentUser) 12.dp else 0.dp,
                bottomEnd = if (isCurrentUser) 0.dp else 12.dp
            ),
            color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ChatInput(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 4.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSend,
                enabled = messageText.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val mockMessages = listOf(
        ChatMessage(senderId = "1", receiverId = "2", message = "Hi Alice!", timestamp = 1L),
        ChatMessage(senderId = "2", receiverId = "1", message = "Hey Bob, how are you?", timestamp = 2L),
        ChatMessage(senderId = "1", receiverId = "2", message = "I'm doing great, working on ChatEasy!", timestamp = 3L)
    )
    ChatScreenContent(
        state = ChatScreenState(messages = mockMessages),
        currentUid = "1",
        messageText = "",
        onMessageChange = {},
        onBack = {},
        onSend = {}
    )
}
