package uk.ac.tees.mad.chateasy.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import uk.ac.tees.mad.chateasy.ViewModel.AppViewModel
import uk.ac.tees.mad.chateasy.domain.RepoImpl
import uk.ac.tees.mad.chateasy.presentation.AuthScreen
import uk.ac.tees.mad.chateasy.presentation.HomeScreen

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val repo = RepoImpl()
    val auth= FirebaseAuth.getInstance();
    val viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(repo))
    
    NavHost(navController = navController, startDestination =  if (auth.currentUser==null){Routes.auth}else{
        Routes.home
    }) {
        composable(Routes.auth) {
            AuthScreen(viewModel = viewModel, navController = navController)
        }
        composable(Routes.home) {
            HomeScreen(viewModel = viewModel, navController = navController)
        }
        composable("${Routes.chat}/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            // ChatScreen(viewModel = viewModel, userId = userId, navController = navController)
            Text("Chat with $userId") // Placeholder for now
        }
    }
}