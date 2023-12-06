package com.example.todoapp.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.todoapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class NotificationService : FirebaseMessagingService() {

    val channelID = "AssignTodo"
    override fun onMessageReceived(message: RemoteMessage) {

        val manager = getSystemService(NOTIFICATION_SERVICE)
        createNotificationChannel(manager as NotificationManager)

        val notification = NotificationCompat.Builder(this, channelID)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setSmallIcon(R.drawable.baseline_done_24)
            .setAutoCancel(false)
            .setContentIntent(null)
            .build()

        manager.notify(Random.nextInt(), notification)
    }


    private fun createNotificationChannel(manager: NotificationManager) {

        val  channel = NotificationChannel(channelID, "assigntodo", NotificationManager.IMPORTANCE_HIGH)
            .apply {
                description = "New Todo"
                enableLights(true)
            }
            manager.createNotificationChannel(channel)
    }
}