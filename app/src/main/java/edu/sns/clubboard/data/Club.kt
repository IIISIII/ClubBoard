package edu.sns.clubboard.data

data class Club(val id: String, val name: String, val memberIds: List<String>, val joinPermission: Permission, val managePermission: Permission, val masterPermission: Permission)
{
    companion object {
        const val KEY_NAME = "name"
        const val KEY_MEMBERS = "members"
        const val KEY_JOIN_PERMISSION = "join_permission"
        const val KEY_MANAGE_PERMISSION = "manage_permission"
        const val KEY_MASTER_PERMISSION = "master_permission"
    }
}