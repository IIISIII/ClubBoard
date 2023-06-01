package edu.sns.clubboard.port

import android.graphics.Bitmap
import edu.sns.clubboard.data.*
import java.util.Date

interface ClubInterface
{
    fun createClub(clubName: String, description: String, imgPath: String?, user: User, onComplete: (Club) -> Unit, onFailed: () -> Unit)

    fun activateClub(club: Club, onSuccess: () -> Unit, onFailed: () -> Unit)

    fun getClubData(id: String, onComplete: (Club, List<Board>) -> Unit, onFailed: () -> Unit)

    fun getClubMembers(club: Club, onComplete: (List<Member>) -> Unit, onFailed: () -> Unit)

    fun getClubListLimited(reset: Boolean, limit: Long, onComplete: (List<Club>, Boolean) -> Unit): Boolean

    fun getUserClubList(user: User, onComplete: (List<Club>) -> Unit): Boolean

    fun sendRequest(user: User, introduce: String, date: Date, club: Club, onComplete: () -> Unit, onFailed: () -> Unit)

    fun getRequests(club: Club, onComplete: (List<Request>) -> Unit, onFailed: () -> Unit)

    fun acceptRequests(requests: List<Request>, onComplete: () -> Unit, onFailed: () -> Unit)

    fun declineRequests(club: Club, requests: List<Request>, onComplete: () -> Unit, onFailed: () -> Unit)

    fun checkProcessingRequest(user: User, club: Club, onComplete: (Boolean) -> Unit)

    fun checkClubMember(userId: String, clubId: String, onComplete: (Boolean, Long?) -> Unit)

    fun checkIsActivated(clubId: String, onComplete: (Boolean) -> Unit)

    fun setUserPemissionLevel(clubId: String, user: User, master: User, permissionLevel: Long, onComplete: (Boolean) -> Unit)

    fun kick(clubId: String, user: User, onComplete: (Boolean) -> Unit)

    fun modifyClubInfo(club: Club, description: String, imgPath: String?, onComplete: (Boolean) -> Unit)

    suspend fun isMember(user: User, club: Club): Boolean

    suspend fun isManager(user: User, club: Club): Boolean

    suspend fun isMaster(user: User, club: Club): Boolean

    suspend fun getPermissionLevel(user: User, club: Club): Long?

    suspend fun getPermissionLevel(userId: String, clubId: String): Long?

    suspend fun isClubMember(user: User, club: Club): Boolean

    suspend fun isClubMember(userId: String, clubId: String): Boolean

    suspend fun isValidName(clubName: String): Boolean

    suspend fun getClubMemberCount(club: Club): Long
}