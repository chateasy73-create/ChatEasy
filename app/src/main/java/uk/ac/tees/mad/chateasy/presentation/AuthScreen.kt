package uk.ac.tees.mad.chateasy.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import uk.ac.tees.mad.chateasy.ViewModel.AppViewModel
import uk.ac.tees.mad.chateasy.model.UserData
import uk.ac.tees.mad.chateasy.navigation.Routes
import uk.ac.tees.mad.chateasy.ui.theme.ChatEasyTheme

@Composable
fun AuthScreen(viewModel: AppViewModel, navController: NavController) {
    val state by viewModel.authScreenState
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(state.success) {
        if (state.success) {
            navController.navigate(Routes.home) {
                popUpTo(Routes.auth) { inclusive = true }
            }
        }
    }

    AuthContent(
        isLogin = isLogin,
        email = email,
        password = password,
        phoneNumber = phoneNumber,
        isLoading = state.isLoading,
        error = state.error,
        onEmailChange = { email = it },
        onPasswordChange = { password = it },
        onPhoneNumberChange = { phoneNumber = it },
        onToggleMode = { 
            isLogin = !isLogin 
            viewModel.resetLoginState()
        },
        onSubmit = {
            val userData = UserData(
                email = email,
                password = password,
                phoneNumber = phoneNumber
            )
            if (isLogin) {
                viewModel.loginUser(userData)
            } else {
                viewModel.registerUser(userData)
            }
        }
    )
}

@Composable
fun AuthContent(
    isLogin: Boolean,
    email: String,
    password: String,
    phoneNumber: String,
    isLoading: Boolean,
    error: String?,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPhoneNumberChange: (String) -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLogin) "Login" else "Sign Up",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (!isLogin) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (isLogin) "Login" else "Sign Up")
            }
        }
        
        TextButton(onClick = onToggleMode) {
            Text(text = if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenLoginPreview() {
    ChatEasyTheme {
        AuthContent(
            isLogin = true,
            email = "user@example.com",
            password = "password",
            phoneNumber = "",
            isLoading = false,
            error = null,
            onEmailChange = {},
            onPasswordChange = {},
            onPhoneNumberChange = {},
            onToggleMode = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenSignUpPreview() {
    ChatEasyTheme {
        AuthContent(
            isLogin = false,
            email = "",
            password = "",
            phoneNumber = "",
            isLoading = false,
            error = null,
            onEmailChange = {},
            onPasswordChange = {},
            onPhoneNumberChange = {},
            onToggleMode = {},
            onSubmit = {}
        )
    }
}
