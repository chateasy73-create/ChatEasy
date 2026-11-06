package uk.ac.tees.mad.chateasy.domain

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import uk.ac.tees.mad.chateasy.data.Repo
import uk.ac.tees.mad.chateasy.model.ResultState
import uk.ac.tees.mad.chateasy.model.UserData

class RepoImpl : Repo {
    private val auth = FirebaseAuth.getInstance()
    override suspend fun registerUser(userdata: UserData): Flow<ResultState<String>> =
        callbackFlow {

            trySend(ResultState.Loading)

            val email = userdata.email
            val password = userdata.password
            val phoneNumber = userdata.phoneNumber


            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                trySend(ResultState.error("Email or Password cannot be empty"))
                close()
                return@callbackFlow
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(ResultState.Succes("Registration Successful"))
                    } else {
                        trySend(
                            ResultState.error(
                                task.exception?.localizedMessage ?: "Unknown error"
                            )
                        )
                    }
                }

            awaitClose { close() }
        }

    override fun loginuserwithemailandpassword(userdata: UserData): Flow<ResultState<String>> =
        callbackFlow {

            trySend(ResultState.Loading)

            val email = userdata.email
            val password = userdata.password

            if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
                trySend(ResultState.error("Email or Password cannot be empty"))
                close()
                return@callbackFlow
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        trySend(ResultState.Succes("Login Successful"))
                    } else {
                        trySend(
                            ResultState.error(
                                task.exception?.localizedMessage ?: "Unknown error"
                            )
                        )
                    }
                }

            awaitClose { close() }
        }

}