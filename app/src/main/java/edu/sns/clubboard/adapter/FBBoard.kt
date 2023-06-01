package edu.sns.clubboard.adapter

import android.util.Log
import com.google.firebase.firestore.*
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

    private val fileManager = FBFileManager.getInstance()

    private var previewRegistration: ListenerRegistration? = null


    override fun registerBoardPreview(id: String, limit: Long, onListChanged: (List<Post>) -> Unit)
    {
        previewRegistration?.remove()
        previewRegistration = boards.document(id).collection("posts").orderBy(Post.KEY_DATE, Query.Direction.DESCENDING).limit(limit).addSnapshotListener { value, error ->
            if(error != null) {

                return@addSnapshotListener
            }

            val postList = ArrayList<Post>()
            value?.documents?.forEach {
                postList.add(FBConvertor.documentToPost(it))
            }
            onListChanged(postList)
        }
    }

    override fun unregisterBoardPreview()
    {
        previewRegistration?.remove()
    }

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
            hashMap[Post.KEY_AUTHOR] = users.document(hashMap[Post.KEY_AUTHOR] as String)

            val posts = boards.document(board.id).collection("posts")
            val task = if(post.id.isBlank())
                    posts.document().set(hashMap)
                else
                    posts.document(post.id).update(hashMap)

            task.addOnCompleteListener {
                onComplete()
            }
            return true
        } catch(_: Exception) {}
        return false
    }

    override fun readPost(boardId: String, postId: String, onComplete: (Post) -> Unit, onFailed: () -> Unit)
    {
        boards.document(boardId).collection("posts").document(postId).get().addOnCompleteListener {
            if(it.isSuccessful && it.result.exists())
                onComplete(FBConvertor.documentToPost(it.result))
            else
                onFailed()
        }
    }

    override fun deletePost(board: Board, post: Post, user: User, onComplete: () -> Unit)
    {
        post.imgPath?.run {
            fileManager.removeImage(this)
        }

        boards.document(board.id).collection("posts").document(post.id).delete().addOnCompleteListener {
            onComplete()
        }
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
                    list.add(FBConvertor.documentToPost(doc))
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
}