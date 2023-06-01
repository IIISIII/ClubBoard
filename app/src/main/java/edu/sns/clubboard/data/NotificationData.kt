package edu.sns.clubboard.data

data class NotificationData(val type: Long, val content: String, val targetId: String? = null)
