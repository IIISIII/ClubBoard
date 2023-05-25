package edu.sns.clubboard.data

import java.util.Date

data class Post(val id: String, val title: String, val text: String, val date: Date, val authorId: String, val postType: Long = TYPE_NORMAL, val parent: String? = null)
{
    var targetClubId: String? = null

    companion object
    {
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
        const val KEY_DATE = "date"
        const val KEY_AUTHOR = "author"
        const val KEY_TYPE = "type"

        //Recruit
        const val KEY_TARGET_CLUB = "parent"

        const val TYPE_NORMAL = 0L
        const val TYPE_RECRUIT = 1L
        const val TYPE_PROMOTION = 2L
    }

    fun toHashMap(): HashMap<String, Any> = hashMapOf(
        KEY_TITLE to title,
        KEY_TEXT to text,
        KEY_DATE to date,
        KEY_AUTHOR to authorId
    )
}
