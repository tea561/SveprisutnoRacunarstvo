package com.example.ubicompapplication

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.ubicompapplication.Constants.Companion.CHANNEL_ID
import com.example.ubicompapplication.Constants.Companion.notificationId
import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date


class NotificationService: Service() {
    @SuppressLint("SimpleDatFormat")
    private val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "parameters channel"
            val descriptionText = "Real time measured parameters"
            val importance = NotificationManager.IMPORTANCE_NONE

            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val description = "Person detected at ${format.format(Date(Calendar.getInstance().time.time)).substring(11)}"

        val builderNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.smart_home)
            .setContentTitle("Smart connect")
            .setContentText(description)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//        builderNotification.setContentIntent(pendingIntent)

        val notification: Notification = builderNotification.build()
        startForeground(notificationId, notification)

        return  START_STICKY
    }
}