package edu.sns.clubboard.adapter

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.Permission
import edu.sns.clubboard.data.User
import edu.sns.clubboard.port.AuthInterface

class FBAuthorization: AuthInterface
{
    private val auth = Firebase.auth

    private val db = Firebase.firestore

    private val users = db.collection("users")

    private var userInfo: User? = null

    private var userRegistration: ListenerRegistration? = null

    companion object {
        private var instance: FBAuthorization? = null

        fun getInstance() =
            instance ?: FBAuthorization().also {
                instance = it
            }

        fun remove() = instance?.unregister()
    }

    override fun load(onComplete: (User?) -> Unit)
    {
        auth.currentUser?.reload()?.addOnCompleteListener {
            unregister()

            if(it.isSuccessful) {
                auth.currentUser?.let { ur ->
                    users.document(ur.uid).get().addOnCompleteListener { res ->
                        if(res.isSuccessful) {
                            res.result.run {
                                userInfo = documentToUserInfo(this)
                                onComplete(userInfo)
                                register(ur.uid)
                            }
                        }
                        else
                            onComplete(null)
                    }
                }
            }
            else
                onComplete(null)
        } ?: onComplete(null)
    }

    override fun login(id: String, pw: String, onSuccess: () -> Unit, onFailed: () -> Unit)
    {
        users.whereEqualTo(User.KEY_LOGINID, id).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val doc = it.result.documents.firstOrNull()
                doc?.run {
                    userInfo = documentToUserInfo(this)
                    auth.signInWithEmailAndPassword(userInfo?.email.toString(), pw).addOnCompleteListener { res ->
                        if (res.isSuccessful) {
                            onSuccess()

                            register(doc.id)
                        }
                        else {
                            unregister()
                            userInfo = null
                            onFailed()
                        }
                    }
                } ?: onFailed()
            }
            else
                onFailed()
        }
    }

    override fun getUserInfo(): User? = userInfo

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
                        if(res.isSuccessful) {
                            userInfo = user
                            onSuccess()

                            register(ur.uid)
                        }
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

    private fun documentToUserInfo(document: DocumentSnapshot): User?
    {
        return try {
            val email = document.getString(User.KEY_EMAIL)
            val studentId = document.getString(User.KEY_STUDENTID)
            val name = document.getString(User.KEY_NAME)
            val nickname = document.getString(User.KEY_NICKNAME)
            val phone = document.getString(User.KEY_PHONE)
            val loginId = document.getString(User.KEY_LOGINID)
            val profileImg = document.getString(User.KEY_PROFILE_IMG)

            var permissions: List<Permission>? = null
            try {
                val plist = document.get(User.KEY_PERMISSIONS) as List<*>
                permissions = plist.map {
                    Permission((it as DocumentReference).id)
                }
                Log.i("testAAA", permissions.toString())
            } catch (err: Exception) {}

            val admin = document.getBoolean(User.KEY_ADMIN) ?: false

            User(document.id, studentId!!, name!!, phone!!, email!!, nickname!!, loginId!!, permissions, profileImg, admin)
        } catch (err: Exception) {
            null
        }
    }

    private fun register(id: String)
    {
        userRegistration?.remove()
        userRegistration = users.document(id).addSnapshotListener { value, error ->
            if(error != null) {
                Log.e("<FBAuthorization load>", error.message.toString())
                return@addSnapshotListener
            }

            value?.run {
                userInfo = documentToUserInfo(this)
            }
        }
    }

    private fun unregister()
    {
        userRegistration?.remove()
    }
}