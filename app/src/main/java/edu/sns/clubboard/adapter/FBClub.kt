package edu.sns.clubboard.adapter

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.*
import edu.sns.clubboard.port.ClubInterface
import kotlinx.coroutines.tasks.await

class FBClub: ClubInterface
{
    private val db = Firebase.firestore

    private val clubs = db.collection("clubs")

    private val relations = db.collection("relations")

    private val boards = db.collection("boards")

    companion object {
        private var instance: FBClub? = null

        fun getInstance() =
            instance ?: FBClub().also {
                instance = it
            }
    }

    override fun load(id: String, onComplete: (Club, List<Board>) -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document(id)
        clubRef.get().addOnCompleteListener {
            if(!it.isSuccessful) {
                onFailed()
                return@addOnCompleteListener
            }

            it.result.let { res ->
                val id = res.id
                val name = res.getString(Club.KEY_NAME)
                val club = Club(id, name!!)

                boards.whereEqualTo("parent", clubRef).get().addOnCompleteListener { resB ->
                    if(!resB.isSuccessful) {
                        onFailed()
                        return@addOnCompleteListener
                    }

                    resB.result.documents.let { list ->
                        val boardList = list.map { item ->
                            val id = item.id
                            val name = item.getString(Board.KEY_NAME)
                            val permissionLevel = item.getLong(Board.KEY_PERMISSION_LEVEL)
                            Board(id, name!!, club, permissionLevel)
                        }
                        onComplete(club, boardList)
                    }
                }
            }
        }
    }

    override fun createClub(clubName: String, user: User, onComplete: (Club) -> Unit, onFailed: () -> Unit)
    {

    }

    override fun getClubMembers(club: Club, onComplete: (List<User>) -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document(club.id)

        relations.whereEqualTo("club", clubRef).get().addOnCompleteListener {
            if(it.isSuccessful) {
                it.result.documents.map { doc ->
                    Log.i("membersIds", doc.getDocumentReference("user").toString())
                }
            }
            else
                onFailed()
        }
    }

    private var queryFlag = false
    private var lastDoc: DocumentSnapshot? = null

    override fun getClubListLimited(reset: Boolean, limit: Long, onComplete: (List<Club>) -> Unit): Boolean
    {
        if(reset)
            lastDoc = null

        if(queryFlag)
            return false

        queryFlag = true
        val lim = if(limit > 100) 100 else limit

        val query = if(lastDoc != null)
            clubs.startAfter(lastDoc).limit(lim)
        else
            clubs.limit(lim)

        query.get().addOnCompleteListener {
            if(it.isSuccessful) {
                val list = ArrayList<Club>()
                for(item in it.result.documents) {
                    val id = item.id
                    val name = item.getString(Club.KEY_NAME)
                    list.add(Club(id, name!!))
                }
                lastDoc = it.result.documents.last()
                onComplete(list)
            }
            queryFlag = false
        }
        return true
    }

    override fun getUserClubList(user: User, reset: Boolean, limit: Long, onComplete: (List<Club>) -> Unit): Boolean
    {
        val userRef = db.collection("users").document(user.id!!)

        relations.whereEqualTo("uesr", userRef).get().addOnCompleteListener {

        }

        return true
    }

    override suspend fun isMember(user: User, club: Club): Boolean
    {
        return getPermissionLevel(user, club) == 2
    }

    override suspend fun isManager(user: User, club: Club): Boolean
    {
        return getPermissionLevel(user, club) == 1
    }

    override suspend fun isMaster(user: User, club: Club): Boolean
    {
        return getPermissionLevel(user, club) == 0
    }

    override suspend fun getPermissionLevel(user: User, club: Club): Int?
    {
        val userRef = db.collection("users").document(user.id!!)

        val relation = relations.whereEqualTo("user", userRef).get().await().firstOrNull()
        relation?.let {
            return it.getLong("permissionLevel") as Int
        }
        return null
    }
}