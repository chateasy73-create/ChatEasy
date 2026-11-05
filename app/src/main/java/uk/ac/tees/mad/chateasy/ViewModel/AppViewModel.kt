package uk.ac.tees.mad.chateasy.ViewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import uk.ac.tees.mad.chateasy.data.Repo
import uk.ac.tees.mad.chateasy.model.ResultState
import uk.ac.tees.mad.chateasy.model.UserData


class AppViewModel(savedStateHandle: SavedStateHandle, private val repo: Repo) : ViewModel() {
    private val _authScreenState = mutableStateOf(AuthScreenState())
    val authScreenState = _authScreenState
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






}
data class AuthScreenState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userdata: String? = null,
    val success: Boolean = false
)