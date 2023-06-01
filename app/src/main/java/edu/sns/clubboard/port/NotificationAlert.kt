package edu.sns.clubboard.port

import edu.sns.clubboard.data.NotificationData
import edu.sns.clubboard.data.User

abstract class NotificationAlert
{
    private val events: ArrayList<(List<NotificationData>) -> Unit> = ArrayList()

    private val notifications: ArrayList<NotificationData> = ArrayList()

    abstract fun registerUser(user: User)

    abstract fun unregisterUser()

    fun getNotifications() = notifications

    fun addEvent(onNotificationArrived: (List<NotificationData>) -> Unit)
    {
        events.add(onNotificationArrived)
    }

    fun removeEvent(onNotificationArrived: (List<NotificationData>) -> Unit)
    {
        events.remove(onNotificationArrived)
    }

    fun notifyEvents(notificationList: List<NotificationData>)
    {
        events.forEach {
            it.invoke(notificationList)
            notifications.addAll(notificationList)
        }
    }
}