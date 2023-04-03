package edu.sns.clubboard.adapter

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.User
import edu.sns.clubboard.port.AuthInterface

class MyAuthorization: AuthInterface
{
    private val auth = Firebase.auth

    private val db = Firebase.firestore

    override fun init(onComplete: () -> Unit)
    {
        auth.currentUser?.reload()?.addOnCompleteListener { onComplete() } ?: onComplete()
    }

    override fun login(id: String, pw: String, onSuccess: () -> Unit, onFailed: () -> Unit)
    {
        val users = db.collection("users")

        users.whereEqualTo(User.KEY_LOGINID, id).get().addOnCompleteListener {
            if(it.isSuccessful) {
                    val doc = it.result.documents.firstOrNull()
                    val email = doc?.get(User.KEY_EMAIL).toString()
                    auth.signInWithEmailAndPassword(email, pw).addOnCompleteListener { res ->
                        if (res.isSuccessful)
                            onSuccess()
                        else
                            onFailed()
                    }
            }
            else
                onFailed()
        }
    }

    override fun authenticate(user: User?)
    {
        //need to reload
        auth.currentUser?.sendEmailVerification()
    }

    override fun isAuthenticated(user: User?): Boolean
    {
        return auth.currentUser?.isEmailVerified ?: false
    }

    override fun checkAuthenticated(user: User?, onSuccess: () -> Unit, onFailed: () -> Unit)
    {
        auth.currentUser?.reload()?.addOnCompleteListener {
            if(it.isSuccessful) {
                when(auth.currentUser?.isEmailVerified) {
                    true -> onSuccess()
                    else -> onFailed()
                }
            }
            else
                onFailed()
        } ?: onFailed()
    }

    override fun signUp(user: User, password: String, onSuccess: () -> Unit, onFailed: () -> Unit)
    {
        val users = db.collection("users")

        auth.createUserWithEmailAndPassword(user.email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                it.result.user?.let { ur ->
                    users.document(ur.uid).set(user.toHashMap()).addOnCompleteListener { res ->
                        if(res.isSuccessful)
                            onSuccess()
                        else
                            ur.delete().addOnCompleteListener { onFailed() }
                    }
                } ?: onFailed()
            }
            else
                onFailed()
        }
    }

    override fun isLogined(): Boolean
    {
        return when(auth.currentUser) {
            null -> false
            else -> true
        }
    }
}