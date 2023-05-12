package edu.sns.clubboard.port

import android.graphics.Bitmap
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.User

interface ClubInterface
{
    fun createClub(clubName: String, description: String, img: Bitmap?, user: User, onComplete: (Club) -> Unit, onFailed: () -> Unit)

    fun getClubData(id: String, onComplete: (Club, List<Board>) -> Unit, onFailed: () -> Unit)

    fun getClubMembers(club: Club, onComplete: (List<User>) -> Unit, onFailed: () -> Unit)

    fun getClubListLimited(reset: Boolean, limit: Long, onComplete: (List<Club>) -> Unit): Boolean

    fun getUserClubList(user: User, onComplete: (List<Club>) -> Unit): Boolean

    suspend fun isMember(user: User, club: Club): Boolean

    suspend fun isManager(user: User, club: Club): Boolean

    suspend fun isMaster(user: User, club: Club): Boolean

    suspend fun getPermissionLevel(user: User, club: Club): Int?
}