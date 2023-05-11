package edu.sns.clubboard.data

data class Board(val id: String, val name: String, var parent: Club? = null, var permissionLevel: Long?)
{
    companion object {
        const val KEY_NAME = "name"
        const val KEY_PARENT = "parent"
        const val KEY_PERMISSION_LEVEL = "leastPermissionLevel"
    }
}
