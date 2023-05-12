package edu.sns.clubboard.data

data class Club(val id: String, val name: String)
{
    companion object {
        const val KEY_NAME = "name"
        const val KEY_ACTIVATE = "activate"
    }
}