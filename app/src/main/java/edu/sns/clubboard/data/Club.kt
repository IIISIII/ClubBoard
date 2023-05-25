package edu.sns.clubboard.data

data class Club(val id: String, val name: String, var description: String? = null, var imgPath: String? = null, var isActivated: Boolean = false)
{
    companion object {
        const val KEY_NAME = "name"
        const val KEY_IMGPATH = "imgPath"
        const val KEY_DESCRIPTION = "description"
        const val KEY_ACTIVATE = "activate"
    }
}