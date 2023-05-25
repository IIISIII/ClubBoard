package edu.sns.clubboard.adapter

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.*
import edu.sns.clubboard.port.ClubInterface
import kotlinx.coroutines.tasks.await
import java.util.Date

class FBClub: ClubInterface
{
    private val db = Firebase.firestore

    private val users = db.collection("users")

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

    override fun getClubData(id: String, onComplete: (Club, List<Board>) -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document(id)
        clubRef.get().addOnCompleteListener {
            if(!it.isSuccessful || !it.result.exists()) {
                onFailed()
                return@addOnCompleteListener
            }

            it.result.let { res ->
                val id = res.id
                val name = res.getString(Club.KEY_NAME)
                val description = res.getString(Club.KEY_DESCRIPTION)
                val imgPath = res.getString(Club.KEY_IMGPATH)
                val club = Club(id, name!!, description, imgPath)

                boards.whereEqualTo(Board.KEY_PARENT, clubRef).orderBy(Board.KEY_ORDER, Query.Direction.ASCENDING).get().addOnCompleteListener { resB ->
                    if(!resB.isSuccessful) {
                        onFailed()
                        return@addOnCompleteListener
                    }

                    resB.result.documents.let { list ->
                        val boardList = list.map { item ->
                            val boardId = item.id
                            val boardName = item.getString(Board.KEY_NAME) ?: ""
                            val permissionLevel = item.getLong(Board.KEY_PERMISSION_LEVEL)
                            val readOnly = item.getBoolean(Board.KEY_READONLY) ?: false
                            Board(boardId, boardName, club, permissionLevel, readOnly=readOnly)
                        }
                        onComplete(club, boardList)
                    }
                }
            }
        }
    }

    override fun createClub(clubName: String, description: String, img: Bitmap?, user: User, onComplete: (Club) -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document()
        val userRef = users.document(user.id!!)
        val relationRef = relations.document("${user.id!!}-${clubRef.id}")
        val postRef = boards.document("1").collection("posts").document()

        val clubMap = hashMapOf(
            Club.KEY_ACTIVATE to false,
            Club.KEY_NAME to clubName,
            Club.KEY_DESCRIPTION to description,
            Club.KEY_IMGPATH to null
        )

        val relationMap = hashMapOf(
            User.KEY_RELATION_CLUB to clubRef,
            User.KEY_RELATION_PERMISSION_LEVEL to 0,
            User.KEY_RELATION_USER to userRef
        )

        val postMap = hashMapOf(
            Post.KEY_TITLE to "${clubName} 모집",
            Post.KEY_TEXT to description,
            Post.KEY_AUTHOR to userRef,
            Post.KEY_DATE to Date(),
            Post.KEY_TARGET_CLUB to clubRef,
            Post.KEY_TYPE to Post.TYPE_RECRUIT
        )

        db.runBatch {
            it.set(clubRef, clubMap)
            it.set(relationRef, relationMap)
            it.set(postRef, postMap)
        }.addOnCompleteListener {
            if(it.isSuccessful)
                onComplete(Club(clubRef.id, clubName, description, null, false))
            else
                onFailed()
        }
    }

    override fun activateClub(club: Club, onSuccess: () -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document(club.id)
        val boardRefNoti = boards.document()
        val boardRefFree = boards.document()

        val batch = db.batch()

        val boardMapNoti = hashMapOf(
            Board.KEY_NAME to "공지사항",
            Board.KEY_PARENT to clubRef,
            Board.KEY_PERMISSION_LEVEL to User.PERMISSION_LEVEL_MANAGER,
            Board.KEY_ORDER to 0L
        )

        val boardMapFree = hashMapOf(
            Board.KEY_NAME to "자유게시판",
            Board.KEY_PARENT to clubRef,
            Board.KEY_ORDER to 1L
        )

        batch.update(clubRef, Club.KEY_ACTIVATE, true)
        batch.set(boardRefNoti, boardMapNoti)
        batch.set(boardRefFree, boardMapFree)

        batch.commit().addOnCompleteListener {
            if(it.isSuccessful)
                onSuccess()
            else
                onFailed()
        }
    }

