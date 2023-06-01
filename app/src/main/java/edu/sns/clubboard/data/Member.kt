package edu.sns.clubboard.data

data class Member(val user: User, var level: Long?): Comparable<Member>
{
    override fun compareTo(other: Member): Int = compareValuesBy(this, other, { it.level }, { it.user.name })
}
