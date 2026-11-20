package uk.ac.tees.mad.chateasy.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import uk.ac.tees.mad.chateasy.ViewModel.AppViewModel
import uk.ac.tees.mad.chateasy.navigation.Routes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: AppViewModel, navController: NavController) {
    val state by viewModel.settingsScreenState

    LaunchedEffect(state.logoutSuccess) {
        if (state.logoutSuccess) {
            viewModel.resetSettingsState()
            viewModel.resetLoginState()
            navController.navigate(Routes.auth) {
                popUpTo(0)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            state.error?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error)
            }

            if (state.cacheCleared) {
                Text(text = "Cache cleared successfully!", color = Color(0xFF4CAF50))
            }

            SettingsItem(
                title = "Clear recent chat cache",
                subtitle = "Deletes locally stored messages to free up space.",
                onClick = { viewModel.clearCache() }
            )

            HorizontalDivider()

            SettingsItem(
                title = "Logout",
                subtitle = "Sign out of your account.",
                titleColor = MaterialTheme.colorScheme.error,
                onClick = { viewModel.logout() }
            )
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
