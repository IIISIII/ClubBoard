package edu.sns.clubboard.adapter

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.User
import edu.sns.clubboard.port.AuthInterface
import kotlinx.coroutines.tasks.await

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
                        if(res.isSuccessful && res.result.exists()) {
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


    override fun login(id: String, pw: String, onSuccess: (user: User) -> Unit, onFailed: () -> Unit)
    {
        users.whereEqualTo(User.KEY_LOGINID, id).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val doc = it.result.documents.firstOrNull()
                doc?.run {
                    userInfo = documentToUserInfo(this)
                    if(userInfo == null) {
                        onFailed()
                        return@addOnCompleteListener
                    }
                    auth.signInWithEmailAndPassword(userInfo?.email.toString(), pw).addOnCompleteListener { res ->
                        if (res.isSuccessful) {
                            onSuccess(userInfo!!)

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

    override fun getUserInfo(id: String?, onComplete: (User?) -> Unit)
    {
        val uid = id ?: auth.currentUser?.uid
        if(uid == null) {
            onComplete(null)
            return
        }
        users.document(uid).get().addOnCompleteListener {
            if(it.isSuccessful && it.result.data != null)
                onComplete(documentToUserInfo(it.result))
            else
                onComplete(null)
        }
    }

    override fun authenticate(user: User?)
    {
        //need to reload
        auth.currentUser?.sendEmailVerification()
    }

    override fun checkAuthenticated(user: User, onComplete: (Boolean) -> Unit)
    {
        if(user.id == null) {
            onComplete(false)
            return
        }
        users.document(user.id!!).get().addOnCompleteListener {
            if(it.isSuccessful && it.result.exists()) {
                val isTestUser = it.result.getBoolean(User.KEY_TEST_USER) ?: false
                if(isTestUser)
                    onComplete(true)
                else {
                    auth.currentUser?.reload()?.addOnCompleteListener { res ->
                        if(res.isSuccessful)
                            onComplete(auth.currentUser?.isEmailVerified ?: false)
                        else
                            onComplete(false)
                    } ?: onComplete(false)
                }
            }
            else
                onComplete(false)
        }
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

    override suspend fun isValidId(id: String): Boolean
    {
        val result = users.whereEqualTo(User.KEY_LOGINID, id).count().get(AggregateSource.SERVER).await()

        return result.count == 0L
    }

    override suspend fun isValidMail(mail: String): Boolean
    {
        val result = users.whereEqualTo(User.KEY_EMAIL, mail).count().get(AggregateSource.SERVER).await()

        return result.count == 0L
    }

    override suspend fun isValidNickname(nickname: String): Boolean
    {
        val result = users.whereEqualTo(User.KEY_NICKNAME, nickname).count().get(AggregateSource.SERVER).await()

        return result.count == 0L
    }

    override fun getPreviewList(user: User, onSuccess: (boardList: List<String>) -> Unit, onFailed: () -> Unit)
    {
        users.document(user.id!!).get().addOnCompleteListener {
            if(it.isSuccessful) {
                if(it.result.exists()) {
                    val doc = it.result
                    val prevList = doc.get(User.KEY_PREVIEW_LIST)
                    if(prevList != null) {
                        val list = prevList as List<*>
                        val boardList = list.map { item ->
                            val docRef = item as DocumentReference
                            docRef.id
                        }
                        onSuccess(boardList)
                    }
                    else
                        onSuccess(ArrayList<String>())
                }
                else
                    onSuccess(ArrayList<String>())
            }
            else {
                onFailed()
            }
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

            val admin = document.getBoolean(User.KEY_ADMIN) ?: false

            User(document.id, studentId!!, name!!, phone!!, email!!, nickname!!, loginId!!, profileImg, admin)
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