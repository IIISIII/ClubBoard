package edu.sns.clubboard.data

data class User(val studentId: String, val name: String, val phone: String, val email: String, var nickname: String, var loginId: String, var imagePath: String? = null)
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
    }

    fun toHashMap(): HashMap<String, String>
    {
        if(imagePath == null) {
            return hashMapOf(
                KEY_STUDENTID to studentId,
                KEY_NAME to name,
                KEY_PHONE to phone,
                KEY_EMAIL to email,
                KEY_NICKNAME to nickname,
                KEY_LOGINID to loginId
            )
        }
        return hashMapOf(
            KEY_STUDENTID to studentId,
            KEY_NAME to name,
            KEY_PHONE to phone,
            KEY_EMAIL to email,
            KEY_NICKNAME to nickname,
            KEY_LOGINID to loginId,
            KEY_PROFILE_IMG to imagePath!!
        )
    }
}