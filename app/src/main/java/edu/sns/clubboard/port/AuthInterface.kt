package edu.sns.clubboard.port

import edu.sns.clubboard.data.User

interface AuthInterface
{
    fun load(onComplete: (User?) -> Unit)

    fun login(id: String, pw: String, onSuccess: () -> Unit, onFailed: () -> Unit)

    fun getUserInfo(): User?

    fun getUserInfo(id: String, onComplete: (User?) -> Unit)

    fun authenticate(user: User?)

    fun isAuthenticated(user: User?): Boolean

    fun checkAuthenticated(user: User?, onSuccess: () -> Unit, onFailed: () -> Unit)

    fun signUp(user: User, password: String, onSuccess: () -> Unit, onFailed: () -> Unit)

    fun isLogined(): Boolean
}