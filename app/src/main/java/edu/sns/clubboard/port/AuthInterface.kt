package edu.sns.clubboard.port

import edu.sns.clubboard.data.User

interface AuthInterface
{
    fun load(onComplete: (User?) -> Unit)

    fun login(id: String, pw: String, onSuccess: (user: User) -> Unit, onFailed: () -> Unit)

    fun getUserInfo(): User?

    fun getUserInfo(id: String?, onComplete: (User?) -> Unit)

    fun authenticate(user: User?)

    fun checkAuthenticated(user: User, onComplete: (Boolean) -> Unit)

    fun signUp(user: User, password: String, onSuccess: () -> Unit, onFailed: () -> Unit)

    fun isLogined(): Boolean

    fun getPreviewList(user: User, onSuccess: (boardList: List<String>) -> Unit, onFailed: () -> Unit)

    suspend fun isValidId(id: String): Boolean

    suspend fun isValidMail(mail: String): Boolean

    suspend fun isValidNickname(nickname: String): Boolean
}