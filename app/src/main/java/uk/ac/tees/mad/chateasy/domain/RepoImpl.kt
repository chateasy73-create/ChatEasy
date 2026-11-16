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
import uk.ac.tees.mad.chateasy.model.ChatMessage
import uk.ac.tees.mad.chateasy.model.ChatUser
import uk.ac.tees.mad.chateasy.model.ResultState
import uk.ac.tees.mad.chateasy.model.UserData

class RepoImpl : Repo {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    override fun getContacts(contentResolver: ContentResolver): Flow<ResultState<List<ChatUser>>> = callbackFlow {
        trySend(ResultState.Loading)
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(ResultState.error("User not logged in"))
            close()
            return@callbackFlow
        }

        val phoneToNameMap = mutableMapOf<String, String>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            ),
            null,
            null,
            null
        )
        cursor?.use {
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            while (it.moveToNext()) {
                val number = it.getString(numberIndex).replace("\\s".toRegex(), "").replace("-", "")
                val name = it.getString(nameIndex)
                phoneToNameMap[number] = name
            }
        }

        firestore.collection("user").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val allUsers = task.result.documents.map { doc ->
                    val firestorePhoneNumber = doc.getString("phoneNumber") ?: ""
                    val normalizedPhoneNumber = firestorePhoneNumber.replace("\\s".toRegex(), "").replace("-", "")
                    
                    // Try to find the name in phone contacts first
                    val contactName = phoneToNameMap[normalizedPhoneNumber] 
                        ?: phoneToNameMap.entries.find { it.key.endsWith(normalizedPhoneNumber.takeLast(10)) }?.value
                        ?: doc.getString("name") 
                        ?: doc.getString("email") 
                        ?: "Unknown"

                    ChatUser(
                        uid = doc.getString("uid") ?: "",
                        name = contactName,
                        email = doc.getString("email") ?: "",
                        phoneNumber = firestorePhoneNumber
                    )
                }

                val matchedUsers = allUsers.filter { user ->
                    val normalizedUserPhone = user.phoneNumber.replace("\\s".toRegex(), "").replace("-", "")
                    user.uid != currentUid && (phoneToNameMap.containsKey(normalizedUserPhone) || phoneToNameMap.keys.any { it.endsWith(normalizedUserPhone.takeLast(10)) })
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
                        .whereEqualTo("chatId", getChatId(currentUid, user.uid))
                        .get()
                        .addOnCompleteListener { msgTask ->
                            val lastMsg = if (msgTask.isSuccessful && !msgTask.result.isEmpty) {
                                msgTask.result.documents
                                    .mapNotNull { it.getString("message") to (it.getLong("timestamp") ?: 0L) }
                                    .maxByOrNull { it.second }?.first ?: ""
                            } else ""
                            
                            finalUsers.add(user.copy(lastMessage = lastMsg))
                            processedCount++
                            if (processedCount == matchedUsers.size) {
                                trySend(ResultState.Succes(finalUsers.sortedByDescending { it.lastMessage.isNotEmpty() }))
                            }
                        }
                }
            } else {
                trySend(ResultState.error(task.exception?.localizedMessage ?: "Error fetching users"))
            }
        }
        awaitClose { close() }
    }
    override fun sendMessage(receiverId: String, message: String): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(ResultState.error("User not logged in"))
            close()
            return@callbackFlow
        }

        val chatMessage = mapOf(
            "senderId" to currentUid,
            "receiverId" to receiverId,
            "chatId" to getChatId(currentUid, receiverId),
            "message" to message,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("messages").add(chatMessage)
            .addOnSuccessListener {
                trySend(ResultState.Succes("Message Sent"))
            }
            .addOnFailureListener { e ->
                trySend(ResultState.error(e.localizedMessage ?: "Failed to send message"))
            }
        awaitClose {  }
    }

    override fun getMessages(receiverId: String): Flow<ResultState<List<ChatMessage>>> = callbackFlow {
        trySend(ResultState.Loading)
        val currentUid = auth.currentUser?.uid
        if (currentUid == null) {
            trySend(ResultState.error("User not logged in"))
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("messages")
            .whereEqualTo("chatId", getChatId(currentUid, receiverId))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultState.error(error.localizedMessage ?: "Error fetching messages"))
                    return@addSnapshotListener
                }
 
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { doc ->
                        ChatMessage(
                            senderId = doc.getString("senderId") ?: "",
                            receiverId = doc.getString("receiverId") ?: "",
                            chatId = doc.getString("chatId") ?: "",
                            message = doc.getString("message") ?: "",
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                    }.sortedBy { it.timestamp }
                    trySend(ResultState.Succes(messages))
                }
            }

        awaitClose { registration.remove() }
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