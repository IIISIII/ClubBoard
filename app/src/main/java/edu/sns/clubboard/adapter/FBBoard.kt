package edu.sns.clubboard.adapter

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Permission
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User
import edu.sns.clubboard.port.BoardInterface

class FBBoard: BoardInterface
{
    private val db = Firebase.firestore

    private val boards = db.collection("boards")

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
                        boardData = Board(this.id, name)
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

    override fun readPost(): Boolean
    {
        return false
    }

    override fun deletePost(post: Post, user: User, onComplete: () -> Unit): Boolean
    {
        //club의 managePermission 또는 masterPermission을 가지고 있을때만 작동, 또는 admin 속성이 true일 때 작동
        if(user.admin || user.permissions?.contains(boardData?.parent?.masterPermission) == true || user.permissions?.contains(boardData?.parent?.masterPermission) == true) {

            return true
        }
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

    override fun havePermission(user: User, board: Board): Boolean
    {
        board.permissions?.let {
            for(permission in it) {
                if(user.permissions?.contains(permission) == true)
                    return true
            }
        } ?: return true
        return false
    }

    private fun documentToPost(document: DocumentSnapshot): Post
    {
        val title = document.getString("title").toString()
        val text = document.getString("text").toString()
        val date = document.getDate("date")
        val author = document.getDocumentReference("author")?.path

        return Post(title, text, date!!, author!!)
    }
}