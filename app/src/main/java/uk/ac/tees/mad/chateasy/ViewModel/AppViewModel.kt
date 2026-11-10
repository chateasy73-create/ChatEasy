package uk.ac.tees.mad.chateasy.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.ac.tees.mad.chateasy.data.Repo
import uk.ac.tees.mad.chateasy.model.ChatUser
import uk.ac.tees.mad.chateasy.model.ResultState
import uk.ac.tees.mad.chateasy.model.UserData
import android.content.ContentResolver
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner


class AppViewModel(savedStateHandle: SavedStateHandle, private val repo: Repo) : ViewModel() {
    private val _authScreenState = mutableStateOf(AuthScreenState())
    val authScreenState = _authScreenState

    private val _homeScreenState = mutableStateOf(HomeScreenState())
    val homeScreenState = _homeScreenState
    fun loginUser(userData: UserData) {
        viewModelScope.launch {
            repo.loginuserwithemailandpassword(userData).collect { result ->
                when (result) {

                    ResultState.Loading -> {
                        _authScreenState.value =
                            AuthScreenState(isLoading = true)
                    }

                    is ResultState.Succes -> {

                        _authScreenState.value =
                            AuthScreenState(
                                success = true,
                                userdata = result.data
                            )
                    }

                    is ResultState.error -> {
                        _authScreenState.value =
                            AuthScreenState(
                                error = result.message
                            )
                    }
                }
            }
        }
    }
    fun resetLoginState() {
        _authScreenState.value = AuthScreenState()
    }
    fun registerUser(userData: UserData) {
        viewModelScope.launch {
            repo.registerUser(userData).collect { result ->
                when (result) {

                    ResultState.Loading -> {
                        _authScreenState.value =
                            AuthScreenState(isLoading = true)
                    }

                    is ResultState.Succes -> {

                        _authScreenState.value =
                            AuthScreenState(
                                success = true,
                                userdata = result.data
                            )
                    }

                    is ResultState.error -> {
                        _authScreenState.value =
                            AuthScreenState(
                                error = result.message
                            )
                    }
                }
            }
        }
    }

    fun getContacts(contentResolver: ContentResolver) {
        viewModelScope.launch {
            repo.getContacts(contentResolver).collect { result ->
                when (result) {
                    ResultState.Loading -> {
                        _homeScreenState.value = _homeScreenState.value.copy(isLoading = true)
                    }
                    is ResultState.Succes -> {
                        _homeScreenState.value = _homeScreenState.value.copy(
                            isLoading = false,
                            contacts = result.data
                        )
                    }
                    is ResultState.error -> {
                        _homeScreenState.value = _homeScreenState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun Factory(repo: Repo): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(SavedStateHandle(), repo) as T
            }
        }
    }
}
data class AuthScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userdata: String? = null,
    val success: Boolean = false
)

data class HomeScreenState(
    val isLoading: Boolean = false,
    val contacts: List<ChatUser> = emptyList(),
    val error: String? = null
)