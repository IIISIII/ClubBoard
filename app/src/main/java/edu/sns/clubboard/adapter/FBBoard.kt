package edu.sns.clubboard.adapter

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User
import edu.sns.clubboard.port.BoardInterface

class FBBoard: BoardInterface
{
    private val db = Firebase.firestore

    private val boards = db.collection("boards")

    private val relations = db.collection("relations")

    private var boardData: Board? = null

    private var boardRef: DocumentReference

    constructor(id: String)
    {
        boardRef = boards.document(id)
    }

    constructor(board: Board): this(board.id)
    {
        boardData = board
    }

    override fun load(onSuccess: (Board) -> Unit, onFailed: () -> Unit)
    {
        if(boardData == null) {
            boardRef.get().addOnCompleteListener {
                if(it.isSuccessful) {
                    it.result.run {
                        val name = this.getString(Board.KEY_NAME) ?: ""
                        val permissionLevel = this.getLong(Board.KEY_PERMISSION_LEVEL)
                        boardData = Board(this.id, name, null, permissionLevel)
                        onSuccess(boardData!!)
                    }
                }
                else
                    onFailed()
            }

            return
        }

        onSuccess(boardData!!)
    }

    override fun writePost(post: Post, author: User, onComplete: () -> Unit): Boolean
    {
        try {
            val hashMap = post.toHashMap()
            hashMap[Post.KEY_AUTHOR] = db.document(hashMap[Post.KEY_AUTHOR] as String)

            boardRef.collection("posts").document().set(hashMap).addOnCompleteListener {
                onComplete()
            }
            return true
        } catch(err: Exception) {}
        return false
    }

    override fun readPost(id: String, onComplete: (Post?) -> Unit): Boolean
    {
        db.collectionGroup("posts").whereEqualTo(FieldPath.documentId(), id).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val doc = it.result.documents.firstOrNull()
                doc?.let { documentSnapshot ->
                    onComplete(documentToPost(documentSnapshot))
                } ?: onComplete(null)
            }
            else
                onComplete(null)
        }

        return false
    }

    override fun deletePost(post: Post, user: User, onComplete: () -> Unit): Boolean
    {
        //club의 managePermission 또는 masterPermission을 가지고 있을때만 작동, 또는 admin 속성이 true일 때 작동

        return false
    }

    override fun getBoardData(): Board? = boardData

    override fun getPostList(): List<Post>?
    {

        return null
    }

    private var queryFlag = false
    private var lastDoc: DocumentSnapshot? = null

    override fun getPostListLimited(reset:Boolean, limit: Long, onComplete: (List<Post>) -> Unit): Boolean
    {
        if(reset)
            lastDoc = null

        if(queryFlag)
            return false

        val posts = boardRef.collection("posts")

        queryFlag = true
        val lim = if(limit > 100) 100 else limit
        boardData?.let {
            val query = if(lastDoc != null)
                posts.startAfter(lastDoc).limit(lim).orderBy(Post.KEY_DATE, Query.Direction.DESCENDING)
            else
                posts.limit(lim).orderBy(Post.KEY_DATE, Query.Direction.DESCENDING)

            query.get().addOnCompleteListener {
                if(it.isSuccessful) {
                    val list = ArrayList<Post>()

                    for(doc in it.result.documents) {
                        list.add(documentToPost(doc))
                    }
                    if(it.result.documents.isNotEmpty())
                        lastDoc = it.result.documents.last()
                    onComplete(list)
                }
                queryFlag = false
            }
        }
        return true
    }

    override fun checkWritePermission(user: User, board: Board, onComplete: (Boolean) -> Unit)
    {
        if(board.permissionLevel == null) {
            onComplete(true)
            return
        }

        val userRef = db.collection("users").document(user.id!!)
        relations.whereEqualTo("user", userRef).get().addOnCompleteListener {
            if(it.isSuccessful) {
                val permissionLevel = it.result.firstOrNull()?.getLong("permissionLevel")
                permissionLevel?.let { level ->
                    onComplete(level <= board.permissionLevel!!)
                } ?: onComplete(false)
            }
            else
                onComplete(false)
        }
    }

    private fun documentToPost(document: DocumentSnapshot): Post
    {
        val id = document.id
        val title = document.getString("title").toString()
        val text = document.getString("text").toString()
        val date = document.getDate("date")
        val author = document.getDocumentReference("author")?.path

        return Post(id, title, text, date!!, author!!)
    }
}