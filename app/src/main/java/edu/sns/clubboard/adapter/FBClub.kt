package edu.sns.clubboard.adapter

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.*
import edu.sns.clubboard.port.ClubInterface

class FBClub: ClubInterface
{
    private val db = Firebase.firestore

    private val clubs = db.collection("clubs")

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
                val members = res.get(Club.KEY_MEMBERS) as List<*>?
                val memberIds = members as List<String>
                val joinPermission = Permission(res.getDocumentReference(Club.KEY_JOIN_PERMISSION)!!.id)
                val managePermission = Permission(res.getDocumentReference(Club.KEY_MANAGE_PERMISSION)!!.id)
                val masterPermission = Permission(res.getDocumentReference(Club.KEY_MASTER_PERMISSION)!!.id)
                val club = Club(id, name!!, memberIds, joinPermission, managePermission, masterPermission)

                boards.whereEqualTo("parent", clubRef).get().addOnCompleteListener { resB ->
                    if(!resB.isSuccessful) {
                        onFailed()
                        return@addOnCompleteListener
                    }

                    resB.result.documents.let { list ->
                        val boardList = list.map { item ->
                            val id = item.id
                            val name = item.getString(Board.KEY_NAME)
                            val permissions = item.get(Board.KEY_PERMISSIONS) as List<*>?
                            val per = permissions?.map { id ->
                                Permission((id as DocumentReference).id)
                            }
                            Board(id, name!!, club, per)
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
        db.collection("users").whereIn(FieldPath.documentId(), club.memberIds).get().addOnCompleteListener {
            if(it.isSuccessful) {
                it.result.documents.map { doc ->
                    Log.i("memberIds", doc.id)
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
                    val members = item.get(Club.KEY_MEMBERS) as List<*>?
                    val memberIds = members as List<String>
                    val joinPermission = Permission(item.getDocumentReference(Club.KEY_JOIN_PERMISSION)!!.id)
                    val managePermission = Permission(item.getDocumentReference(Club.KEY_MANAGE_PERMISSION)!!.id)
                    val masterPermission = Permission(item.getDocumentReference(Club.KEY_MASTER_PERMISSION)!!.id)
                    list.add(Club(id, name!!, memberIds, joinPermission, managePermission, masterPermission))
                }
                lastDoc = it.result.documents.last()
                onComplete(list)
            }
            queryFlag = false
        }
        return true
    }

    private var queryFlag2 = false
    private var lastDoc2: DocumentSnapshot? = null

    override fun getUserClubListLimited(user: User, reset: Boolean, limit: Long, onComplete: (List<Club>) -> Unit): Boolean
    {
        if(reset)
            lastDoc2 = null

        if(queryFlag2)
            return false

        queryFlag2 = true
        val lim = if(limit > 100) 100 else limit

        val query = if(lastDoc2 != null)
            clubs.whereArrayContains(Club.KEY_MEMBERS, user.id!!).startAfter(lastDoc2).limit(lim)
        else
            clubs.whereArrayContains(Club.KEY_MEMBERS, user.id!!).limit(lim)

        query.get().addOnCompleteListener {
            if(it.isSuccessful) {
                val list = ArrayList<Club>()
                for(item in it.result.documents) {
                    val id = item.id
                    val name = item.getString(Club.KEY_NAME)
                    val members = item.get(Club.KEY_MEMBERS) as List<*>?
                    val memberIds = members as List<String>
                    val joinPermission = Permission(item.getDocumentReference(Club.KEY_JOIN_PERMISSION)!!.id)
                    val managePermission = Permission(item.getDocumentReference(Club.KEY_MANAGE_PERMISSION)!!.id)
                    val masterPermission = Permission(item.getDocumentReference(Club.KEY_MASTER_PERMISSION)!!.id)
                    list.add(Club(id, name!!, memberIds, joinPermission, managePermission, masterPermission))
                }
                lastDoc2 = it.result.documents.last()
                onComplete(list)
            }
            queryFlag2 = false
        }
        return true
    }

    override fun isMember(user: User, club: Club): Boolean
    {
        return user.permissions?.contains(club.joinPermission) == true
    }

    override fun isManager(user: User, club: Club): Boolean
    {
        return user.permissions?.contains(club.managePermission) == true
    }

    override fun isMaster(user: User, club: Club): Boolean
    {
        return user.permissions?.contains(club.masterPermission) == true
    }

}