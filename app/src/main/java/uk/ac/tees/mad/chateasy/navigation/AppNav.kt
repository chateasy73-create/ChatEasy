package uk.ac.tees.mad.chateasy.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.chateasy.ViewModel.AppViewModel
import uk.ac.tees.mad.chateasy.domain.RepoImpl
import uk.ac.tees.mad.chateasy.presentation.AuthScreen

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val repo = RepoImpl()
    val viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(repo))
    
    NavHost(navController = navController, startDestination = Routes.auth) {
        composable(Routes.auth) {
            AuthScreen(viewModel = viewModel)
        }
    }
}