    override fun getClubMembers(club: Club, onComplete: (List<User>) -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document(club.id)

        relations.whereEqualTo(User.KEY_RELATION_CLUB, clubRef).get().addOnCompleteListener {
            if(it.isSuccessful) {
                it.result.documents.map { doc ->
                    Log.i("membersIds", doc.getDocumentReference(User.KEY_RELATION_USER).toString())
                }
            }
            else
                onFailed()
        }
    }

    private var queryFlag = false
    private var lastDoc: DocumentSnapshot? = null

    override fun getClubListLimited(reset: Boolean, limit: Long, onComplete: (List<Club>, Boolean) -> Unit): Boolean
    {
        if(reset)
            lastDoc = null

        if(queryFlag)
            return false

        queryFlag = true
        val lim = if(limit > 100) 100 else limit

        val query = if(lastDoc != null)
            clubs.orderBy(Club.KEY_NAME, Query.Direction.ASCENDING).startAfter(lastDoc!!).limit(lim)
        else
            clubs.orderBy(Club.KEY_NAME, Query.Direction.ASCENDING).limit(lim)

        query.get().addOnCompleteListener {
            if(it.isSuccessful) {
                val list = ArrayList<Club>()
                for(item in it.result.documents) {
                    val id = item.id
                    val name = item.getString(Club.KEY_NAME)
                    val description = item.getString(Club.KEY_DESCRIPTION)
                    val imgPath = item.getString(Club.KEY_IMGPATH)
                    val isActivated = item.getBoolean(Club.KEY_ACTIVATE) ?: false
                    list.add(Club(id, name!!, description, imgPath, isActivated))

                    lastDoc = item
                }

                onComplete(list, it.result.documents.size < lim)
            }
            queryFlag = false
        }
        return true
    }

