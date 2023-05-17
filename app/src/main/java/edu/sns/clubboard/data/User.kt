package edu.sns.clubboard.data

data class User(var id: String?, val studentId: String, val name: String, val phone: String, val email: String, var nickname: String = "user", var loginId: String, var imagePath: String? = null, val admin: Boolean = false)
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