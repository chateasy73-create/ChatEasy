package uk.ac.tees.mad.chateasy.model

data class ChatMessage(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val chatId: String = ""
)
