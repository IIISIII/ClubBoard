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

    private val fileManager = FBFileManager.getInstance()


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
                val club = FBConvertor.documentToClub(res)

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

    override fun createClub(clubName: String, description: String, imgPath: String?, user: User, onComplete: (Club) -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document()
        val userRef = users.document(user.id!!)
        val relationRef = relations.document("${user.id!!}-${clubRef.id}")
        val postRef = boards.document("1").collection("posts").document()

        val clubMap = hashMapOf(
            Club.KEY_ACTIVATE to false,
            Club.KEY_NAME to clubName,
            Club.KEY_DESCRIPTION to description,
            Club.KEY_IMGPATH to imgPath
        )

        val relationMap = hashMapOf(
            User.KEY_RELATION_CLUB to clubRef,
            User.KEY_RELATION_PERMISSION_LEVEL to 0,
            User.KEY_RELATION_USER to userRef
        )

        val postMap = hashMapOf(
            Post.KEY_TITLE to "$clubName 모집",
            Post.KEY_TEXT to description,
            Post.KEY_AUTHOR to userRef,
            Post.KEY_DATE to Date(),
            Post.KEY_TARGET_CLUB to clubRef,
            Post.KEY_TYPE to Post.TYPE_RECRUIT,
            Post.KEY_IMG to imgPath
        )

        db.runBatch {
            it.set(clubRef, clubMap)
            it.set(relationRef, relationMap)
            it.set(postRef, postMap)
        }.addOnCompleteListener {
            if(it.isSuccessful)
                onComplete(Club(clubRef.id, clubName, description, imgPath, false))
            else
                onFailed()
        }
    }

    override fun activateClub(club: Club, onSuccess: () -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document(club.id)
        val boardRefNoti = boards.document()
        val boardRefFree = boards.document()

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

        db.runTransaction {
            val clubSnapshot = it.get(clubRef)
            val isActivated = clubSnapshot.getBoolean(Club.KEY_ACTIVATE) ?: false

            if(!isActivated) {
                it.update(clubRef, Club.KEY_ACTIVATE, true)
                it.set(boardRefNoti, boardMapNoti)
                it.set(boardRefFree, boardMapFree)
            }
        }.addOnCompleteListener {
            if(it.isSuccessful)
                onSuccess()
            else
                onFailed()
        }
    }

    override fun getClubMembers(club: Club, onComplete: (List<Member>) -> Unit, onFailed: () -> Unit)
    {
        val clubRef = clubs.document(club.id)
        val memberMap = HashMap<String?, Long?>()

        relations.whereEqualTo(User.KEY_RELATION_CLUB, clubRef).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val refs = it.result.documents.map { doc ->
                    val uref = doc.getDocumentReference(User.KEY_RELATION_USER)
                    val level = doc.getLong(User.KEY_RELATION_PERMISSION_LEVEL)
                    memberMap[uref?.id] = level
                    uref
                }
                users.whereIn(FieldPath.documentId(), refs).get().addOnCompleteListener { res ->
                    if(res.isSuccessful) {
                        val list = res.result.documents.map { doc ->
                            Member(FBConvertor.documentToUser(doc), memberMap[doc.id])
                        }
                        onComplete(list as ArrayList<Member>)
                    }
                    else
                        onFailed()
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
                    list.add(FBConvertor.documentToClub(item))

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
                    doc.getDocumentReference(User.KEY_RELATION_CLUB)?.id
                }
                clubs.whereIn(FieldPath.documentId(), clubRefs).get().addOnCompleteListener { res ->
                    if(res.isSuccessful) {
                        for(item in res.result.documents) {
                            clubList.add(FBConvertor.documentToClub(item))
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

    override fun setUserPemissionLevel(clubId: String, user: User, master: User, permissionLevel: Long, onComplete: (Boolean) -> Unit)
    {
        val userRef = relations.document("${user.id}-${clubId}")
        val masterRef = relations.document("${master.id}-${clubId}")

        db.runTransaction {
            val master = it.get(masterRef)
            val userSnap = it.get(userRef)

            if(master.data == null || userSnap.data == null) {
                onComplete(false)
                return@runTransaction
            }

            if(permissionLevel == User.PERMISSION_LEVEL_MASTER)
                it.update(masterRef, User.KEY_RELATION_PERMISSION_LEVEL, User.PERMISSION_LEVEL_MEMBER)
            it.update(userRef, User.KEY_RELATION_PERMISSION_LEVEL, permissionLevel)
        }.addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    override fun kick(clubId: String, user: User, onComplete: (Boolean) -> Unit)
    {
        val userRef = relations.document("${user.id}-${clubId}")

        userRef.delete().addOnCompleteListener {
            onComplete(it.isSuccessful)
        }
    }

    override fun modifyClubInfo(club: Club, description: String, imgPath: String?, onComplete: (Boolean) -> Unit)
    {
        val clubRef = clubs.document(club.id)

        db.runBatch {
            it.update(clubRef, Club.KEY_DESCRIPTION, description)
            if(imgPath != null)
                it.update(clubRef, Club.KEY_IMGPATH, imgPath)
        }.addOnCompleteListener {
            onComplete(it.isSuccessful)
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