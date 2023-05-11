package edu.sns.clubboard.data

import java.util.Date

data class Post(val id: String, val title: String, val text: String, val date: Date, val authorId: String)
{
    companion object
    {
        const val KEY_TITLE = "title"
        const val KEY_TEXT = "text"
        const val KEY_DATE = "date"
        const val KEY_AUTHOR = "author"
    }

    fun toHashMap(): HashMap<String, Any> = hashMapOf(
        KEY_TITLE to title,
        KEY_TEXT to text,
        KEY_DATE to date,
        KEY_AUTHOR to authorId
    )
}
