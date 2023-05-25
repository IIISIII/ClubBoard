package edu.sns.clubboard.adapter

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User
import edu.sns.clubboard.port.BoardInterface

class FBBoard: BoardInterface
{
    private val db = Firebase.firestore

    private val users = db.collection("users")

    private val clubs = db.collection("clubs")

    private val boards = db.collection("boards")

    private val relations = db.collection("relations")


    override fun getBoardData(id: String, onSuccess: (Board) -> Unit, onFailed: () -> Unit)
    {
        boards.document(id).get().addOnCompleteListener {
            if(it.isSuccessful) {
                it.result.run {
                    val name = this.getString(Board.KEY_NAME) ?: ""
                    val permissionLevel = this.getLong(Board.KEY_PERMISSION_LEVEL)
                    val readOnly = this.getBoolean(Board.KEY_READONLY) ?: false
                    val board = Board(this.id, name, null, permissionLevel, readOnly=readOnly)
                    onSuccess(board)
                }
            }
            else
                onFailed()
        }
    }

    override fun writePost(board: Board, post: Post, author: User, onComplete: () -> Unit): Boolean
    {
        try {
            val hashMap = post.toHashMap()
            hashMap[Post.KEY_AUTHOR] = db.document(hashMap[Post.KEY_AUTHOR] as String)

            boards.document(board.id).collection("posts").document().set(hashMap).addOnCompleteListener {
                onComplete()
            }
            return true
        } catch(_: Exception) {}
        return false
    }

    override fun readPost(boardId: String, postId: String, onComplete: (Post) -> Unit, onFailed: () -> Unit): Boolean
    {
        boards.document(boardId).collection("posts").document(postId).get().addOnCompleteListener {
            if(it.isSuccessful && it.result.exists())
                onComplete(documentToPost(it.result))
            else
                onFailed()
        }

        return false
    }

    override fun deletePost(board: Board, post: Post, user: User, onComplete: () -> Unit): Boolean
    {
        //club의 managePermission 또는 masterPermission을 가지고 있을때만 작동, 또는 admin 속성이 true일 때 작동

        return false
    }

    override fun getBoardListByIdList(list: List<String>, onSuccess: (List<Board>) -> Unit, onFailed: () -> Unit)
    {
        if(list.isEmpty()) {
            onSuccess(ArrayList<Board>())
            return
        }
        boards.whereIn(FieldPath.documentId(), list).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val boardList = ArrayList<Board>()
                for(doc in it.result.documents) {
                    val name = doc.getString(Board.KEY_NAME) ?: ""
                    val permissionLevel = doc.getLong(Board.KEY_PERMISSION_LEVEL)
                    val parentRef = doc.getDocumentReference(Board.KEY_PARENT)
                    val readOnly = doc.getBoolean(Board.KEY_READONLY) ?: false
                    val board = Board(doc.id, name, null, permissionLevel, parentRef?.id, readOnly)
                    boardList.add(board)
                }
                onSuccess(boardList)
            }
            else
                onFailed()
        }
    }

    override fun getParent(board: Board, onSuccess: (Club?) -> Unit, onFailed: () -> Unit)
    {
        if(board.parentId == null) {
            onSuccess(null)
            return
        }

        clubs.document(board.parentId!!).get().addOnCompleteListener {
            if(it.isSuccessful && it.result.exists()) {
                it.result.let { doc ->
                    val club = Club(doc.id, doc.getString(Club.KEY_NAME) ?: "")
                    onSuccess(club)
                }
            }
            else
                onFailed()
        }
    }

    override fun getPostList(board: Board): List<Post>?
    {

        return null
    }

    private var queryFlag = false
    private var lastDoc: DocumentSnapshot? = null

    override fun getPostListLimited(board: Board, reset: Boolean, limit: Long, onComplete: (List<Post>, Boolean) -> Unit): Boolean
    {
        if(reset)
            lastDoc = null

        if(queryFlag)
            return false

        val posts = boards.document(board.id).collection("posts")

        queryFlag = true
        val lim = if(limit > 100L) 100L else limit

        val query = if(lastDoc != null)
            posts.orderBy(Post.KEY_DATE, Query.Direction.DESCENDING).startAfter(lastDoc!!).limit(lim)
        else
            posts.orderBy(Post.KEY_DATE, Query.Direction.DESCENDING).limit(lim)

        query.get().addOnCompleteListener {
            if(it.isSuccessful) {
                val list = ArrayList<Post>()

                for(doc in it.result.documents) {
                    list.add(documentToPost(doc))
                    lastDoc = doc
                }

                onComplete(list, it.result.documents.size < lim)
            }
            queryFlag = false
        }
        return true
    }

    override fun checkWritePermission(user: User, board: Board, onComplete: (Boolean) -> Unit)
    {
        val userRef = users.document(user.id!!)
        val boardParent = board.parent
        if(boardParent == null) {
            onComplete(!board.readOnly)
            return
        }
        val clubRef = clubs.document(boardParent.id)

        relations.whereEqualTo(User.KEY_RELATION_USER, userRef).whereEqualTo(User.KEY_RELATION_CLUB, clubRef).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val doc = it.result.firstOrNull()
                if(doc?.exists() == true) {
                    if(board.permissionLevel == null)
                        onComplete(true)
                    else {
                        val permissionLevel = doc.getLong(User.KEY_RELATION_PERMISSION_LEVEL)
                        onComplete(if (permissionLevel == null) false else permissionLevel <= board.permissionLevel!!)
                    }
                }
                else
                    onComplete(false)
            }
            else
                onComplete(false)
        }
    }

    private fun documentToPost(document: DocumentSnapshot): Post
    {
        val id = document.id
        val title = document.getString(Post.KEY_TITLE).toString()
        val text = document.getString(Post.KEY_TEXT).toString()
        val date = document.getDate(Post.KEY_DATE)!!
        val author = document.getDocumentReference(Post.KEY_AUTHOR)?.path!!
        val postType = document.getLong(Post.KEY_TYPE) ?: Post.TYPE_NORMAL
        val targetClubId = document.getDocumentReference(Post.KEY_TARGET_CLUB)?.id

        val post = Post(id, title, text, date, author, postType)
        if(postType == Post.TYPE_RECRUIT)
            post.targetClubId = targetClubId

        return post
    }
}