    override fun getUserClubList(user: User, onComplete: (List<Club>) -> Unit): Boolean
    {
        val userRef = users.document(user.id!!)

        var clubList = ArrayList<Club>()

        relations.whereEqualTo(User.KEY_RELATION_USER, userRef).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val clubRefs = it.result.documents.map { doc ->
                    doc.getDocumentReference(User.KEY_RELATION_CLUB)
                }
                clubs.whereIn(FieldPath.documentId(), clubRefs).orderBy(Club.KEY_NAME, Query.Direction.ASCENDING).get().addOnCompleteListener { res ->
                    if(res.isSuccessful) {
                        for(item in res.result.documents) {
                            val id = item.id
                            val name = item.getString(Club.KEY_NAME)
                            val description = item.getString(Club.KEY_DESCRIPTION)
                            val imgPath = item.getString(Club.KEY_IMGPATH)
                            val isActivated = item.getBoolean(Club.KEY_ACTIVATE) ?: false
                            clubList.add(Club(id, name!!, description, imgPath, isActivated))
                        }
                    }

                    onComplete(clubList)
                }
            }
            else
                onComplete(clubList)
        }

        return true
    }

    override fun sendRequest(user: User, introduce: String, date: Date, club: Club, onComplete: () -> Unit, onFailed: () -> Unit)
    {
        val requestMap = hashMapOf(
            Request.KEY_USER to users.document(user.id!!),
            Request.KEY_USER_NAME to user.name,
            Request.KEY_USER_STUDENT_ID to user.studentId,
            Request.KEY_INTRODUCE to introduce,
            Request.KEY_DATE to date
        )

        clubs.document(club.id).collection("requests").document().set(requestMap).addOnCompleteListener {
            if(it.isSuccessful)
                onComplete()
            else
                onFailed()
        }
    }

    override fun getRequests(club: Club, onComplete: (List<Request>) -> Unit, onFailed: () -> Unit)
    {
        clubs.document(club.id).collection("requests").orderBy(Request.KEY_DATE, Query.Direction.ASCENDING).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val requests = it.result.documents.map { doc ->
                    val userId = doc.getDocumentReference(Request.KEY_USER)!!.id
                    val userName = doc.getString(Request.KEY_USER_NAME).toString()
                    val userStudentId = doc.getString(Request.KEY_USER_STUDENT_ID).toString()
                    val introduce = doc.getString(Request.KEY_INTRODUCE).toString()
                    val date = doc.getDate(Request.KEY_DATE)!!
                    Request(doc.id, userId, userName, userStudentId, introduce, date, club)
                } as ArrayList<Request>
                onComplete(requests)
            }
            else
                onFailed()
        }
    }

    override fun acceptRequests(requests: List<Request>, onComplete: () -> Unit, onFailed: () -> Unit)
    {
        val batch = db.batch()

        requests.forEach {
            val clubRef = clubs.document(it.club.id)
            val requestRef = clubRef.collection("requests").document(it.id)
            val userRef = users.document(it.userId)
            val permissionLevel = User.PERMISSION_LEVEL_MEMBER

            val relRef = relations.document("${it.userId}-${it.club.id}")
            val dataMap = hashMapOf(
                User.KEY_RELATION_USER to userRef,
                User.KEY_RELATION_CLUB to clubRef,
                User.KEY_RELATION_PERMISSION_LEVEL to permissionLevel
            )

            batch.delete(requestRef)
            batch.set(relRef, dataMap)
        }

        batch.commit().addOnCompleteListener {
            if(it.isSuccessful)
                onComplete()
            else
                onFailed()
        }
    }

    override fun declineRequests(club: Club, requests: List<Request>, onComplete: () -> Unit, onFailed: () -> Unit)
    {
        val batch = db.batch()

        requests.forEach {
            val relRef = clubs.document(club.id).collection("requests").document(it.id)
            batch.delete(relRef)
        }

        batch.commit().addOnCompleteListener {
            if(it.isSuccessful)
                onComplete()
            else
                onFailed()
        }
    }

    override fun checkProcessingRequest(user: User, club: Club, onComplete: (Boolean) -> Unit)
    {
        val userRef = users.document(user.id!!)
        clubs.document(club.id).collection("requests").whereEqualTo(Request.KEY_USER, userRef).count().get(AggregateSource.SERVER).addOnCompleteListener {
            if(it.isSuccessful)
                onComplete(it.result.count != 0L)
            else
                onComplete(false)
        }
    }

    override fun checkClubMember(userId: String, clubId: String, onComplete: (Boolean, Long?) -> Unit)
    {
        relations.document("${userId}-${clubId}").get().addOnCompleteListener {
            if(it.isSuccessful && it.result.data != null)
                onComplete(true, it.result.getLong(User.KEY_RELATION_PERMISSION_LEVEL))
            else
                onComplete(false, null)
        }
    }

    override fun checkIsActivated(clubId: String, onComplete: (Boolean) -> Unit)
    {
        clubs.document(clubId).get().addOnCompleteListener {
            if(it.isSuccessful && it.result.exists()) {
                val isActivated = it.result.getBoolean(Club.KEY_ACTIVATE) ?: false
                onComplete(isActivated)
            }
            else
                onComplete(false)
        }
    }

    override suspend fun isMember(user: User, club: Club): Boolean
    {
        return getPermissionLevel(user, club) == 2L
    }

    override suspend fun isManager(user: User, club: Club): Boolean
    {
        return getPermissionLevel(user, club) == 1L
    }

    override suspend fun isMaster(user: User, club: Club): Boolean
    {
        return getPermissionLevel(user, club) == 0L
    }

    override suspend fun getPermissionLevel(user: User, club: Club): Long?
    {
        val relation = relations.document("${user.id!!}-${club.id}").get().await()
        return if(relation.data != null)
            relation.getLong(User.KEY_RELATION_PERMISSION_LEVEL)
        else
            null
    }

    override suspend fun getPermissionLevel(userId: String, clubId: String): Long?
    {
        val relation = relations.document("${userId}-${clubId}").get().await()
        return if(relation.data != null)
            relation.getLong(User.KEY_RELATION_PERMISSION_LEVEL)
        else
            null
    }

    override suspend fun isClubMember(user: User, club: Club): Boolean
    {
        return getPermissionLevel(user, club) != null
    }

    override suspend fun isClubMember(userId: String, clubId: String): Boolean
    {
        return getPermissionLevel(userId, clubId) != null
    }

    override suspend fun isValidName(clubName: String): Boolean
    {
        val result = clubs.whereEqualTo(Club.KEY_NAME, clubName).count().get(AggregateSource.SERVER).await()

        return result.count == 0L
    }

    override suspend fun getClubMemberCount(club: Club): Long
    {
        val clubRef = clubs.document(club.id)
        val result = relations.whereEqualTo(User.KEY_RELATION_CLUB, clubRef).count().get(AggregateSource.SERVER).await()

        return result.count
    }
}