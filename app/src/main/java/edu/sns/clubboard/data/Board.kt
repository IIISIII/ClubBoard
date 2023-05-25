package edu.sns.clubboard.data

data class Board(val id: String, val name: String, var parent: Club? = null, var permissionLevel: Long?, var parentId: String? = null, val readOnly: Boolean = false)
{
    companion object {
        const val KEY_NAME = "name"
        const val KEY_PARENT = "parent"
        const val KEY_PERMISSION_LEVEL = "leastPermissionLevel"

        const val KEY_READONLY = "readOnly"

        const val KEY_ORDER = "order"
    }
}
