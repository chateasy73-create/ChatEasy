package uk.ac.tees.mad.chateasy.model

data class ChatUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val lastMessage: String = ""
)
