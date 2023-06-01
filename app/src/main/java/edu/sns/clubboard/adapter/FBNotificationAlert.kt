package edu.sns.clubboard.adapter

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.NotificationData
import edu.sns.clubboard.data.User
import edu.sns.clubboard.port.NotificationAlert

class FBNotificationAlert: NotificationAlert()
{
    companion object
    {
        private var instance: FBNotificationAlert? = null

        fun getInstance() =
            instance ?: FBNotificationAlert().also {
                instance = it
            }
    }

    private val db = Firebase.firestore

    private val users = db.collection("users")

    private var userRegistration: ListenerRegistration? = null

    override fun registerUser(user: User)
    {
        userRegistration?.remove()
        userRegistration = users.document(user.id!!).collection("notifications").addSnapshotListener { value, error ->
            if(error != null) {
                Log.e("<FBNotification register>", error.message.toString())
                return@addSnapshotListener
            }

            value?.run {
                val list = ArrayList<NotificationData>()
                this.documentChanges.forEach { dc ->
                    if(dc.type == DocumentChange.Type.ADDED) {
                        //
                        //list.add()
                    }
                }
                if(list.isNotEmpty())
                    notifyEvents(list)
            }
        }
    }

    override fun unregisterUser()
    {
        userRegistration?.remove()
    }
}