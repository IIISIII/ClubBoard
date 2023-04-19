package edu.sns.clubboard.port

import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User

interface BoardInterface
{
    fun load(onSuccess: (Board) -> Unit, onFailed: () -> Unit)

    fun writePost(post: Post, author: User, onComplete: () -> Unit): Boolean

    fun readPost(): Boolean

    fun deletePost(post: Post, user: User, onComplete: () -> Unit): Boolean

    fun getBoardData(): Board?

    fun getPostList(): List<Post>?

    fun getPostListLimited(reset: Boolean, limit: Long, onComplete: (List<Post>) -> Unit): Boolean

    fun havePermission(user: User, board: Board): Boolean
}