package edu.sns.clubboard.data

data class User(var id: String?, val studentId: String, val name: String, val phone: String, val email: String, var nickname: String = "user", var loginId: String, var imagePath: String? = null, val isAdmin: Boolean = false, val isTestUser: Boolean = false)
{
    companion object
    {
        const val KEY_STUDENTID = "student_id"
        const val KEY_NAME = "name"
        const val KEY_PHONE = "phone"
        const val KEY_EMAIL = "email"
        const val KEY_NICKNAME = "nickname"
        const val KEY_LOGINID = "login_id"
        const val KEY_PROFILE_IMG = "image_path"
        const val KEY_ADMIN = "admin"
        const val KEY_PREVIEW_LIST = "preview_list"

        const val KEY_TEST_USER = "test_user"

        //relation
        const val KEY_RELATION_USER = "user"
        const val KEY_RELATION_CLUB = "club"
        const val KEY_RELATION_PERMISSION_LEVEL = "permissionLevel"

        const val PERMISSION_LEVEL_MEMBER = 2L
        const val PERMISSION_LEVEL_MANAGER = 1L
        const val PERMISSION_LEVEL_MASTER = 0L

        const val SUCCESS_DELETE_ACCOUNT = 0
        const val ERROR_DELETE_ACCOUNT_MASTER = 1
        const val ERROR_DELETE_ACCOUNT = 2
    }

    fun toHashMap(): HashMap<String, Any?>
    {
        return hashMapOf(
            KEY_STUDENTID to studentId,
            KEY_NAME to name,
            KEY_PHONE to phone,
            KEY_EMAIL to email,
            KEY_NICKNAME to nickname,
            KEY_LOGINID to loginId,
            KEY_PROFILE_IMG to imagePath
        )
    }
}