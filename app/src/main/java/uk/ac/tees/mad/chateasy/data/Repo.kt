package uk.ac.tees.mad.chateasy.data

import kotlinx.coroutines.flow.Flow
import uk.ac.tees.mad.chateasy.model.ResultState
import uk.ac.tees.mad.chateasy.model.UserData


interface Repo {
    suspend fun registerUser(userdata: UserData): Flow<ResultState<String>>
    fun loginuserwithemailandpassword(userdata: UserData): Flow<ResultState<String>>

}