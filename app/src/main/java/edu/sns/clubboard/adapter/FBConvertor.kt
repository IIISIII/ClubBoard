package edu.sns.clubboard.adapter

import com.google.firebase.firestore.DocumentSnapshot
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User

class FBConvertor
{
    companion object {
        fun documentToUser(document: DocumentSnapshot): User
        {
            val email = document.getString(User.KEY_EMAIL)
            val studentId = document.getString(User.KEY_STUDENTID)
            val name = document.getString(User.KEY_NAME)
            val nickname = document.getString(User.KEY_NICKNAME)
            val phone = document.getString(User.KEY_PHONE)
            val loginId = document.getString(User.KEY_LOGINID)
            val profileImg = document.getString(User.KEY_PROFILE_IMG)
            val isAdmin = document.getBoolean(User.KEY_ADMIN) ?: false
            val isTestUser = document.getBoolean(User.KEY_TEST_USER) ?: false

            return User(document.id, studentId!!, name!!, phone!!, email!!, nickname!!, loginId!!, profileImg, isAdmin, isTestUser)
        }

        fun documentToClub(document: DocumentSnapshot): Club
        {
            val id = document.id
            val name = document.getString(Club.KEY_NAME) ?: ""
            val description = document.getString(Club.KEY_DESCRIPTION)
            val imgPath = document.getString(Club.KEY_IMGPATH)
            val isActivated = document.getBoolean(Club.KEY_ACTIVATE) ?: false
            return Club(id, name, description, imgPath, isActivated)
        }

        fun documentToPost(document: DocumentSnapshot): Post
        {
            val id = document.id
            val title = document.getString(Post.KEY_TITLE).toString()
            val text = document.getString(Post.KEY_TEXT).toString()
            val date = document.getDate(Post.KEY_DATE)!!
            val author = document.getDocumentReference(Post.KEY_AUTHOR)?.id!!
            val postImgPath = document.getString(Post.KEY_IMG)
            val postType = document.getLong(Post.KEY_TYPE) ?: Post.TYPE_NORMAL
            val targetClubId = document.getDocumentReference(Post.KEY_TARGET_CLUB)?.id

            val post = Post(id, title, text, date, author, postImgPath, postType)
            if(postType == Post.TYPE_RECRUIT)
                post.targetClubId = targetClubId

            return post
        }
    }
}