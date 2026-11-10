package uk.ac.tees.mad.chateasy.domain

import android.content.ContentResolver
import android.provider.ContactsContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Filter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

import uk.ac.tees.mad.chateasy.data.Repo
import uk.ac.tees.mad.chateasy.model.ChatUser
import uk.ac.tees.mad.chateasy.model.ResultState
import uk.ac.tees.mad.chateasy.model.UserData

class RepoImpl : Repo {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun getContacts(contentResolver: ContentResolver): Flow<ResultState<List<ChatUser>>> = callbackFlow {
        trySend(ResultState.Loading)
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(ResultState.error("User not logged in"))
            close()
            return@callbackFlow
        }

        val phoneContacts = mutableSetOf<String>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        )
        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val number = it.getString(numberIndex).replace("\\s".toRegex(), "").replace("-", "")
                phoneContacts.add(number)
            }
        }

        firestore.collection("user").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allUsers = task.result.documents.map { doc ->
                    ChatUser(
                        uid = doc.getString("uid") ?: "",
                        name = doc.getString("name") ?: doc.getString("email") ?: "Unknown",
                        email = doc.getString("email") ?: "",
                        phoneNumber = doc.getString("phoneNumber") ?: ""
                    )
                }

                val matchedUsers = allUsers.filter { user ->
                    user.uid != currentUid && (phoneContacts.contains(user.phoneNumber) || phoneContacts.any { it.endsWith(user.phoneNumber.takeLast(10)) })
                }

                // Temporary list to hold updated users with last message
                val finalUsers = mutableListOf<ChatUser>()
                var processedCount = 0

                if (matchedUsers.isEmpty()) {
                    trySend(ResultState.Succes(emptyList()))
                    return@addOnCompleteListener
                }

                matchedUsers.forEach { user ->
                    firestore.collection("messages")
                        .where(
                            Filter.or(
                                Filter.and(
                                    Filter.equalTo("senderId", currentUid),
                                    Filter.equalTo("receiverId", user.uid)
                                ),
                                Filter.and(
                                    Filter.equalTo("senderId", user.uid),
                                    Filter.equalTo("receiverId", currentUid)
                                )
                            )
                        )
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .addOnCompleteListener { msgTask ->
                            val lastMsg = if (msgTask.isSuccessful && !msgTask.result.isEmpty) {
                                msgTask.result.documents[0].getString("message") ?: ""
                            } else ""
                            
                            finalUsers.add(user.copy(lastMessage = lastMsg))
                            processedCount++
                            if (processedCount == matchedUsers.size) {
                                trySend(ResultState.Succes(finalUsers))
                            }
                        }
                }
            } else {
                trySend(ResultState.error(task.exception?.localizedMessage ?: "Error fetching users"))
            }
        }
        awaitClose { close() }
    }
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
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            val userMap = mapOf(
                                "uid" to uid,
                                "email" to email,
                                "phoneNumber" to phoneNumber
                            )
                            firestore.collection("user").document(uid)
                                .set(userMap)
                                .addOnSuccessListener {
                                    trySend(ResultState.Succes("Registration Successful"))
                                }
                                .addOnFailureListener { e ->
                                    trySend(ResultState.error(e.localizedMessage ?: "Firestore error"))
                                }
                        } else {
                            trySend(ResultState.error("User UID not found"))
                        }
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