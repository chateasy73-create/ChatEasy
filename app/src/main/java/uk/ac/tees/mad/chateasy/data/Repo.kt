package uk.ac.tees.mad.chateasy.data

import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.chateasy.model.ChatMessage
import uk.ac.tees.mad.chateasy.model.ChatUser
import uk.ac.tees.mad.chateasy.model.ResultState
import uk.ac.tees.mad.chateasy.model.UserData

interface Repo {
    suspend fun registerUser(userdata: UserData): Flow<ResultState<String>>
    fun loginuserwithemailandpassword(userdata: UserData): Flow<ResultState<String>>
    fun getContacts(contentResolver: android.content.ContentResolver): Flow<ResultState<List<ChatUser>>>
    fun sendMessage(receiverId: String, message: String): Flow<ResultState<String>>
    fun getMessages(receiverId: String): Flow<ResultState<List<ChatMessage>>>
    fun updateFcmToken(token: String): Flow<ResultState<String>>
    fun logout(): Flow<ResultState<String>>
    fun clearCache(): Flow<ResultState<String>>
}