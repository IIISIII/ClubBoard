package edu.sns.clubboard.data

import java.util.Date

data class Request(val id: String, val userId: String, val userName: String, val userStudentId: String, val introduce: String, val date: Date, val club: Club)
{
    companion object {
        val KEY_USER = "user"
        val KEY_USER_NAME = "userName"
        val KEY_USER_STUDENT_ID = "userStudentId"
        val KEY_INTRODUCE = "introduce"
        val KEY_DATE = "date"
    }
}
