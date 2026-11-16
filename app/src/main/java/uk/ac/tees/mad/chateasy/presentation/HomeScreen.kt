package uk.ac.tees.mad.chateasy.presentation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.chateasy.ViewModel.AppViewModel
import uk.ac.tees.mad.chateasy.model.ChatUser
import uk.ac.tees.mad.chateasy.navigation.Routes
import uk.ac.tees.mad.chateasy.ui.theme.ChatEasyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: AppViewModel, navController: NavController) {
    val state by viewModel.homeScreenState
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasPermission = isGranted
            if (isGranted) {
                viewModel.getContacts(context.contentResolver)
            }
        }
    )

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            launcher.launch(Manifest.permission.READ_CONTACTS)
        } else {
            viewModel.getContacts(context.contentResolver)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ChatEasy", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* TODO: Implement Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                !hasPermission -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Contacts permission is required to find friends.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) {
                            Text("Grant Permission")
                        }
                    }
                }
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Unknown Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.contacts.isEmpty() -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No contacts found", style = MaterialTheme.typography.bodyLarge)
                        Text("Invite friends to start chatting!", style = MaterialTheme.typography.bodySmall)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.contacts) { contact ->
                            ContactItem(contact = contact) {
                                navController.navigate("${Routes.chat}/${contact.uid}")
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactItem(contact: ChatUser, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(getRandomColor(contact.name)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = contact.name.take(1).uppercase(),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1.1f)) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (contact.lastMessage.isEmpty()) "Tap to start a conversation" else contact.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (contact.lastMessage.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getRandomColor(seed: String): Color {
    val colors = listOf(
        Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC),
        Color(0xFF7E57C2), Color(0xFF5C6BC0), Color(0xFF42A5F5),
        Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFFFFA726)
    )
    return colors[seed.hashCode().coerceAtLeast(0) % colors.size]
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ChatEasyTheme {
        val mockContacts = listOf(
            ChatUser(uid = "1", name = "Alice", lastMessage = "Hey there!"),
            ChatUser(uid = "2", name = "Bob", lastMessage = ""),
            ChatUser(uid = "3", name = "Charlie", lastMessage = "Seen any good movies lately?")
        )
        Surface {
            LazyColumn {
                items(mockContacts) { contact ->
                    ContactItem(contact = contact) {}
                }
            }
        }
    }
}