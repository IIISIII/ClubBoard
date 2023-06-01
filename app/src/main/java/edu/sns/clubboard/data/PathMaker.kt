package edu.sns.clubboard.data

import java.util.Date
import java.util.Random

class PathMaker
{
    companion object {
        fun makeWithClub(clubName: String): String
        {
            return "${clubName}/0"
        }

        fun makeWithClub(club: Club): String
        {
            return "${club.name}/0"
        }

        fun makeForPost(title: String, date: Date, board: Board): String
        {
            if(board.parent != null)
                return "${board.parent!!.name}/${board.id}/${title}-${date}-${Random().nextInt()}"
            return "${board.id}/${title}-${date}-${Random().nextInt()}"
        }

        fun makeWithUser(user: User): String
        {
            return "${user.email}/${user.name}"
        }
    }
}