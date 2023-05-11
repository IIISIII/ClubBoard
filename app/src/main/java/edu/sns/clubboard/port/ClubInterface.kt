package edu.sns.clubboard.port

import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.User

interface ClubInterface
{
    fun load(id: String, onComplete: (Club, List<Board>) -> Unit, onFailed: () -> Unit)

    fun createClub(clubName: String, user: User, onComplete: (Club) -> Unit, onFailed: () -> Unit)

    fun getClubMembers(club: Club, onComplete: (List<User>) -> Unit, onFailed: () -> Unit)

    fun getClubListLimited(reset: Boolean, limit: Long, onComplete: (List<Club>) -> Unit): Boolean

    fun getUserClubList(user: User, reset: Boolean, limit: Long, onComplete: (List<Club>) -> Unit): Boolean

    suspend fun isMember(user: User, club: Club): Boolean

    suspend fun isManager(user: User, club: Club): Boolean

    suspend fun isMaster(user: User, club: Club): Boolean

    suspend fun getPermissionLevel(user: User, club: Club): Int?
}