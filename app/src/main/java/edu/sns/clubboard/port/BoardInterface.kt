package edu.sns.clubboard.port

import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User

interface BoardInterface
{
    fun getBoardData(id: String, onSuccess: (Board) -> Unit, onFailed: () -> Unit)

    fun writePost(board: Board, post: Post, author: User, onComplete: () -> Unit): Boolean

    fun readPost(boardId: String, postId: String, onComplete: (Post) -> Unit, onFailed: () -> Unit): Boolean

    fun deletePost(board: Board, post: Post, user: User, onComplete: () -> Unit): Boolean

    fun getBoardListByIdList(list: List<String>, onSuccess: (List<Board>) -> Unit, onFailed: () -> Unit)

    fun getParent(board: Board, onSuccess: (Club?) -> Unit, onFailed: () -> Unit)

    fun getPostList(board: Board): List<Post>?

    fun getPostListLimited(board: Board, reset: Boolean, limit: Long, onComplete: (List<Post>, Boolean) -> Unit): Boolean

    fun checkWritePermission(user: User, board: Board, onComplete: (Boolean) -> Unit)
}