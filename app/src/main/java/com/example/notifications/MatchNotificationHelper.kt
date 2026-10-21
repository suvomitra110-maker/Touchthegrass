package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object MatchNotificationHelper {

    const val CHANNEL_MAJOR_GOALS = "framextv_major_goals_channel"
    const val CHANNEL_KICKOFF_ALERTS = "framextv_kickoff_channel"

    fun ensureChannelsCreated(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val goalChannel = NotificationChannel(
                CHANNEL_MAJOR_GOALS,
                "Major Goal Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time push alerts for major goals across UCL, EPL, LaLiga, Serie A, Bundesliga, Inter Miami & International Cups"
                enableVibration(true)
            }

            val kickoffChannel = NotificationChannel(
                CHANNEL_KICKOFF_ALERTS,
                "Match Kickoff & Lineup Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when subscribed live football matches are kicking off"
                enableVibration(true)
            }

            manager.createNotificationChannel(goalChannel)
            manager.createNotificationChannel(kickoffChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun sendMajorGoalNotification(
        context: Context,
        competition: String,
        scoreline: String,
        headline: String,
        detail: String
    ): Boolean {
        ensureChannelsCreated(context)
        if (!hasNotificationPermission(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            scoreline.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_MAJOR_GOALS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚽ GOAL! [$competition] $scoreline")
            .setContentText(headline)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$headline\n$detail")
                    .setSummaryText("FrameXTV Live Football • $competition")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context)
                .notify((System.currentTimeMillis() % 100000).toInt(), notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    fun sendKickoffNotification(
        context: Context,
        competition: String,
        matchTitle: String,
        venue: String,
        streamServerName: String
    ): Boolean {
        ensureChannelsCreated(context)
        if (!hasNotificationPermission(context)) return false

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            matchTitle.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_KICKOFF_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔔 KICKOFF ALERT: $matchTitle")
            .setContentText("$competition is kicking off at $venue • Streaming in 4K on $streamServerName")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$matchTitle is now LIVE at $venue!\nStream live in 4K HDR on $streamServerName with real-time commentary.")
                    .setSummaryText("FrameXTV Kickoff Alert • $competition")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context)
                .notify(((System.currentTimeMillis() + 500) % 100000).toInt(), notification)
            true
        } catch (e: SecurityException) {
            false
        }
    }
}
